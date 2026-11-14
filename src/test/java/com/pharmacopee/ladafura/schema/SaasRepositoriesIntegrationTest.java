package com.pharmacopee.ladafura.schema;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDate;
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
import org.springframework.transaction.IllegalTransactionStateException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.pharmacopee.ladafura.Models.Abonnement;
import com.pharmacopee.ladafura.Models.Caisse;
import com.pharmacopee.ladafura.Models.DisponibiliteProduit;
import com.pharmacopee.ladafura.Models.Lot;
import com.pharmacopee.ladafura.Models.MembrePharmacopee;
import com.pharmacopee.ladafura.Models.MouvementStock;
import com.pharmacopee.ladafura.Models.OffreAbonnement;
import com.pharmacopee.ladafura.Models.OperationCaisse;
import com.pharmacopee.ladafura.Models.Pharmacopee;
import com.pharmacopee.ladafura.Models.Produit;
import com.pharmacopee.ladafura.Models.SessionCaisse;
import com.pharmacopee.ladafura.Models.Utilisateur;
import com.pharmacopee.ladafura.Models.Vente;
import com.pharmacopee.ladafura.enums.NatureOperationCaisse;
import com.pharmacopee.ladafura.enums.OrigineAction;
import com.pharmacopee.ladafura.enums.Role;
import com.pharmacopee.ladafura.enums.SensOperation;
import com.pharmacopee.ladafura.enums.StatutAbonnement;
import com.pharmacopee.ladafura.enums.StatutMembre;
import com.pharmacopee.ladafura.enums.StatutPharmacopee;
import com.pharmacopee.ladafura.enums.StatutProduit;
import com.pharmacopee.ladafura.enums.StatutVente;
import com.pharmacopee.ladafura.enums.TypeActionAudit;
import com.pharmacopee.ladafura.enums.TypeCaisse;
import com.pharmacopee.ladafura.enums.TypeMouvementStock;
import com.pharmacopee.ladafura.repository.AbonnementRepository;
import com.pharmacopee.ladafura.repository.CaisseRepository;
import com.pharmacopee.ladafura.repository.DisponibiliteProduitRepository;
import com.pharmacopee.ladafura.repository.LotRepository;
import com.pharmacopee.ladafura.repository.OperationCaisseRepository;
import com.pharmacopee.ladafura.repository.VenteRepository;
import com.pharmacopee.ladafura.services.impl.JournalAuditServiceImpl;
import com.pharmacopee.ladafura.services.impl.PharmacopeeContexteServiceImpl;
import com.pharmacopee.ladafura.services.impl.PharmacopeeMouvementStockServiceImpl;
import com.pharmacopee.ladafura.services.impl.PharmacopeeOperationCaisseServiceImpl;
import com.pharmacopee.ladafura.services.impl.PharmacopeeQuotaServiceImpl;
import com.pharmacopee.ladafura.services.interfaces.IJournalAuditService;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeAuthService;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeMouvementStockService;

/**
 * Vérifie sur une base MySQL VIERGE les requêtes écrites à la main des repositories SaaS.
 * Mêmes prérequis que {@link SchemaMigrationIntegrationTest}. Ne JAMAIS pointer vers Aiven.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@EnabledIfEnvironmentVariable(named = "LADAFURA_TEST_DB_URL", matches = ".+")
@Import({PharmacopeeContexteServiceImpl.class, PharmacopeeQuotaServiceImpl.class, PharmacopeeMouvementStockServiceImpl.class,
        PharmacopeeOperationCaisseServiceImpl.class, JournalAuditServiceImpl.class})
class SaasRepositoriesIntegrationTest {

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
    @Autowired private AbonnementRepository abonnementRepository;
    @Autowired private LotRepository lotRepository;
    @Autowired private DisponibiliteProduitRepository disponibiliteProduitRepository;
    @Autowired private CaisseRepository caisseRepository;
    @Autowired private OperationCaisseRepository operationCaisseRepository;
    @Autowired private VenteRepository venteRepository;
    @Autowired private IPharmacopeeMouvementStockService mouvementStockService;
    @Autowired private IJournalAuditService journalAuditService;
    @MockitoBean private IPharmacopeeAuthService pharmacopeeAuthService;

    @Test
    @DisplayName("Les lots vendables : péremption la plus proche d'abord, sans date en dernier, périmés et vides exclus")
    void lotsVendables() {
        LocalDate aujourdhui = LocalDate.of(2026, 11, 9);
        Pharmacopee officine = pharmacopee();
        DisponibiliteProduit fiche = fiche(officine);
        Lot sansDate = lot(officine, fiche, null, 5);
        Lot lointain = lot(officine, fiche, aujourdhui.plusMonths(6), 5);
        Lot proche = lot(officine, fiche, aujourdhui, 5);
        lot(officine, fiche, aujourdhui.minusDays(1), 5);
        lot(officine, fiche, aujourdhui.plusDays(1), 0);
        em.flush();

        assertThat(lotRepository.findLotsVendablesForUpdate(fiche.getId(), aujourdhui)).containsExactly(proche, lointain, sansDate);
        assertThat(lotRepository.sumQuantiteRestante(fiche.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("La somme des lots vaut zéro pour une fiche sans lot")
    void sommeSansLot() {
        assertThat(lotRepository.sumQuantiteRestante(fiche(pharmacopee()).getId())).isZero();
    }

    @Test
    @DisplayName("Le verrou sur une fiche de stock est borné par la pharmacopée")
    void ficheBorneeParPharmacopee() {
        Pharmacopee officine = pharmacopee();
        DisponibiliteProduit fiche = fiche(officine);
        em.flush();

        assertThat(disponibiliteProduitRepository.findByIdAndPharmacopeeIdForUpdate(fiche.getId(), officine.getId())).isPresent();
        assertThat(disponibiliteProduitRepository.findByIdAndPharmacopeeIdForUpdate(fiche.getId(), pharmacopee().getId())).isEmpty();
    }

    @Test
    @DisplayName("Le solde d'une caisse et d'une session : entrées moins sorties, zéro sans opération")
    void soldes() {
        Pharmacopee officine = pharmacopee();
        Caisse caisse = em.persist(Caisse.builder().pharmacopee(officine).nom("Caisse principale").type(TypeCaisse.PHYSIQUE).build());
        SessionCaisse session = em.persist(SessionCaisse.builder().caisse(caisse).ouvertePar(membre(officine)).build());
        em.flush();
        assertThat(operationCaisseRepository.soldeTheorique(caisse.getId())).isEqualByComparingTo(BigDecimal.ZERO);

        operation(officine, caisse, session, SensOperation.ENTREE, "2500.00");
        operation(officine, caisse, session, SensOperation.SORTIE, "1000.00");
        operation(officine, caisse, null, SensOperation.ENTREE, "300.00");
        em.flush();

        assertThat(operationCaisseRepository.soldeTheorique(caisse.getId())).isEqualByComparingTo("1800.00");
        assertThat(operationCaisseRepository.soldeSession(session.getId())).isEqualByComparingTo("1500.00");
        assertThat(caisseRepository.findByIdAndPharmacopeeIdForUpdate(caisse.getId(), pharmacopee().getId())).isEmpty();
    }

    @Test
    @DisplayName("Le quota mensuel ne compte que les ventes validées de la période")
    void ventesDuMois() {
        Pharmacopee officine = pharmacopee();
        MembrePharmacopee vendeur = membre(officine);
        LocalDateTime debut = LocalDateTime.of(2026, 11, 1, 0, 0);
        LocalDateTime fin = LocalDateTime.of(2026, 11, 30, 23, 59, 59);
        vente(officine, vendeur, "V1", StatutVente.VALIDEE, debut.plusDays(3));
        vente(officine, vendeur, "V2", StatutVente.EN_ATTENTE, debut.plusDays(3));
        vente(officine, vendeur, "V3", StatutVente.VALIDEE, debut.minusDays(1));
        em.flush();

        assertThat(venteRepository.countByPharmacopeeIdAndStatutAndDateVenteBetween(officine.getId(), StatutVente.VALIDEE, debut, fin))
                .isEqualTo(1);
    }

    @Test
    @DisplayName("L'abonnement en vigueur ignore les abonnements expirés")
    void abonnementEnVigueur() {
        Pharmacopee officine = pharmacopee();
        abonnement(officine, StatutAbonnement.EXPIRE);
        Abonnement actif = abonnement(officine, StatutAbonnement.EN_GRACE);
        em.flush();

        assertThat(abonnementRepository.findEnVigueur(officine.getId())).contains(actif);
        assertThat(abonnementRepository.findEnVigueur(pharmacopee().getId())).isEmpty();
    }

    @Test
    @DisplayName("La sortie de stock réelle puise d'abord le lot le plus proche de la péremption")
    void sortieDeStockSurBase() {
        Pharmacopee officine = pharmacopee();
        DisponibiliteProduit fiche = fiche(officine);
        Lot lointain = lot(officine, fiche, LocalDate.now().plusMonths(6), 15);
        Lot proche = lot(officine, fiche, LocalDate.now().plusDays(10), 5);
        em.flush();

        List<MouvementStock> mouvements = mouvementStockService.sortir(officine.getId(), fiche.getId(), 7,
                TypeMouvementStock.SORTIE_VENTE, null, "Vente au comptoir", null);
        em.flush();

        assertThat(mouvements).extracting(MouvementStock::getLot).containsExactly(proche, lointain);
        assertThat(proche.getQuantiteRestante()).isZero();
        assertThat(lointain.getQuantiteRestante()).isEqualTo(13);
        assertThat(fiche.getQuantiteStock()).isEqualTo(13);
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    @DisplayName("Le journal d'audit refuse d'écrire hors d'une transaction")
    void auditExigeUneTransaction() {
        assertThatThrownBy(() -> journalAuditService.tracer(IJournalAuditService.EvenementAudit.builder()
                .action(TypeActionAudit.VENTE_ANNULEE).origine(OrigineAction.SYSTEME).typeEntite("Vente").build()))
                .isInstanceOf(IllegalTransactionStateException.class);
    }

    private int compteur;

    private Pharmacopee pharmacopee() {
        return em.persist(Pharmacopee.builder().nom("Officine " + (++compteur)).statut(StatutPharmacopee.VALIDEE).build());
    }

    private DisponibiliteProduit fiche(Pharmacopee officine) {
        Produit produit = em.persist(Produit.builder().nom("Tisane " + (++compteur)).prix(1500.0).statut(StatutProduit.VALIDE).build());
        return em.persist(DisponibiliteProduit.builder().pharmacopee(officine).produit(produit).quantiteStock(20).build());
    }

    private Lot lot(Pharmacopee officine, DisponibiliteProduit fiche, LocalDate peremption, int quantite) {
        return em.persist(Lot.builder().pharmacopee(officine).disponibiliteProduit(fiche)
                .datePeremption(peremption).quantiteInitiale(quantite).build());
    }

    private MembrePharmacopee membre(Pharmacopee officine) {
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setNom("Traoré");
        utilisateur.setPrenom("Awa");
        utilisateur.setEmail("membre" + (++compteur) + "@test.ml");
        utilisateur.setRole(Role.POPULATION);
        em.persist(utilisateur);
        return em.persist(MembrePharmacopee.builder().utilisateur(utilisateur).pharmacopee(officine)
                .statut(StatutMembre.ACTIF).estProprietaire(false).build());
    }

    private void operation(Pharmacopee officine, Caisse caisse, SessionCaisse session, SensOperation sens, String montant) {
        em.persist(OperationCaisse.builder().pharmacopee(officine).caisse(caisse).session(session)
                .sens(sens).nature(NatureOperationCaisse.APPORT).montant(new BigDecimal(montant)).build());
    }

    private void vente(Pharmacopee officine, MembrePharmacopee vendeur, String numero, StatutVente statut, LocalDateTime date) {
        em.persist(Vente.builder().pharmacopee(officine).vendeur(vendeur).numeroVente(numero).statut(statut).dateVente(date).build());
    }

    private Abonnement abonnement(Pharmacopee officine, StatutAbonnement statut) {
        OffreAbonnement gratuite = em.getEntityManager()
                .createQuery("select o from OffreAbonnement o where o.code = 'GRATUITE'", OffreAbonnement.class)
                .getSingleResult();
        return em.persist(Abonnement.builder().pharmacopee(officine).offre(gratuite).statut(statut)
                .codeOffreSouscrite(gratuite.getCode()).nomOffreSouscrite(gratuite.getNom())
                .prixSouscrit(gratuite.getPrix()).deviseSouscrite(gratuite.getDevise()).build());
    }
}
