package com.pharmacopee.ladafura.schema;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
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
import com.pharmacopee.ladafura.Models.LimiteAbonnement;
import com.pharmacopee.ladafura.Models.LimiteOffre;
import com.pharmacopee.ladafura.Models.MembrePharmacopee;
import com.pharmacopee.ladafura.Models.OffreAbonnement;
import com.pharmacopee.ladafura.Models.Pharmacopee;
import com.pharmacopee.ladafura.Models.RolePharmacopee;
import com.pharmacopee.ladafura.Models.Utilisateur;
import com.pharmacopee.ladafura.dto.pharmacopee.membre.InviterMembreRequest;
import com.pharmacopee.ladafura.dto.pharmacopee.membre.MembreResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.membre.TransfertProprieteRequest;
import com.pharmacopee.ladafura.enums.Role;
import com.pharmacopee.ladafura.enums.StatutAbonnement;
import com.pharmacopee.ladafura.enums.StatutMembre;
import com.pharmacopee.ladafura.enums.StatutPharmacopee;
import com.pharmacopee.ladafura.repository.AttributionRolePharmacopeeRepository;
import com.pharmacopee.ladafura.repository.RolePharmacopeeRepository;
import com.pharmacopee.ladafura.services.impl.JournalAuditServiceImpl;
import com.pharmacopee.ladafura.services.impl.PharmacopeeContexteServiceImpl;
import com.pharmacopee.ladafura.services.impl.PharmacopeeInvitationServiceImpl;
import com.pharmacopee.ladafura.services.impl.PharmacopeeMembreServiceImpl;
import com.pharmacopee.ladafura.services.impl.PharmacopeeQuotaServiceImpl;
import com.pharmacopee.ladafura.services.interfaces.IFirebaseAuthService;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeAuthService;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeInvitationService;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeMembreService;

/**
 * Cycle complet des membres sur une base MySQL VIERGE, avec les services réels.
 * Mêmes prérequis que {@link SchemaMigrationIntegrationTest}. Ne JAMAIS pointer vers Aiven.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@EnabledIfEnvironmentVariable(named = "LADAFURA_TEST_DB_URL", matches = ".+")
@Import({PharmacopeeContexteServiceImpl.class, PharmacopeeQuotaServiceImpl.class, JournalAuditServiceImpl.class,
        PharmacopeeMembreServiceImpl.class, PharmacopeeInvitationServiceImpl.class})
class MembresIntegrationTest {

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
    @Autowired private IPharmacopeeMembreService membreService;
    @Autowired private IPharmacopeeInvitationService invitationService;
    @Autowired private RolePharmacopeeRepository roleRepository;
    @Autowired private AttributionRolePharmacopeeRepository attributionRepository;
    @MockitoBean private IPharmacopeeAuthService pharmacopeeAuthService;
    @Autowired private com.pharmacopee.ladafura.repository.MembrePharmacopeeRepository adhesionsDeTest;

    /** Le service d'authentification est simulé : l'adhésion courante suit l'utilisateur simulé. */
    @org.junit.jupiter.api.BeforeEach
    void brancherMembreCourant() {
        when(pharmacopeeAuthService.getMembreCourant()).thenAnswer(appel -> adhesionsDeTest
                .findByUtilisateurIdAndStatut(pharmacopeeAuthService.getCurrentUtilisateur().getId(), com.pharmacopee.ladafura.enums.StatutMembre.ACTIF)
                .get(0));
    }

    @MockitoBean private IFirebaseAuthService firebaseAuthService;

    @Test
    @DisplayName("Invitation, acceptation, suspension, réactivation puis transfert de propriété")
    void cycleComplet() {
        Utilisateur gerant = utilisateur("gerant@test.ml");
        Utilisateur employe = utilisateur("employe@test.ml");
        Pharmacopee officine = em.persist(Pharmacopee.builder().nom("Officine de Kayes").statut(StatutPharmacopee.VALIDEE).build());
        MembrePharmacopee proprietaire = em.persist(MembrePharmacopee.builder().utilisateur(gerant).pharmacopee(officine)
                .statut(StatutMembre.ACTIF).estProprietaire(true).build());
        em.persist(AttributionRolePharmacopee.builder().membrePharmacopee(proprietaire).rolePharmacopee(role("GERANT")).build());
        abonnementActif(officine, 5);
        em.flush();

        when(pharmacopeeAuthService.getCurrentUtilisateur()).thenReturn(gerant);
        MembreResponse invite = membreService.inviter(InviterMembreRequest.builder()
                .email("employe@test.ml").roleIds(List.of(role("MANAGER").getId())).build());
        assertThat(invite.getStatut()).isEqualTo(StatutMembre.INVITE);
        em.flush();

        when(pharmacopeeAuthService.getCurrentUtilisateur()).thenReturn(employe);
        assertThat(invitationService.getMesInvitations()).hasSize(1);
        invitationService.accepter(invite.getId());
        em.flush();

        when(pharmacopeeAuthService.getCurrentUtilisateur()).thenReturn(gerant);
        assertThat(membreService.getMembre(invite.getId()).getEmail()).isEqualTo("employe@test.ml");
        assertThat(membreService.suspendre(invite.getId()).getStatut()).isEqualTo(StatutMembre.SUSPENDU);
        assertThat(membreService.reactiver(invite.getId()).getStatut()).isEqualTo(StatutMembre.ACTIF);
        em.flush();

        MembreResponse nouveauGerant = membreService.transfererPropriete(new TransfertProprieteRequest(invite.getId()));
        em.flush();
        em.clear();

        assertThat(nouveauGerant.getEstProprietaire()).isTrue();
        assertThat(codesRoles(invite.getId())).containsExactly("GERANT");
        assertThat(codesRoles(proprietaire.getId())).containsExactly("MANAGER");
        assertThat(em.find(MembrePharmacopee.class, proprietaire.getId()).getEstProprietaire()).isFalse();
        assertThat(em.find(Pharmacopee.class, officine.getId()).getUtilisateur().getId()).isEqualTo(employe.getId());
    }

    private List<String> codesRoles(Long membreId) {
        return attributionRepository.findByMembrePharmacopeeId(membreId).stream()
                .map(a -> a.getRolePharmacopee().getCode()).toList();
    }

    private RolePharmacopee role(String code) {
        return roleRepository.findByCodeAndPharmacopeeIsNull(code).orElseThrow();
    }

    private Utilisateur utilisateur(String email) {
        Utilisateur u = new Utilisateur();
        u.setNom("Coulibaly");
        u.setPrenom("Fanta");
        u.setEmail(email);
        u.setRole(Role.PHARMACOPEE);
        return em.persist(u);
    }

    private void abonnementActif(Pharmacopee officine, int maxUtilisateurs) {
        OffreAbonnement gratuite = em.getEntityManager()
                .createQuery("select o from OffreAbonnement o where o.code = 'GRATUITE'", OffreAbonnement.class).getSingleResult();
        LimiteOffre limite = em.getEntityManager()
                .createQuery("select l from LimiteOffre l where l.codeLimite = 'MAX_UTILISATEURS_ACTIFS'", LimiteOffre.class).getSingleResult();
        Abonnement abonnement = em.persist(Abonnement.builder().pharmacopee(officine).offre(gratuite).statut(StatutAbonnement.ACTIF)
                .codeOffreSouscrite(gratuite.getCode()).nomOffreSouscrite(gratuite.getNom())
                .prixSouscrit(BigDecimal.ZERO).deviseSouscrite("XOF").build());
        em.persist(LimiteAbonnement.builder().abonnement(abonnement).limiteOffre(limite)
                .codeLimite(limite.getCodeLimite()).valeurMaximale(maxUtilisateurs).illimite(false).build());
    }
}
