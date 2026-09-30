package com.pharmacopee.ladafura.population;

import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import com.pharmacopee.ladafura.Models.Commande;
import com.pharmacopee.ladafura.Models.DisponibiliteProduit;
import com.pharmacopee.ladafura.Models.LigneCommande;
import com.pharmacopee.ladafura.Models.LignePanier;
import com.pharmacopee.ladafura.Models.ModeRetrait;
import com.pharmacopee.ladafura.Models.Panier;
import com.pharmacopee.ladafura.Models.Pharmacopee;
import com.pharmacopee.ladafura.Models.Produit;
import com.pharmacopee.ladafura.Models.Utilisateur;
import com.pharmacopee.ladafura.dto.population.avis.PopulationCreateAvisRequest;
import com.pharmacopee.ladafura.dto.population.commande.PopulationCreateCommandeRequest;
import com.pharmacopee.ladafura.dto.population.paiement.PopulationProcessPaiementRequest;
import com.pharmacopee.ladafura.enums.MethodePaiement;
import com.pharmacopee.ladafura.enums.Role;
import com.pharmacopee.ladafura.enums.StatutCommande;
import com.pharmacopee.ladafura.enums.StatutPharmacopee;
import com.pharmacopee.ladafura.enums.StatutProduit;
import com.pharmacopee.ladafura.enums.StatutUtilisateur;
import com.pharmacopee.ladafura.enums.TypeModeRetrait;
import com.pharmacopee.ladafura.exceptions.BadRequestException;
import com.pharmacopee.ladafura.exceptions.ResourceNotFoundException;
import com.pharmacopee.ladafura.mappers.PopulationAvisMapper;
import com.pharmacopee.ladafura.mappers.PopulationCommandeMapper;
import com.pharmacopee.ladafura.mappers.PopulationPaiementMapper;
import com.pharmacopee.ladafura.mappers.PopulationPanierMapper;
import com.pharmacopee.ladafura.repository.AvisRepository;
import com.pharmacopee.ladafura.repository.CommandeRepository;
import com.pharmacopee.ladafura.repository.DisponibiliteProduitRepository;
import com.pharmacopee.ladafura.repository.LigneCommandeRepository;
import com.pharmacopee.ladafura.repository.LignePanierRepository;
import com.pharmacopee.ladafura.repository.ModeRetraitRepository;
import com.pharmacopee.ladafura.repository.PaiementRepository;
import com.pharmacopee.ladafura.repository.PanierRepository;
import com.pharmacopee.ladafura.repository.PharmacopeeRepository;
import com.pharmacopee.ladafura.repository.ProduitRepository;
import com.pharmacopee.ladafura.services.impl.PopulationAvisServiceImpl;
import com.pharmacopee.ladafura.services.impl.PopulationCommandeServiceImpl;
import com.pharmacopee.ladafura.services.impl.PopulationPaiementServiceImpl;
import com.pharmacopee.ladafura.services.impl.PopulationPanierServiceImpl;
import com.pharmacopee.ladafura.services.interfaces.IPopulationAuthService;

/**
 * Tests de validation des règles métier, étanchéité des données et sécurité (Étape 16).
 */
@ExtendWith(MockitoExtension.class)
class PopulationSecurityAndRulesValidationTest {

    @Mock private PanierRepository panierRepository;
    @Mock private LignePanierRepository lignePanierRepository;
    @Mock private CommandeRepository commandeRepository;
    @Mock private LigneCommandeRepository ligneCommandeRepository;
    @Mock private DisponibiliteProduitRepository dispoRepo;
    @Mock private ModeRetraitRepository modeRetraitRepository;
    @Mock private PharmacopeeRepository pharmacopeeRepository;
    @Mock private ProduitRepository produitRepository;
    @Mock private PaiementRepository paiementRepository;
    @Mock private AvisRepository avisRepository;
    @Mock private IPopulationAuthService authService;

    @Spy private PopulationPanierMapper panierMapper = new PopulationPanierMapper();
    @Spy private PopulationCommandeMapper commandeMapper = new PopulationCommandeMapper();
    @Spy private PopulationPaiementMapper paiementMapper = new PopulationPaiementMapper();
    @Spy private PopulationAvisMapper avisMapper = new PopulationAvisMapper();

    private PopulationPanierServiceImpl panierService;
    private PopulationCommandeServiceImpl commandeService;
    private PopulationPaiementServiceImpl paiementService;
    private PopulationAvisServiceImpl avisService;

    private Utilisateur citoyenA;
    private Utilisateur citoyenB;
    private Pharmacopee pharmacopee;
    private Produit produit;
    private DisponibiliteProduit disponibilite;
    private ModeRetrait modeRetrait;

    @BeforeEach
    void setUp() {
        citoyenA = new Utilisateur();
        citoyenA.setId(10L);
        citoyenA.setEmail("citoyenA@ladafura.ml");
        citoyenA.setRole(Role.POPULATION);
        citoyenA.setStatut(StatutUtilisateur.ACTIF);

        citoyenB = new Utilisateur();
        citoyenB.setId(20L);
        citoyenB.setEmail("citoyenB@ladafura.ml");
        citoyenB.setRole(Role.POPULATION);
        citoyenB.setStatut(StatutUtilisateur.ACTIF);

        pharmacopee = Pharmacopee.builder()
                .id(1L)
                .nom("Herboristerie Centrale")
                .statut(StatutPharmacopee.VALIDEE)
                .build();

        produit = Produit.builder()
                .id(100L)
                .nom("Poudre Artemisia")
                .prix(2000.0)
                .statut(StatutProduit.VALIDE)
                .build();

        disponibilite = DisponibiliteProduit.builder()
                .id(1L)
                .produit(produit)
                .pharmacopee(pharmacopee)
                .prix(2000.0)
                .quantiteStock(5)
                .disponible(true)
                .build();

        modeRetrait = ModeRetrait.builder()
                .id(1L)
                .pharmacopee(pharmacopee)
                .type(TypeModeRetrait.PICKUP)
                .frais(0.0)
                .actif(true)
                .build();

        panierService = new PopulationPanierServiceImpl(
                authService, panierRepository, lignePanierRepository, produitRepository, panierMapper);

        commandeService = new PopulationCommandeServiceImpl(
                authService, commandeRepository, panierRepository, lignePanierRepository,
                pharmacopeeRepository, modeRetraitRepository, dispoRepo, commandeMapper);

        paiementService = new PopulationPaiementServiceImpl(
                authService, paiementRepository, commandeRepository, paiementMapper);

        avisService = new PopulationAvisServiceImpl(
                authService, avisRepository, produitRepository, ligneCommandeRepository, avisMapper);
    }

    @Test
    @DisplayName("Commande - Refus si la quantité demandée dépasse le stock de l'officine")
    void commande_rejectWhenQuantityExceedsStock() {
        when(authService.getCurrentPopulationUser()).thenReturn(citoyenA);

        Panier panier = Panier.builder()
                .id(1L)
                .utilisateur(citoyenA)
                .lignes(new ArrayList<>())
                .build();

        LignePanier lp = LignePanier.builder()
                .id(10L)
                .panier(panier)
                .produit(produit)
                .quantite(10) // demande 10 alors qu'il n'y a que 5
                .prixUnitaire(2000.0)
                .build();
        panier.getLignes().add(lp);

        when(panierRepository.findByUtilisateurId(10L)).thenReturn(Optional.of(panier));
        when(pharmacopeeRepository.findById(1L)).thenReturn(Optional.of(pharmacopee));
        when(modeRetraitRepository.findById(1L)).thenReturn(Optional.of(modeRetrait));
        when(dispoRepo.findByPharmacopeeIdAndProduitId(1L, 100L)).thenReturn(Optional.of(disponibilite)); // quantiteStock = 5

        PopulationCreateCommandeRequest request = PopulationCreateCommandeRequest.builder()
                .pharmacopeeId(1L)
                .modeRetraitId(1L)
                .build();

        assertThatThrownBy(() -> commandeService.passerCommande(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Stock insuffisant");
    }

    @Test
    @DisplayName("Commande - Refus de commande sur un panier vide")
    void commande_rejectWhenCartIsEmpty() {
        when(authService.getCurrentPopulationUser()).thenReturn(citoyenA);
        Panier panierVide = Panier.builder().id(1L).utilisateur(citoyenA).lignes(new ArrayList<>()).build();
        when(panierRepository.findByUtilisateurId(10L)).thenReturn(Optional.of(panierVide));

        PopulationCreateCommandeRequest request = PopulationCreateCommandeRequest.builder()
                .pharmacopeeId(1L)
                .modeRetraitId(1L)
                .build();

        assertThatThrownBy(() -> commandeService.passerCommande(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Votre panier est vide");
    }

    @Test
    @DisplayName("Sécurité Paiement - Refus si un utilisateur tente de payer la commande d'un tiers")
    void paiement_rejectWhenOrderBelongsToAnotherUser() {
        when(authService.getCurrentPopulationUser()).thenReturn(citoyenB); // Citoyen B connecté

        // Commande appartient à Citoyen A (10L), donc introuvable pour B (20L)
        when(commandeRepository.findByIdAndUtilisateurId(999L, 20L)).thenReturn(Optional.empty());

        PopulationProcessPaiementRequest req = PopulationProcessPaiementRequest.builder()
                .commandeId(999L)
                .methode(MethodePaiement.MOBILE_MONEY)
                .telephoneMobileMoney("+223 70 00 00 00")
                .build();

        assertThatThrownBy(() -> paiementService.payerCommande(req))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Commande");
    }

    @Test
    @DisplayName("Avis Client - Refus strict si l'utilisateur n'a jamais acheté ni reçu le produit")
    void avis_rejectWhenUserHasNotPurchasedProduct() {
        when(authService.getCurrentPopulationUser()).thenReturn(citoyenA);
        when(produitRepository.findById(100L)).thenReturn(Optional.of(produit));
        when(ligneCommandeRepository.hasUserPurchasedAndReceivedProduct(10L, 100L)).thenReturn(false);

        PopulationCreateAvisRequest req = PopulationCreateAvisRequest.builder()
                .produitId(100L)
                .note(4)
                .commentaire("Je donne un avis sans avoir commandé.")
                .build();

        assertThatThrownBy(() -> avisService.creerAvis(req))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Vous devez avoir commandé et réceptionné ce produit");
    }

    @Test
    @DisplayName("Avis Client - Refus du doublon si l'utilisateur a déjà déposé un avis sur le même produit")
    void avis_rejectDuplicateReview() {
        when(authService.getCurrentPopulationUser()).thenReturn(citoyenA);
        when(produitRepository.findById(100L)).thenReturn(Optional.of(produit));
        when(ligneCommandeRepository.hasUserPurchasedAndReceivedProduct(10L, 100L)).thenReturn(true);
        when(avisRepository.existsByUtilisateurIdAndProduitId(10L, 100L)).thenReturn(true);

        PopulationCreateAvisRequest req = PopulationCreateAvisRequest.builder()
                .produitId(100L)
                .note(5)
                .commentaire("Deuxième avis sur le même produit.")
                .build();

        assertThatThrownBy(() -> avisService.creerAvis(req))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Vous avez déjà déposé un avis pour ce produit");
    }

    @Test
    @DisplayName("Annulation Commande - Restitution automatique des stocks et bascule au statut ANNULEE")
    void commande_cancelRestoresStock() {
        when(authService.getCurrentPopulationUser()).thenReturn(citoyenA);

        Commande commande = Commande.builder()
                .id(555L)
                .numero("CMD-555")
                .utilisateur(citoyenA)
                .pharmacopee(pharmacopee)
                .statut(StatutCommande.EN_ATTENTE)
                .lignes(new ArrayList<>())
                .build();

        LigneCommande lc = LigneCommande.builder()
                .id(1L)
                .commande(commande)
                .produit(produit)
                .quantite(3)
                .build();
        commande.getLignes().add(lc);

        disponibilite.setQuantiteStock(5); // stock initial

        when(commandeRepository.findByIdAndUtilisateurId(555L, 10L)).thenReturn(Optional.of(commande));
        when(dispoRepo.findByPharmacopeeIdAndProduitId(1L, 100L)).thenReturn(Optional.of(disponibilite));

        commandeService.annulerCommande(555L);

        assertThat(commande.getStatut()).isEqualTo(StatutCommande.ANNULEE);
        assertThat(disponibilite.getQuantiteStock()).isEqualTo(8); // 5 + 3 = 8
        verify(dispoRepo).save(disponibilite);
    }

    @Test
    @DisplayName("Annulation Commande - Interdiction d'annuler une commande déjà livrée")
    void commande_rejectCancelWhenAlreadyDelivered() {
        when(authService.getCurrentPopulationUser()).thenReturn(citoyenA);

        Commande commande = Commande.builder()
                .id(555L)
                .utilisateur(citoyenA)
                .statut(StatutCommande.LIVREE)
                .build();

        when(commandeRepository.findByIdAndUtilisateurId(555L, 10L)).thenReturn(Optional.of(commande));

        assertThatThrownBy(() -> commandeService.annulerCommande(555L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Impossible d'annuler cette commande");
    }
}
