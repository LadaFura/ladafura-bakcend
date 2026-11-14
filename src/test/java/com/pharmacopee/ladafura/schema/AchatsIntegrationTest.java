package com.pharmacopee.ladafura.schema;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.pharmacopee.ladafura.Models.Abonnement;
import com.pharmacopee.ladafura.Models.AttributionRolePharmacopee;
import com.pharmacopee.ladafura.Models.Caisse;
import com.pharmacopee.ladafura.Models.DisponibiliteProduit;
import com.pharmacopee.ladafura.Models.LimiteAbonnement;
import com.pharmacopee.ladafura.Models.LimiteOffre;
import com.pharmacopee.ladafura.Models.Lot;
import com.pharmacopee.ladafura.Models.MembrePharmacopee;
import com.pharmacopee.ladafura.Models.OffreAbonnement;
import com.pharmacopee.ladafura.Models.Pharmacopee;
import com.pharmacopee.ladafura.Models.Produit;
import com.pharmacopee.ladafura.Models.Utilisateur;
import com.pharmacopee.ladafura.dto.pharmacopee.achat.AchatResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.achat.EnregistrerAchatRequest;
import com.pharmacopee.ladafura.dto.pharmacopee.achat.EnregistrerReglementAchatRequest;
import com.pharmacopee.ladafura.dto.pharmacopee.achat.LigneAchatRequest;
import com.pharmacopee.ladafura.dto.pharmacopee.achat.ReglementAchatResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.fournisseur.EnregistrerFournisseurRequest;
import com.pharmacopee.ladafura.dto.pharmacopee.fournisseur.FournisseurResponse;
import com.pharmacopee.ladafura.enums.MethodePaiement;
import com.pharmacopee.ladafura.enums.Role;
import com.pharmacopee.ladafura.enums.StatutAbonnement;
import com.pharmacopee.ladafura.enums.StatutAchat;
import com.pharmacopee.ladafura.enums.StatutMembre;
import com.pharmacopee.ladafura.enums.StatutPharmacopee;
import com.pharmacopee.ladafura.enums.StatutProduit;
import com.pharmacopee.ladafura.enums.TypeCaisse;
import com.pharmacopee.ladafura.exceptions.ConflictException;
import com.pharmacopee.ladafura.repository.DisponibiliteProduitRepository;
import com.pharmacopee.ladafura.repository.OperationCaisseRepository;
import com.pharmacopee.ladafura.repository.RolePharmacopeeRepository;
import com.pharmacopee.ladafura.services.impl.JournalAuditServiceImpl;
import com.pharmacopee.ladafura.services.impl.PharmacopeeAchatServiceImpl;
import com.pharmacopee.ladafura.services.impl.PharmacopeeContexteServiceImpl;
import com.pharmacopee.ladafura.services.impl.PharmacopeeFournisseurServiceImpl;
import com.pharmacopee.ladafura.services.impl.PharmacopeeMouvementStockServiceImpl;
import com.pharmacopee.ladafura.services.impl.PharmacopeeOperationCaisseServiceImpl;
import com.pharmacopee.ladafura.services.impl.PharmacopeeQuotaServiceImpl;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeAchatService;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeAuthService;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeFournisseurService;

/**
 * Parcours complet d'un achat sur une base MySQL VIERGE, avec les services réels.
 * Mêmes prérequis que {@link SchemaMigrationIntegrationTest}. Ne JAMAIS pointer vers Aiven.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@EnabledIfEnvironmentVariable(named = "LADAFURA_TEST_DB_URL", matches = ".+")
@Import({PharmacopeeContexteServiceImpl.class, PharmacopeeQuotaServiceImpl.class, JournalAuditServiceImpl.class,
        PharmacopeeMouvementStockServiceImpl.class, PharmacopeeOperationCaisseServiceImpl.class,
        PharmacopeeFournisseurServiceImpl.class, PharmacopeeAchatServiceImpl.class})
class AchatsIntegrationTest {

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
    @Autowired private IPharmacopeeFournisseurService fournisseurService;
    @Autowired private IPharmacopeeAchatService achatService;
    @Autowired private DisponibiliteProduitRepository disponibiliteRepository;
    @Autowired private OperationCaisseRepository operationCaisseRepository;
    @Autowired private RolePharmacopeeRepository roleRepository;
    @MockitoBean private IPharmacopeeAuthService pharmacopeeAuthService;
    @Autowired private com.pharmacopee.ladafura.repository.MembrePharmacopeeRepository adhesionsDeTest;

    /** Le service d'authentification est simulé : l'adhésion courante suit l'utilisateur simulé. */
    @org.junit.jupiter.api.BeforeEach
    void brancherMembreCourant() {
        when(pharmacopeeAuthService.getMembreCourant()).thenAnswer(appel -> adhesionsDeTest
                .findByUtilisateurIdAndStatut(pharmacopeeAuthService.getCurrentUtilisateur().getId(), com.pharmacopee.ladafura.enums.StatutMembre.ACTIF)
                .get(0));
    }


    @Test
    @DisplayName("Brouillon, validation avec lot, règlement en caisse, annulation refusée puis réussie")
    void parcoursComplet() {
        Utilisateur gerant = new Utilisateur();
        gerant.setNom("Keita");
        gerant.setPrenom("Aminata");
        gerant.setEmail("gerant-achats@test.ml");
        gerant.setRole(Role.PHARMACOPEE);
        em.persist(gerant);
        Pharmacopee officine = em.persist(Pharmacopee.builder().nom("Officine de Mopti").statut(StatutPharmacopee.VALIDEE).build());
        MembrePharmacopee membre = em.persist(MembrePharmacopee.builder().utilisateur(gerant).pharmacopee(officine)
                .statut(StatutMembre.ACTIF).estProprietaire(true).build());
        em.persist(AttributionRolePharmacopee.builder().membrePharmacopee(membre)
                .rolePharmacopee(roleRepository.findByCodeAndPharmacopeeIsNull("GERANT").orElseThrow()).build());
        abonnementActif(officine);
        Produit produit = em.persist(Produit.builder().nom("Tisane de kinkéliba").prix(1500.0).statut(StatutProduit.VALIDE).build());
        Caisse compte = em.persist(Caisse.builder().pharmacopee(officine).nom("Mobile Money").type(TypeCaisse.MOBILE_MONEY).build());
        em.flush();
        when(pharmacopeeAuthService.getCurrentUtilisateur()).thenReturn(gerant);

        FournisseurResponse fournisseur = fournisseurService.creer(EnregistrerFournisseurRequest.builder().nom("Coopérative de Bandiagara").build());
        LigneAchatRequest ligne = LigneAchatRequest.builder().produitId(produit.getId()).quantite(10).prixUnitaire(new BigDecimal("1000"))
                .numeroLot("L-1").datePeremption(LocalDate.now().plusYears(1)).build();
        AchatResponse brouillon = achatService.creer(EnregistrerAchatRequest.builder().fournisseurId(fournisseur.getId()).lignes(List.of(ligne)).build());
        em.flush();
        assertThat(brouillon.getStatut()).isEqualTo(StatutAchat.BROUILLON);
        assertThat(disponibiliteRepository.findByPharmacopeeIdAndProduitId(officine.getId(), produit.getId())).isEmpty();

        AchatResponse valide = achatService.valider(brouillon.getId());
        em.flush();
        assertThat(valide.getStatut()).isEqualTo(StatutAchat.VALIDE);
        DisponibiliteProduit fiche = disponibiliteRepository.findByPharmacopeeIdAndProduitId(officine.getId(), produit.getId()).orElseThrow();
        assertThat(fiche.getQuantiteStock()).isEqualTo(10);
        assertThat(fiche.getDisponible()).isFalse();
        assertThat(em.getEntityManager().createQuery("select l from Lot l where l.disponibiliteProduit.id = :id", Lot.class)
                .setParameter("id", fiche.getId()).getSingleResult().getQuantiteRestante()).isEqualTo(10);
        assertThat(fournisseurService.getHistorique(fournisseur.getId(), PageRequest.of(0, 10)).getTotalElements()).isEqualTo(1);

        ReglementAchatResponse reglement = achatService.enregistrerReglement(brouillon.getId(), EnregistrerReglementAchatRequest.builder()
                .montant(new BigDecimal("4000")).methode(MethodePaiement.MOBILE_MONEY).caisseId(compte.getId()).build());
        em.flush();
        assertThat(operationCaisseRepository.soldeTheorique(compte.getId())).isEqualByComparingTo("-4000");
        assertThat(achatService.getAchat(brouillon.getId()).getResteAPayer()).isEqualByComparingTo("6000");
        assertThat(achatService.getAchats(null, fournisseur.getId(), PageRequest.of(0, 10)).getContent())
                .singleElement().satisfies(a -> assertThat(a.getMontantPaye()).isEqualByComparingTo("4000"));

        assertThatThrownBy(() -> achatService.annuler(brouillon.getId(), "Erreur")).isInstanceOf(ConflictException.class);

        achatService.annulerReglement(brouillon.getId(), reglement.getId(), "Doublon");
        em.flush();
        assertThat(operationCaisseRepository.soldeTheorique(compte.getId())).isEqualByComparingTo("0");

        assertThat(achatService.annuler(brouillon.getId(), "Marchandise refusée").getStatut()).isEqualTo(StatutAchat.ANNULE);
        em.flush();
        assertThat(fiche.getQuantiteStock()).isZero();
    }

    private void abonnementActif(Pharmacopee officine) {
        OffreAbonnement gratuite = em.getEntityManager()
                .createQuery("select o from OffreAbonnement o where o.code = 'GRATUITE'", OffreAbonnement.class).getSingleResult();
        LimiteOffre limite = em.getEntityManager()
                .createQuery("select l from LimiteOffre l where l.codeLimite = 'MAX_PRODUITS'", LimiteOffre.class).getSingleResult();
        Abonnement abonnement = em.persist(Abonnement.builder().pharmacopee(officine).offre(gratuite).statut(StatutAbonnement.ACTIF)
                .codeOffreSouscrite(gratuite.getCode()).nomOffreSouscrite(gratuite.getNom())
                .prixSouscrit(BigDecimal.ZERO).deviseSouscrite("XOF").build());
        em.persist(LimiteAbonnement.builder().abonnement(abonnement).limiteOffre(limite)
                .codeLimite(limite.getCodeLimite()).valeurMaximale(5).illimite(false).build());
    }
}
