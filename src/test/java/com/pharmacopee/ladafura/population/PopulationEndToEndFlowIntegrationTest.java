package com.pharmacopee.ladafura.population;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.pharmacopee.ladafura.Models.Avis;
import com.pharmacopee.ladafura.Models.Commande;
import com.pharmacopee.ladafura.Models.DisponibiliteProduit;
import com.pharmacopee.ladafura.Models.Favori;
import com.pharmacopee.ladafura.Models.LigneCommande;
import com.pharmacopee.ladafura.Models.LignePanier;
import com.pharmacopee.ladafura.Models.ModeRetrait;
import com.pharmacopee.ladafura.Models.Panier;
import com.pharmacopee.ladafura.Models.Paiement;
import com.pharmacopee.ladafura.Models.Pharmacopee;
import com.pharmacopee.ladafura.Models.Produit;
import com.pharmacopee.ladafura.Models.Utilisateur;
import com.pharmacopee.ladafura.dto.population.avis.PopulationCreateAvisRequest;
import com.pharmacopee.ladafura.dto.population.commande.PopulationCommandeDetailResponse;
import com.pharmacopee.ladafura.dto.population.commande.PopulationCreateCommandeRequest;
import com.pharmacopee.ladafura.dto.population.favori.PopulationToggleFavoriRequest;
import com.pharmacopee.ladafura.dto.population.favori.PopulationToggleFavoriResponse;
import com.pharmacopee.ladafura.dto.population.historique.PopulationDepensesSyntheseResponse;
import com.pharmacopee.ladafura.dto.population.historique.PopulationJournalActiviteItem;
import com.pharmacopee.ladafura.dto.population.paiement.PopulationPaiementResponse;
import com.pharmacopee.ladafura.dto.population.paiement.PopulationProcessPaiementRequest;
import com.pharmacopee.ladafura.dto.population.panier.PopulationAddToCartRequest;
import com.pharmacopee.ladafura.dto.population.panier.PopulationPanierResponse;
import com.pharmacopee.ladafura.enums.MethodePaiement;
import com.pharmacopee.ladafura.enums.Role;
import com.pharmacopee.ladafura.enums.StatutAvis;
import com.pharmacopee.ladafura.enums.StatutCommande;
import com.pharmacopee.ladafura.enums.StatutPaiement;
import com.pharmacopee.ladafura.enums.StatutPharmacopee;
import com.pharmacopee.ladafura.enums.StatutProduit;
import com.pharmacopee.ladafura.enums.StatutUtilisateur;
import com.pharmacopee.ladafura.enums.TypeEvenementHistorique;
import com.pharmacopee.ladafura.enums.TypeFavori;
import com.pharmacopee.ladafura.enums.TypeModeRetrait;
import com.pharmacopee.ladafura.mappers.PopulationAvisMapper;
import com.pharmacopee.ladafura.mappers.PopulationCommandeMapper;
import com.pharmacopee.ladafura.mappers.PopulationFavoriMapper;
import com.pharmacopee.ladafura.mappers.PopulationHistoriqueMapper;
import com.pharmacopee.ladafura.mappers.PopulationPaiementMapper;
import com.pharmacopee.ladafura.mappers.PopulationPanierMapper;
import com.pharmacopee.ladafura.repository.AvisRepository;
import com.pharmacopee.ladafura.repository.CommandeRepository;
import com.pharmacopee.ladafura.repository.DisponibiliteProduitRepository;
import com.pharmacopee.ladafura.repository.FavoriRepository;
import com.pharmacopee.ladafura.repository.LigneCommandeRepository;
import com.pharmacopee.ladafura.repository.LignePanierRepository;
import com.pharmacopee.ladafura.repository.ModeRetraitRepository;
import com.pharmacopee.ladafura.repository.NotificationRepository;
import com.pharmacopee.ladafura.repository.PaiementRepository;
import com.pharmacopee.ladafura.repository.PanierRepository;
import com.pharmacopee.ladafura.repository.PharmacopeeRepository;
import com.pharmacopee.ladafura.repository.PlanteRepository;
import com.pharmacopee.ladafura.repository.ProduitRepository;
import com.pharmacopee.ladafura.services.impl.PopulationAvisServiceImpl;
import com.pharmacopee.ladafura.services.impl.PopulationCommandeServiceImpl;
import com.pharmacopee.ladafura.services.impl.PopulationFavoriServiceImpl;
import com.pharmacopee.ladafura.services.impl.PopulationHistoriqueServiceImpl;
import com.pharmacopee.ladafura.services.impl.PopulationPaiementServiceImpl;
import com.pharmacopee.ladafura.services.impl.PopulationPanierServiceImpl;
import com.pharmacopee.ladafura.services.interfaces.IPopulationAuthService;

/**
 * Test transversal d'intégration de l'acteur POPULATION (Étape 16).
 * Valide le cycle de vie complet de bout en bout :
 * Panier -> Commande -> Livraison -> Paiement -> Avis vérifié -> Favoris -> Traçabilité & Historique.
 */
@ExtendWith(MockitoExtension.class)
class PopulationEndToEndFlowIntegrationTest {

    // Repositories
    @Mock private PanierRepository panierRepository;
    @Mock private LignePanierRepository lignePanierRepository;
    @Mock private CommandeRepository commandeRepository;
    @Mock private LigneCommandeRepository ligneCommandeRepository;
    @Mock private DisponibiliteProduitRepository dispoRepo;
    @Mock private ModeRetraitRepository modeRetraitRepository;
    @Mock private PharmacopeeRepository pharmacopeeRepository;
    @Mock private ProduitRepository produitRepository;
    @Mock private PlanteRepository planteRepository;
    @Mock private PaiementRepository paiementRepository;
    @Mock private AvisRepository avisRepository;
    @Mock private FavoriRepository favoriRepository;
    @Mock private NotificationRepository notificationRepository;
    @Mock private IPopulationAuthService authService;

    // Mappers
    @Spy private PopulationPanierMapper panierMapper = new PopulationPanierMapper();
    @Spy private PopulationCommandeMapper commandeMapper = new PopulationCommandeMapper();
    @Spy private PopulationPaiementMapper paiementMapper = new PopulationPaiementMapper();
    @Spy private PopulationAvisMapper avisMapper = new PopulationAvisMapper();
    @Mock private PopulationFavoriMapper favoriMapper;
    @Spy private PopulationHistoriqueMapper historiqueMapper = new PopulationHistoriqueMapper();

    // Services
    private PopulationPanierServiceImpl panierService;
    private PopulationCommandeServiceImpl commandeService;
    private PopulationPaiementServiceImpl paiementService;
    private PopulationAvisServiceImpl avisService;
    private PopulationFavoriServiceImpl favoriService;
    private PopulationHistoriqueServiceImpl historiqueService;

    // Fixtures
    private Utilisateur citoyen;
    private Pharmacopee pharmacopee;
    private Produit produit;
    private DisponibiliteProduit disponibilite;
    private ModeRetrait modeLivraison;
    private Panier panier;

    @BeforeEach
    void setUp() {
        citoyen = new Utilisateur();
        citoyen.setId(100L);
        citoyen.setEmail("citoyen.bamako@ladafura.ml");
        citoyen.setPrenom("Amadou");
        citoyen.setNom("Traoré");
        citoyen.setRole(Role.POPULATION);
        citoyen.setStatut(StatutUtilisateur.ACTIF);

        pharmacopee = Pharmacopee.builder()
                .id(10L)
                .nom("Pharmacie Traditionnelle de Bamako-Coura")
                .statut(StatutPharmacopee.VALIDEE)
                .telephone("+223 70 00 11 22")
                .build();

        produit = Produit.builder()
                .id(200L)
                .nom("Sirop Kinkeliba Fortifié")
                .forme("Sirop 250ml")
                .prix(3000.0)
                .statut(StatutProduit.VALIDE)
                .build();

        disponibilite = DisponibiliteProduit.builder()
                .id(300L)
                .produit(produit)
                .pharmacopee(pharmacopee)
                .prix(3000.0)
                .quantiteStock(50)
                .disponible(true)
                .build();

        modeLivraison = ModeRetrait.builder()
                .id(400L)
                .pharmacopee(pharmacopee)
                .type(TypeModeRetrait.LIVRAISON)
                .frais(1500.0)
                .actif(true)
                .build();

        panier = Panier.builder()
                .id(500L)
                .utilisateur(citoyen)
                .lignes(new ArrayList<>())
                .build();

        // Initialisation manuelle des services
        panierService = new PopulationPanierServiceImpl(
                authService, panierRepository, lignePanierRepository, produitRepository, panierMapper);

        commandeService = new PopulationCommandeServiceImpl(
                authService, commandeRepository, panierRepository, lignePanierRepository,
                pharmacopeeRepository, modeRetraitRepository, dispoRepo, commandeMapper);

        paiementService = new PopulationPaiementServiceImpl(
                authService, paiementRepository, commandeRepository, paiementMapper);

        avisService = new PopulationAvisServiceImpl(
                authService, avisRepository, produitRepository, ligneCommandeRepository, avisMapper);

        favoriService = new PopulationFavoriServiceImpl(
                favoriRepository, planteRepository, produitRepository, pharmacopeeRepository, authService, favoriMapper);

        historiqueService = new PopulationHistoriqueServiceImpl(
                commandeRepository, paiementRepository, ligneCommandeRepository,
                avisRepository, favoriRepository, notificationRepository, authService, historiqueMapper);
    }

    @Test
    @DisplayName("Cycle E2E Complet : Panier -> Commande -> Paiement Mobile Money -> Avis Post-Achat -> Favoris -> Historique")
    void testEndToEndCitizenFullLifecycle() {
        when(authService.getCurrentPopulationUser()).thenReturn(citoyen);

        // --- 1. AJOUT AU PANIER ---
        when(produitRepository.findByIdAndStatut(200L, StatutProduit.VALIDE)).thenReturn(Optional.of(produit));
        when(panierRepository.findByUtilisateurId(100L)).thenReturn(Optional.of(panier));
        when(lignePanierRepository.findByPanierIdAndProduitId(500L, 200L)).thenReturn(Optional.empty());

        PopulationAddToCartRequest addReq = PopulationAddToCartRequest.builder()
                .produitId(200L)
                .quantite(2)
                .build();

        PopulationPanierResponse panierResp = panierService.ajouterProduitAuPanier(addReq);
        assertThat(panierResp).isNotNull();
        assertThat(panierResp.getLignes()).hasSize(1);
        assertThat(panierResp.getNombreArticles()).isEqualTo(2);

        // --- 2. VALIDATION & CRÉATION DE LA COMMANDE ---
        when(pharmacopeeRepository.findById(10L)).thenReturn(Optional.of(pharmacopee));
        when(modeRetraitRepository.findById(400L)).thenReturn(Optional.of(modeLivraison));
        when(dispoRepo.findByPharmacopeeIdAndProduitId(10L, 200L)).thenReturn(Optional.of(disponibilite));

        Commande nouvelleCommande = Commande.builder()
                .id(600L)
                .numero("CMD-2026-BKO-001")
                .utilisateur(citoyen)
                .pharmacopee(pharmacopee)
                .statut(StatutCommande.EN_ATTENTE)
                .modeRetrait(modeLivraison)
                .totalProduit(6000.0)
                .montantLivraison(1500.0)
                .montantTotal(7500.0)
                .dateCommande(LocalDateTime.now())
                .lignes(new ArrayList<>())
                .build();

        LigneCommande lc = LigneCommande.builder()
                .id(700L)
                .commande(nouvelleCommande)
                .produit(produit)
                .quantite(2)
                .prixUnitaire(3000.0)
                .sousTotal(6000.0)
                .build();
        nouvelleCommande.getLignes().add(lc);

        when(commandeRepository.save(any(Commande.class))).thenReturn(nouvelleCommande);

        PopulationCreateCommandeRequest cmdReq = PopulationCreateCommandeRequest.builder()
                .pharmacopeeId(10L)
                .modeRetraitId(400L)
                .adresseLivraison("Bamako-Coura, Rue 14")
                .notes("Appeler en arrivant devant la pharmacie")
                .build();

        PopulationCommandeDetailResponse cmdResp = commandeService.passerCommande(cmdReq);
        assertThat(cmdResp).isNotNull();
        assertThat(cmdResp.getNumero()).isEqualTo("CMD-2026-BKO-001");
        assertThat(cmdResp.getMontantTotal()).isEqualTo(7500.0);
        assertThat(cmdResp.getStatut()).isEqualTo(StatutCommande.EN_ATTENTE);

        // --- 3. PAIEMENT DE LA COMMANDE (Mobile Money) ---
        when(commandeRepository.findByIdAndUtilisateurId(600L, 100L)).thenReturn(Optional.of(nouvelleCommande));

        Paiement paiementEffectue = Paiement.builder()
                .id(800L)
                .commande(nouvelleCommande)
                .reference("PAY-OM-994827")
                .montant(7500.0)
                .methode(MethodePaiement.MOBILE_MONEY)
                .statut(StatutPaiement.REUSSI)
                .datePaiement(LocalDateTime.now())
                .build();

        when(paiementRepository.save(any(Paiement.class))).thenReturn(paiementEffectue);

        PopulationProcessPaiementRequest payReq = PopulationProcessPaiementRequest.builder()
                .commandeId(600L)
                .methode(MethodePaiement.MOBILE_MONEY)
                .telephoneMobileMoney("+223 70 12 34 56")
                .simulerSucces(true)
                .build();

        PopulationPaiementResponse payResp = paiementService.payerCommande(payReq);
        assertThat(payResp).isNotNull();
        assertThat(payResp.getStatut()).isEqualTo(StatutPaiement.REUSSI);
        assertThat(payResp.getMontant()).isEqualTo(7500.0);
        assertThat(nouvelleCommande.getStatut()).isEqualTo(StatutCommande.CONFIRMEE);

        // --- 4. TRANSITION VERS COMMANDE LIVRÉE ---
        nouvelleCommande.setStatut(StatutCommande.LIVREE);

        // --- 5. AVIS POST-ACHAT VÉRIFIÉ SUR LE PRODUIT ---
        when(produitRepository.findById(200L)).thenReturn(Optional.of(produit));
        when(ligneCommandeRepository.hasUserPurchasedAndReceivedProduct(100L, 200L)).thenReturn(true);
        when(avisRepository.existsByUtilisateurIdAndProduitId(100L, 200L)).thenReturn(false);

        Avis nouvelAvis = Avis.builder()
                .id(900L)
                .utilisateur(citoyen)
                .produit(produit)
                .note(5)
                .commentaire("Remède traditionnel très puissant contre la toux et les fièvres saisonnières.")
                .statut(StatutAvis.EN_ATTENTE)
                .dateAvis(LocalDateTime.now())
                .build();

        when(avisRepository.save(any(Avis.class))).thenReturn(nouvelAvis);

        PopulationCreateAvisRequest avisReq = PopulationCreateAvisRequest.builder()
                .produitId(200L)
                .note(5)
                .commentaire("Remède traditionnel très puissant contre la toux et les fièvres saisonnières.")
                .build();

        var avisResp = avisService.creerAvis(avisReq);
        assertThat(avisResp).isNotNull();
        assertThat(avisResp.getNote()).isEqualTo(5);
        assertThat(avisResp.getProduitId()).isEqualTo(200L);

        // --- 6. GESTION DES FAVORIS ---
        when(favoriRepository.findByUtilisateurIdAndProduitId(100L, 200L)).thenReturn(Optional.empty());
        Favori fav = Favori.builder()
                .id(1000L)
                .utilisateur(citoyen)
                .produit(produit)
                .dateAjout(LocalDateTime.now())
                .build();
        when(favoriRepository.save(any(Favori.class))).thenReturn(fav);
        when(favoriMapper.toToggleResponse(any(), any(), anyBoolean(), any(), anyString()))
                .thenReturn(PopulationToggleFavoriResponse.builder().favori(true).message("Produit ajouté aux favoris").build());

        PopulationToggleFavoriResponse favResp = favoriService.toggleFavori(
                PopulationToggleFavoriRequest.builder().type(TypeFavori.PRODUIT).cibleId(200L).build());
        assertThat(favResp.isFavori()).isTrue();
        assertThat(favResp.getMessage()).contains("ajouté");

        // --- 7. TRAÇABILITÉ & JOURNAL D'ACTIVITÉ HISTORIQUE ---
        when(commandeRepository.findByUtilisateurId(100L)).thenReturn(List.of(nouvelleCommande));
        when(paiementRepository.findByCommandeUtilisateurId(100L)).thenReturn(List.of(paiementEffectue));
        when(avisRepository.findByUtilisateurId(100L)).thenReturn(List.of(nouvelAvis));
        when(favoriRepository.findByUtilisateurId(100L)).thenReturn(List.of(fav));
        when(notificationRepository.findByUtilisateurId(eq(100L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        Page<PopulationJournalActiviteItem> journal =
                historiqueService.getJournalActivite(null, null, null, PageRequest.of(0, 10));

        assertThat(journal.getContent()).hasSize(4); // Commande, Paiement, Avis, Favori
        assertThat(journal.getContent())
                .extracting(PopulationJournalActiviteItem::getType)
                .contains(
                        TypeEvenementHistorique.COMMANDE,
                        TypeEvenementHistorique.PAIEMENT,
                        TypeEvenementHistorique.AVIS,
                        TypeEvenementHistorique.FAVORI
                );

        // --- 8. SYNTHÈSE ANALYTIQUE DES DÉPENSES ---
        when(paiementRepository.findByCommandeUtilisateurIdAndStatut(100L, StatutPaiement.REUSSI))
                .thenReturn(List.of(paiementEffectue));

        PopulationDepensesSyntheseResponse synthese = historiqueService.getSyntheseDepenses();
        assertThat(synthese.getMontantTotalDepense()).isEqualTo(7500.0);
        assertThat(synthese.getNombreTotalTransactions()).isEqualTo(1);
        assertThat(synthese.getPanierMoyen()).isEqualTo(7500.0);
        assertThat(synthese.getDepensesParMethode()).hasSize(1);
        assertThat(synthese.getDepensesParMethode().get(0).getMethode()).isEqualTo(MethodePaiement.MOBILE_MONEY);
    }
}
