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
import com.pharmacopee.ladafura.dto.pharmacopee.client.ClientResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.client.EnregistrerClientRequest;
import com.pharmacopee.ladafura.dto.pharmacopee.vente.EnregistrerVenteRequest;
import com.pharmacopee.ladafura.dto.pharmacopee.vente.LigneVenteRequest;
import com.pharmacopee.ladafura.dto.pharmacopee.vente.PaiementVenteRequest;
import com.pharmacopee.ladafura.dto.pharmacopee.vente.ValiderVenteRequest;
import com.pharmacopee.ladafura.dto.pharmacopee.vente.VenteResponse;
import com.pharmacopee.ladafura.enums.MethodePaiement;
import com.pharmacopee.ladafura.enums.Role;
import com.pharmacopee.ladafura.enums.StatutAbonnement;
import com.pharmacopee.ladafura.enums.StatutMembre;
import com.pharmacopee.ladafura.enums.StatutPharmacopee;
import com.pharmacopee.ladafura.enums.StatutProduit;
import com.pharmacopee.ladafura.enums.StatutVente;
import com.pharmacopee.ladafura.enums.TypeCaisse;
import com.pharmacopee.ladafura.repository.OperationCaisseRepository;
import com.pharmacopee.ladafura.repository.RolePharmacopeeRepository;
import com.pharmacopee.ladafura.services.impl.JournalAuditServiceImpl;
import com.pharmacopee.ladafura.services.impl.PharmacopeeClientServiceImpl;
import com.pharmacopee.ladafura.services.impl.PharmacopeeContexteServiceImpl;
import com.pharmacopee.ladafura.services.impl.PharmacopeeMouvementStockServiceImpl;
import com.pharmacopee.ladafura.services.impl.PharmacopeeOperationCaisseServiceImpl;
import com.pharmacopee.ladafura.services.impl.PharmacopeeQuotaServiceImpl;
import com.pharmacopee.ladafura.services.impl.PharmacopeeVenteServiceImpl;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeAuthService;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeClientService;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeVenteService;

/**
 * Parcours complet d'une vente au comptoir sur une base MySQL VIERGE, avec les services réels.
 * Mêmes prérequis que {@link SchemaMigrationIntegrationTest}. Ne JAMAIS pointer vers Aiven.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@EnabledIfEnvironmentVariable(named = "LADAFURA_TEST_DB_URL", matches = ".+")
@Import({PharmacopeeContexteServiceImpl.class, PharmacopeeQuotaServiceImpl.class, JournalAuditServiceImpl.class,
        PharmacopeeMouvementStockServiceImpl.class, PharmacopeeOperationCaisseServiceImpl.class,
        PharmacopeeClientServiceImpl.class, PharmacopeeVenteServiceImpl.class})
class VentesIntegrationTest {

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
    @Autowired private IPharmacopeeClientService clientService;
    @Autowired private IPharmacopeeVenteService venteService;
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
    @DisplayName("Vente en attente, validation par lots, crédit client, paiement ultérieur, annulation et retour du stock")
    void parcoursComplet() {
        Utilisateur gerant = new Utilisateur();
        gerant.setNom("Coulibaly");
        gerant.setPrenom("Mariam");
        gerant.setEmail("gerant-ventes@test.ml");
        gerant.setRole(Role.PHARMACOPEE);
        em.persist(gerant);
        Pharmacopee officine = em.persist(Pharmacopee.builder().nom("Officine de Kita").statut(StatutPharmacopee.VALIDEE).build());
        MembrePharmacopee membre = em.persist(MembrePharmacopee.builder().utilisateur(gerant).pharmacopee(officine)
                .statut(StatutMembre.ACTIF).estProprietaire(true).build());
        em.persist(AttributionRolePharmacopee.builder().membrePharmacopee(membre)
                .rolePharmacopee(roleRepository.findByCodeAndPharmacopeeIsNull("GERANT").orElseThrow()).build());
        abonnementActif(officine);
        Produit produit = em.persist(Produit.builder().nom("Sirop de moringa").prix(1500.0).statut(StatutProduit.VALIDE).build());
        DisponibiliteProduit fiche = em.persist(DisponibiliteProduit.builder().pharmacopee(officine).produit(produit)
                .disponible(false).quantiteStock(10).prix(2000.0).build());
        Lot proche = em.persist(Lot.builder().pharmacopee(officine).disponibiliteProduit(fiche).datePeremption(LocalDate.now().plusDays(30)).quantiteInitiale(3).build());
        Lot lointain = em.persist(Lot.builder().pharmacopee(officine).disponibiliteProduit(fiche).datePeremption(LocalDate.now().plusYears(1)).quantiteInitiale(7).build());
        Caisse compte = em.persist(Caisse.builder().pharmacopee(officine).nom("Mobile Money").type(TypeCaisse.MOBILE_MONEY).build());
        em.flush();
        when(pharmacopeeAuthService.getCurrentUtilisateur()).thenReturn(gerant);

        ClientResponse client = clientService.creer(EnregistrerClientRequest.builder().nom("Sidibé").prenom("Oumar").telephone("+223 70 11 22 33").build());
        em.flush();
        assertThat(clientService.getClients("sidi", PageRequest.of(0, 10)).getTotalElements()).isEqualTo(1);
        assertThat(clientService.getClients(null, PageRequest.of(0, 10)).getTotalElements()).isEqualTo(1);

        VenteResponse enAttente = venteService.creer(EnregistrerVenteRequest.builder().clientId(client.getId())
                .lignes(List.of(LigneVenteRequest.builder().disponibiliteProduitId(fiche.getId()).quantite(5).build())).build());
        em.flush();
        assertThat(enAttente.getStatut()).isEqualTo(StatutVente.EN_ATTENTE);
        assertThat(fiche.getQuantiteStock()).isEqualTo(10);

        // Validation avec un acompte : 10 000 F dus, 4 000 F payés. La fiche non disponible se vend au comptoir.
        VenteResponse validee = venteService.valider(enAttente.getId(), ValiderVenteRequest.builder()
                .paiements(List.of(paiement("4000", compte))).build());
        em.flush();
        assertThat(validee.getStatut()).isEqualTo(StatutVente.VALIDEE);
        assertThat(validee.getResteAPayer()).isEqualByComparingTo("6000");
        assertThat(fiche.getQuantiteStock()).isEqualTo(5);
        assertThat(proche.getQuantiteRestante()).isZero();
        assertThat(lointain.getQuantiteRestante()).isEqualTo(5);
        assertThat(clientService.getClient(client.getId()).getEncours()).isEqualByComparingTo("6000");

        venteService.enregistrerPaiement(enAttente.getId(), paiement("6000", compte));
        em.flush();
        assertThat(clientService.getClient(client.getId()).getEncours()).isEqualByComparingTo("0");
        assertThat(operationCaisseRepository.soldeTheorique(compte.getId())).isEqualByComparingTo("10000");
        assertThat(venteService.getVentes(StatutVente.VALIDEE, client.getId(), PageRequest.of(0, 10)).getContent())
                .singleElement().satisfies(v -> assertThat(v.getMontantPaye()).isEqualByComparingTo("10000"));

        VenteResponse annulee = venteService.annuler(enAttente.getId(), "Erreur de caisse");
        em.flush();
        assertThat(annulee.getStatut()).isEqualTo(StatutVente.ANNULEE);
        assertThat(fiche.getQuantiteStock()).isEqualTo(10);
        assertThat(proche.getQuantiteRestante()).isEqualTo(3);
        assertThat(lointain.getQuantiteRestante()).isEqualTo(7);
        assertThat(operationCaisseRepository.soldeTheorique(compte.getId())).isEqualByComparingTo("0");
        assertThat(clientService.getClient(client.getId()).getEncours()).isEqualByComparingTo("0");
    }

    private static PaiementVenteRequest paiement(String montant, Caisse compte) {
        return PaiementVenteRequest.builder().montant(new BigDecimal(montant)).methode(MethodePaiement.MOBILE_MONEY).caisseId(compte.getId()).build();
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
