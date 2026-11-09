package com.pharmacopee.ladafura.schema;

import static org.assertj.core.api.Assertions.assertThat;
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
import com.pharmacopee.ladafura.Models.DisponibiliteProduit;
import com.pharmacopee.ladafura.Models.LimiteAbonnement;
import com.pharmacopee.ladafura.Models.LimiteOffre;
import com.pharmacopee.ladafura.Models.MembrePharmacopee;
import com.pharmacopee.ladafura.Models.OffreAbonnement;
import com.pharmacopee.ladafura.Models.Pharmacopee;
import com.pharmacopee.ladafura.Models.Produit;
import com.pharmacopee.ladafura.Models.Utilisateur;
import com.pharmacopee.ladafura.dto.pharmacopee.caisse.CaisseResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.caisse.CloturerSessionRequest;
import com.pharmacopee.ladafura.dto.pharmacopee.caisse.EnregistrerCaisseRequest;
import com.pharmacopee.ladafura.dto.pharmacopee.caisse.OperationManuelleRequest;
import com.pharmacopee.ladafura.dto.pharmacopee.caisse.OuvrirSessionRequest;
import com.pharmacopee.ladafura.dto.pharmacopee.caisse.SessionCaisseResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.caisse.TransfertCaisseRequest;
import com.pharmacopee.ladafura.dto.pharmacopee.depense.EnregistrerDepenseRequest;
import com.pharmacopee.ladafura.dto.pharmacopee.tresorerie.SyntheseTresorerieResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.vente.EnregistrerVenteRequest;
import com.pharmacopee.ladafura.dto.pharmacopee.vente.LigneVenteRequest;
import com.pharmacopee.ladafura.dto.pharmacopee.vente.PaiementVenteRequest;
import com.pharmacopee.ladafura.dto.pharmacopee.vente.ValiderVenteRequest;
import com.pharmacopee.ladafura.enums.CategorieDepense;
import com.pharmacopee.ladafura.enums.MethodePaiement;
import com.pharmacopee.ladafura.enums.NatureOperationCaisse;
import com.pharmacopee.ladafura.enums.Role;
import com.pharmacopee.ladafura.enums.StatutAbonnement;
import com.pharmacopee.ladafura.enums.StatutMembre;
import com.pharmacopee.ladafura.enums.StatutPharmacopee;
import com.pharmacopee.ladafura.enums.StatutProduit;
import com.pharmacopee.ladafura.enums.StatutSessionCaisse;
import com.pharmacopee.ladafura.enums.TypeCaisse;
import com.pharmacopee.ladafura.repository.RolePharmacopeeRepository;
import com.pharmacopee.ladafura.services.impl.JournalAuditServiceImpl;
import com.pharmacopee.ladafura.services.impl.PharmacopeeCaisseServiceImpl;
import com.pharmacopee.ladafura.services.impl.PharmacopeeContexteServiceImpl;
import com.pharmacopee.ladafura.services.impl.PharmacopeeDepenseServiceImpl;
import com.pharmacopee.ladafura.services.impl.PharmacopeeMouvementStockServiceImpl;
import com.pharmacopee.ladafura.services.impl.PharmacopeeOperationCaisseServiceImpl;
import com.pharmacopee.ladafura.services.impl.PharmacopeeQuotaServiceImpl;
import com.pharmacopee.ladafura.services.impl.PharmacopeeTresorerieServiceImpl;
import com.pharmacopee.ladafura.services.impl.PharmacopeeVenteServiceImpl;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeAuthService;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeCaisseService;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeDepenseService;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeTresorerieService;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeVenteService;

/**
 * Journée de caisse complète sur une base MySQL VIERGE, avec les services réels.
 * Mêmes prérequis que {@link SchemaMigrationIntegrationTest}. Ne JAMAIS pointer vers Aiven.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@EnabledIfEnvironmentVariable(named = "LADAFURA_TEST_DB_URL", matches = ".+")
@Import({PharmacopeeContexteServiceImpl.class, PharmacopeeQuotaServiceImpl.class, JournalAuditServiceImpl.class,
        PharmacopeeMouvementStockServiceImpl.class, PharmacopeeOperationCaisseServiceImpl.class, PharmacopeeVenteServiceImpl.class,
        PharmacopeeCaisseServiceImpl.class, PharmacopeeDepenseServiceImpl.class, PharmacopeeTresorerieServiceImpl.class})
class CaissesIntegrationTest {

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
    @Autowired private IPharmacopeeCaisseService caisseService;
    @Autowired private IPharmacopeeVenteService venteService;
    @Autowired private IPharmacopeeDepenseService depenseService;
    @Autowired private IPharmacopeeTresorerieService tresorerieService;
    @Autowired private RolePharmacopeeRepository roleRepository;
    @MockitoBean private IPharmacopeeAuthService pharmacopeeAuthService;

    @Test
    @DisplayName("Ouverture, vente en espèces, dépense, clôture avec écart, session suivante, transfert et synthèse")
    void journeeDeCaisse() {
        Utilisateur gerant = new Utilisateur();
        gerant.setNom("Sanogo");
        gerant.setPrenom("Fatou");
        gerant.setEmail("gerant-caisse@test.ml");
        gerant.setRole(Role.PHARMACOPEE);
        em.persist(gerant);
        Pharmacopee officine = em.persist(Pharmacopee.builder().nom("Officine de Gao").statut(StatutPharmacopee.VALIDEE).build());
        MembrePharmacopee membre = em.persist(MembrePharmacopee.builder().utilisateur(gerant).pharmacopee(officine)
                .statut(StatutMembre.ACTIF).estProprietaire(true).build());
        em.persist(AttributionRolePharmacopee.builder().membrePharmacopee(membre)
                .rolePharmacopee(roleRepository.findByCodeAndPharmacopeeIsNull("GERANT").orElseThrow()).build());
        abonnementActif(officine);
        Produit produit = em.persist(Produit.builder().nom("Baume de karité").prix(1500.0).statut(StatutProduit.VALIDE).build());
        DisponibiliteProduit fiche = em.persist(DisponibiliteProduit.builder().pharmacopee(officine).produit(produit).quantiteStock(10).prix(2000.0).build());
        em.flush();
        when(pharmacopeeAuthService.getCurrentUtilisateur()).thenReturn(gerant);

        CaisseResponse caisse = caisseService.creer(EnregistrerCaisseRequest.builder().nom("Comptoir").type(TypeCaisse.PHYSIQUE).build());
        CaisseResponse banque = caisseService.creer(EnregistrerCaisseRequest.builder().nom("Banque").type(TypeCaisse.BANQUE).build());
        em.flush();

        SessionCaisseResponse session = caisseService.ouvrirSession(caisse.getId(), new OuvrirSessionRequest(new BigDecimal("5000"), null));
        em.flush();
        assertThat(caisseService.getCaisse(caisse.getId()).getSessionOuverteId()).isEqualTo(session.getId());

        var vente = venteService.creer(EnregistrerVenteRequest.builder()
                .lignes(List.of(LigneVenteRequest.builder().disponibiliteProduitId(fiche.getId()).quantite(2).build())).build());
        venteService.valider(vente.getId(), ValiderVenteRequest.builder().paiements(List.of(PaiementVenteRequest.builder()
                .montant(new BigDecimal("4000")).methode(MethodePaiement.CASH).caisseId(caisse.getId()).build())).build());
        em.flush();
        depenseService.creer(EnregistrerDepenseRequest.builder().libelle("Transport").categorie(CategorieDepense.TRANSPORT)
                .montant(new BigDecimal("1000")).caisseId(caisse.getId()).methode(MethodePaiement.CASH).build());
        em.flush();
        assertThat(caisseService.getCaisse(caisse.getId()).getSoldeTheorique()).isEqualByComparingTo("8000");

        SessionCaisseResponse cloturee = caisseService.cloturerSession(caisse.getId(), new CloturerSessionRequest(new BigDecimal("7900"), "Erreur de rendu"));
        em.flush();
        assertThat(cloturee.getStatut()).isEqualTo(StatutSessionCaisse.CLOTUREE);
        assertThat(cloturee.getMontantTheorique()).isEqualByComparingTo("8000");
        assertThat(cloturee.getEcart()).isEqualByComparingTo("-100");
        assertThat(caisseService.getOperations(caisse.getId(), PageRequest.of(0, 10)).getContent())
                .extracting(o -> o.getNature()).contains(NatureOperationCaisse.AJUSTEMENT);

        SessionCaisseResponse suivante = caisseService.ouvrirSession(caisse.getId(), new OuvrirSessionRequest());
        em.flush();
        assertThat(suivante.getFondOuverture()).isEqualByComparingTo("7900");

        caisseService.enregistrerOperation(caisse.getId(), new OperationManuelleRequest(NatureOperationCaisse.APPORT, new BigDecimal("100"), "Apport du gérant"));
        caisseService.transferer(new TransfertCaisseRequest(caisse.getId(), banque.getId(), new BigDecimal("3000"), "Dépôt en banque"));
        em.flush();
        assertThat(caisseService.getCaisse(caisse.getId()).getSoldeTheorique()).isEqualByComparingTo("5000");
        assertThat(caisseService.getCaisse(banque.getId()).getSoldeTheorique()).isEqualByComparingTo("3000");

        LocalDate aujourdhui = LocalDate.now();
        SyntheseTresorerieResponse synthese = tresorerieService.getSynthese(aujourdhui, aujourdhui);
        assertThat(synthese.getChiffreAffairesComptoir()).isEqualByComparingTo("4000");
        assertThat(synthese.getChiffreAffairesCommandes()).isEqualByComparingTo("0");
        assertThat(synthese.getComptes()).hasSize(2);
        assertThat(synthese.getDepenses()).singleElement().satisfies(d -> assertThat(d.getMontant()).isEqualByComparingTo("1000"));
        assertThat(depenseService.getDepenses(null, null, PageRequest.of(0, 10)).getTotalElements()).isEqualTo(1);
        assertThat(depenseService.getDepenses(aujourdhui, aujourdhui, PageRequest.of(0, 10)).getTotalElements()).isEqualTo(1);
    }

    private void abonnementActif(Pharmacopee officine) {
        OffreAbonnement gratuite = em.getEntityManager()
                .createQuery("select o from OffreAbonnement o where o.code = 'GRATUITE'", OffreAbonnement.class).getSingleResult();
        LimiteOffre limite = em.getEntityManager()
                .createQuery("select l from LimiteOffre l where l.codeLimite = 'MAX_VENTES_MENSUELLES'", LimiteOffre.class).getSingleResult();
        Abonnement abonnement = em.persist(Abonnement.builder().pharmacopee(officine).offre(gratuite).statut(StatutAbonnement.ACTIF)
                .codeOffreSouscrite(gratuite.getCode()).nomOffreSouscrite(gratuite.getNom())
                .prixSouscrit(BigDecimal.ZERO).deviseSouscrite("XOF").build());
        em.persist(LimiteAbonnement.builder().abonnement(abonnement).limiteOffre(limite)
                .codeLimite(limite.getCodeLimite()).valeurMaximale(50).illimite(false).build());
    }
}
