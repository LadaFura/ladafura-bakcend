package com.pharmacopee.ladafura.schema;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.pharmacopee.ladafura.Models.Abonnement;
import com.pharmacopee.ladafura.Models.AttributionRolePharmacopee;
import com.pharmacopee.ladafura.Models.LimiteOffre;
import com.pharmacopee.ladafura.Models.MembrePharmacopee;
import com.pharmacopee.ladafura.Models.OffreAbonnement;
import com.pharmacopee.ladafura.Models.Pharmacopee;
import com.pharmacopee.ladafura.Models.Utilisateur;
import com.pharmacopee.ladafura.dto.admin.abonnement.DefinirQuotasOffreRequest;
import com.pharmacopee.ladafura.dto.admin.abonnement.EnregistrerOffreRequest;
import com.pharmacopee.ladafura.dto.admin.abonnement.LigneQuotaRequest;
import com.pharmacopee.ladafura.dto.pharmacopee.abonnement.AbonnementResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.abonnement.DeclarerPaiementRequest;
import com.pharmacopee.ladafura.dto.pharmacopee.abonnement.FactureAbonnementResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.abonnement.PaiementAbonnementResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.abonnement.SouscrireOffreRequest;
import com.pharmacopee.ladafura.enums.ModePaiementAbonnement;
import com.pharmacopee.ladafura.enums.MotifSuspensionMembre;
import com.pharmacopee.ladafura.enums.Role;
import com.pharmacopee.ladafura.enums.StatutAbonnement;
import com.pharmacopee.ladafura.enums.StatutMembre;
import com.pharmacopee.ladafura.enums.StatutPharmacopee;
import com.pharmacopee.ladafura.repository.AbonnementRepository;
import com.pharmacopee.ladafura.repository.RolePharmacopeeRepository;
import com.pharmacopee.ladafura.services.impl.AbonnementCycleServiceImpl;
import com.pharmacopee.ladafura.services.impl.AdminAbonnementServiceImpl;
import com.pharmacopee.ladafura.services.impl.AdminOffreServiceImpl;
import com.pharmacopee.ladafura.services.impl.JournalAuditServiceImpl;
import com.pharmacopee.ladafura.services.impl.PharmacopeeAbonnementServiceImpl;
import com.pharmacopee.ladafura.services.impl.PharmacopeeContexteServiceImpl;
import com.pharmacopee.ladafura.services.impl.PharmacopeeQuotaServiceImpl;
import com.pharmacopee.ladafura.services.interfaces.IAbonnementCycleService;
import com.pharmacopee.ladafura.services.interfaces.IAdminAbonnementService;
import com.pharmacopee.ladafura.services.interfaces.IAdminOffreService;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeAbonnementService;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeAuthService;

/**
 * Parcours complet d'un abonnement sur une base MySQL VIERGE, avec les services réels.
 * Mêmes prérequis que {@link SchemaMigrationIntegrationTest}. Ne JAMAIS pointer vers Aiven.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@EnabledIfEnvironmentVariable(named = "LADAFURA_TEST_DB_URL", matches = ".+")
@Import({PharmacopeeContexteServiceImpl.class, PharmacopeeQuotaServiceImpl.class, JournalAuditServiceImpl.class,
        AbonnementCycleServiceImpl.class, PharmacopeeAbonnementServiceImpl.class, AdminOffreServiceImpl.class, AdminAbonnementServiceImpl.class})
class AbonnementsIntegrationTest {

    @DynamicPropertySource
    static void proprietes(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> System.getenv("LADAFURA_TEST_DB_URL"));
        registry.add("spring.datasource.username", () -> envOuDefaut("LADAFURA_TEST_DB_USER", "root"));
        registry.add("spring.datasource.password", () -> envOuDefaut("LADAFURA_TEST_DB_PASSWORD", ""));
        registry.add("spring.flyway.enabled", () -> "true");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
    }

    private static String envOuDefaut(String nom, String defaut) {
        String valeur = System.getenv(nom);
        return valeur != null ? valeur : defaut;
    }

    @Autowired private TestEntityManager em;
    @Autowired private IAdminOffreService offreService;
    @Autowired private IAdminAbonnementService adminService;
    @Autowired private IPharmacopeeAbonnementService abonnementService;
    @Autowired private IAbonnementCycleService cycleService;
    @Autowired private AbonnementRepository abonnementRepository;
    @Autowired private RolePharmacopeeRepository roleRepository;
    @MockitoBean private IPharmacopeeAuthService pharmacopeeAuthService;
    @MockitoBean private com.pharmacopee.ladafura.services.interfaces.IAbonnementNotificationService abonnementNotificationService;
    @Autowired private com.pharmacopee.ladafura.repository.MembrePharmacopeeRepository adhesionsDeTest;

    /** Le service d'authentification est simulé : l'adhésion courante suit l'utilisateur simulé. */
    @org.junit.jupiter.api.BeforeEach
    void brancherMembreCourant() {
        when(pharmacopeeAuthService.getMembreCourant()).thenAnswer(appel -> adhesionsDeTest
                .findByUtilisateurIdAndStatut(pharmacopeeAuthService.getCurrentUtilisateur().getId(), com.pharmacopee.ladafura.enums.StatutMembre.ACTIF)
                .get(0));
    }


    private int compteur;

    @Test
    @DisplayName("Souscription, paiement confirmé, facture, grâce, puis retour au gratuit avec suspension des membres en trop")
    void parcoursComplet() {
        Utilisateur admin = utilisateur();
        Utilisateur gerant = utilisateur();
        Pharmacopee officine = em.persist(Pharmacopee.builder().nom("Officine de Ségou").statut(StatutPharmacopee.VALIDEE).build());
        MembrePharmacopee proprietaire = membre(gerant, officine, true, StatutMembre.ACTIF);
        em.persist(AttributionRolePharmacopee.builder().membrePharmacopee(proprietaire)
                .rolePharmacopee(roleRepository.findByCodeAndPharmacopeeIsNull("GERANT").orElseThrow()).build());
        MembrePharmacopee employe = membre(utilisateur(), officine, false, StatutMembre.ACTIF);
        em.flush();
        Abonnement gratuit = cycleService.retournerAuGratuit(officine, null, admin);
        em.flush();
        assertThat(abonnementRepository.findEnVigueur(officine.getId())).contains(gratuit);

        // L'administrateur rend l'offre mensuelle vendable.
        when(pharmacopeeAuthService.getCurrentUtilisateur()).thenReturn(admin);
        OffreAbonnement mensuelle = offre("MENSUELLE");
        offreService.modifier(mensuelle.getId(), EnregistrerOffreRequest.builder().nom("Offre mensuelle").prix(new BigDecimal("10000")).joursGrace(7).build());
        List<LigneQuotaRequest> quotas = em.getEntityManager().createQuery("select l from LimiteOffre l", LimiteOffre.class).getResultList().stream()
                .map(l -> new LigneQuotaRequest(l.getId(), 5, false)).toList();
        offreService.definirQuotas(mensuelle.getId(), new DefinirQuotasOffreRequest(quotas));
        offreService.activer(mensuelle.getId());
        em.flush();

        // Le gérant souscrit et déclare son paiement.
        when(pharmacopeeAuthService.getCurrentUtilisateur()).thenReturn(gerant);
        AbonnementResponse souscription = abonnementService.souscrire(new SouscrireOffreRequest(mensuelle.getId()));
        PaiementAbonnementResponse paiement = abonnementService.declarerPaiement(souscription.getId(), DeclarerPaiementRequest.builder()
                .montant(new BigDecimal("10000")).modePaiement(ModePaiementAbonnement.ORANGE_MONEY).referenceTransaction("OM-1").build());
        em.flush();

        // L'administrateur confirme : la facture est émise et l'abonnement payant prend la place du gratuit.
        when(pharmacopeeAuthService.getCurrentUtilisateur()).thenReturn(admin);
        FactureAbonnementResponse facture = adminService.confirmerPaiement(paiement.getId());
        em.flush();
        assertThat(facture.getNumeroFacture()).endsWith("-000001");
        assertThat(gratuit.getStatut()).isEqualTo(StatutAbonnement.EXPIRE);
        Abonnement payant = abonnementRepository.findEnVigueur(officine.getId()).orElseThrow();
        assertThat(payant.getId()).isEqualTo(souscription.getId());
        assertThat(payant.getStatut()).isEqualTo(StatutAbonnement.ACTIF);

        // Échéance : passage en grâce.
        payant.setDateFin(LocalDateTime.now().minusDays(1));
        em.flush();
        cycleService.traiterEcheances(LocalDateTime.now());
        em.flush();
        assertThat(payant.getStatut()).isEqualTo(StatutAbonnement.EN_GRACE);

        // Fin de grâce : retour au gratuit, le gérant reste actif, l'employé est suspendu.
        payant.setDateFinGrace(LocalDateTime.now().minusMinutes(1));
        em.flush();
        cycleService.traiterEcheances(LocalDateTime.now());
        em.flush();
        assertThat(payant.getStatut()).isEqualTo(StatutAbonnement.EXPIRE);
        assertThat(abonnementRepository.findEnVigueur(officine.getId()).orElseThrow().getCodeOffreSouscrite()).isEqualTo("GRATUITE");
        assertThat(proprietaire.getStatut()).isEqualTo(StatutMembre.ACTIF);
        assertThat(employe.getStatut()).isEqualTo(StatutMembre.SUSPENDU);
        assertThat(employe.getMotifSuspension()).isEqualTo(MotifSuspensionMembre.QUOTA_ABONNEMENT);
    }

    private OffreAbonnement offre(String code) {
        return em.getEntityManager().createQuery("select o from OffreAbonnement o where o.code = :code", OffreAbonnement.class)
                .setParameter("code", code).getSingleResult();
    }

    private Utilisateur utilisateur() {
        Utilisateur u = new Utilisateur();
        u.setNom("Diarra");
        u.setPrenom("Moussa");
        u.setEmail("compte" + (++compteur) + "@test.ml");
        u.setRole(Role.PHARMACOPEE);
        return em.persist(u);
    }

    private MembrePharmacopee membre(Utilisateur utilisateur, Pharmacopee officine, boolean proprietaire, StatutMembre statut) {
        return em.persist(MembrePharmacopee.builder().utilisateur(utilisateur).pharmacopee(officine)
                .statut(statut).estProprietaire(proprietaire).dateAdhesion(LocalDateTime.now()).build());
    }
}
