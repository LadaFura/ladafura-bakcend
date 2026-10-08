package com.pharmacopee.ladafura.services;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.pharmacopee.ladafura.Models.Commande;
import com.pharmacopee.ladafura.Models.DisponibiliteProduit;
import com.pharmacopee.ladafura.Models.LigneCommande;
import com.pharmacopee.ladafura.Models.LignePanier;
import com.pharmacopee.ladafura.Models.ModeRetrait;
import com.pharmacopee.ladafura.Models.Panier;
import com.pharmacopee.ladafura.Models.Pharmacopee;
import com.pharmacopee.ladafura.Models.Produit;
import com.pharmacopee.ladafura.Models.Utilisateur;
import com.pharmacopee.ladafura.dto.population.commande.PopulationCommandeDetailResponse;
import com.pharmacopee.ladafura.dto.population.commande.PopulationCommandeRecapitulatifRequest;
import com.pharmacopee.ladafura.dto.population.commande.PopulationCommandeRecapitulatifResponse;
import com.pharmacopee.ladafura.dto.population.commande.PopulationCommandeStatutResponse;
import com.pharmacopee.ladafura.dto.population.commande.PopulationCommandeSummaryResponse;
import com.pharmacopee.ladafura.dto.population.commande.PopulationCreateCommandeRequest;
import com.pharmacopee.ladafura.enums.MethodePaiement;
import com.pharmacopee.ladafura.enums.Role;
import com.pharmacopee.ladafura.enums.StatutCommande;
import com.pharmacopee.ladafura.enums.StatutPharmacopee;
import com.pharmacopee.ladafura.enums.StatutProduit;
import com.pharmacopee.ladafura.enums.StatutUtilisateur;
import com.pharmacopee.ladafura.enums.TypeModeRetrait;
import com.pharmacopee.ladafura.exceptions.BadRequestException;
import com.pharmacopee.ladafura.mappers.PopulationCommandeMapper;
import com.pharmacopee.ladafura.repository.CommandeRepository;
import com.pharmacopee.ladafura.repository.DisponibiliteProduitRepository;
import com.pharmacopee.ladafura.repository.LignePanierRepository;
import com.pharmacopee.ladafura.repository.ModeRetraitRepository;
import com.pharmacopee.ladafura.repository.PaiementRepository;
import com.pharmacopee.ladafura.repository.PanierRepository;
import com.pharmacopee.ladafura.repository.PharmacopeeRepository;
import com.pharmacopee.ladafura.services.impl.PopulationCommandeServiceImpl;
import com.pharmacopee.ladafura.services.interfaces.IPopulationAuthService;

@ExtendWith(MockitoExtension.class)
class PopulationCommandeServiceImplTest {

    @Mock
    private IPopulationAuthService populationAuthService;

    @Mock
    private CommandeRepository commandeRepository;

    @Mock
    private PanierRepository panierRepository;

    @Mock
    private LignePanierRepository lignePanierRepository;

    @Mock
    private PharmacopeeRepository pharmacopeeRepository;

    @Mock
    private ModeRetraitRepository modeRetraitRepository;

    @Mock
    private DisponibiliteProduitRepository disponibiliteProduitRepository;

    @Mock
    private PaiementRepository paiementRepository;

    @Spy
    private PopulationCommandeMapper mapper = new PopulationCommandeMapper();

    @InjectMocks
    private PopulationCommandeServiceImpl commandeService;

    private Utilisateur currentUser;
    private Pharmacopee pharmacopee;
    private ModeRetrait modeLivraison;
    private ModeRetrait modePickup;
    private Produit produit;
    private Panier panier;
    private DisponibiliteProduit disponibilite;

    @BeforeEach
    void setUp() {
        currentUser = new Utilisateur();
        currentUser.setId(10L);
        currentUser.setEmail("client@ladafura.ml");
        currentUser.setNom("Diarra");
        currentUser.setPrenom("Fatoumata");
        currentUser.setRole(Role.POPULATION);
        currentUser.setStatut(StatutUtilisateur.ACTIF);

        pharmacopee = Pharmacopee.builder()
                .id(1L)
                .nom("Pharmacie Mandé")
                .telephone("+223 70 11 22 33")
                .statut(StatutPharmacopee.VALIDEE)
                .build();

        modeLivraison = ModeRetrait.builder()
                .id(10L)
                .type(TypeModeRetrait.LIVRAISON)
                .actif(true)
                .frais(1500.0)
                .pharmacopee(pharmacopee)
                .build();

        modePickup = ModeRetrait.builder()
                .id(11L)
                .type(TypeModeRetrait.PICKUP)
                .actif(true)
                .frais(0.0)
                .pharmacopee(pharmacopee)
                .build();

        produit = Produit.builder()
                .id(5L)
                .nom("Sirop d'Artemisia")
                .forme("Sirop 250ml")
                .prix(2500.0)
                .statut(StatutProduit.VALIDE)
                .build();

        disponibilite = DisponibiliteProduit.builder()
                .id(100L)
                .pharmacopee(pharmacopee)
                .produit(produit)
                .prix(2500.0)
                .disponible(true)
                .quantiteStock(50)
                .build();

        panier = Panier.builder()
                .id(20L)
                .utilisateur(currentUser)
                .dateCreation(LocalDateTime.now())
                .dateModification(LocalDateTime.now())
                .lignes(new ArrayList<>())
                .build();

        LignePanier ligne = LignePanier.builder()
                .id(201L)
                .panier(panier)
                .produit(produit)
                .quantite(2)
                .prixUnitaire(2500.0)
                .sousTotal(5000.0)
                .build();
        panier.getLignes().add(ligne);
    }

    @Test
    @DisplayName("getRecapitulatif - Calcul correct du récapitulatif avec frais de livraison")
    void getRecapitulatif_Success() {
        PopulationCommandeRecapitulatifRequest request = PopulationCommandeRecapitulatifRequest.builder()
                .pharmacopeeId(1L)
                .modeRetraitId(10L)
                .build();

        when(populationAuthService.getCurrentPopulationUser()).thenReturn(currentUser);
        when(panierRepository.findByUtilisateurId(10L)).thenReturn(Optional.of(panier));
        when(pharmacopeeRepository.findById(1L)).thenReturn(Optional.of(pharmacopee));
        when(modeRetraitRepository.findById(10L)).thenReturn(Optional.of(modeLivraison));
        when(disponibiliteProduitRepository.findByPharmacopeeIdAndProduitId(1L, 5L))
                .thenReturn(Optional.of(disponibilite));

        PopulationCommandeRecapitulatifResponse response = commandeService.getRecapitulatif(request);

        assertThat(response).isNotNull();
        assertThat(response.getNomPharmacopee()).isEqualTo("Pharmacie Mandé");
        assertThat(response.getModeRetrait()).isEqualTo("LIVRAISON");
        assertThat(response.getFraisLivraison()).isEqualTo(1500.0);
        assertThat(response.getTotalProduits()).isEqualTo(5000.0);
        assertThat(response.getMontantTotal()).isEqualTo(6500.0);
        assertThat(response.getNombreArticles()).isEqualTo(2);
        assertThat(response.getLignes()).hasSize(1);
    }

    @Test
    @DisplayName("getRecapitulatif - Panier vide lance BadRequestException")
    void getRecapitulatif_EmptyCart() {
        panier.getLignes().clear();
        PopulationCommandeRecapitulatifRequest request = PopulationCommandeRecapitulatifRequest.builder()
                .pharmacopeeId(1L)
                .modeRetraitId(10L)
                .build();

        when(populationAuthService.getCurrentPopulationUser()).thenReturn(currentUser);
        when(panierRepository.findByUtilisateurId(10L)).thenReturn(Optional.of(panier));

        assertThatThrownBy(() -> commandeService.getRecapitulatif(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("panier est vide");
    }

    @Test
    @DisplayName("passerCommande - Confirmation avec Livraison et Paiement validé : commande créée, stock décrémenté et panier vidé")
    void passerCommande_Livraison_Success() {
        PopulationCreateCommandeRequest request = PopulationCreateCommandeRequest.builder()
                .pharmacopeeId(1L)
                .modeRetraitId(10L)
                .adresseLivraison("Badalabougou Rue 12")
                .notes("Appeler en arrivant")
                .methode(MethodePaiement.MOBILE_MONEY)
                .operateur("ORANGE_MONEY")
                .telephoneMobileMoney("+22370112233")
                .simulerSucces(true)
                .build();

        when(populationAuthService.getCurrentPopulationUser()).thenReturn(currentUser);
        when(panierRepository.findByUtilisateurId(10L)).thenReturn(Optional.of(panier));
        when(pharmacopeeRepository.findById(1L)).thenReturn(Optional.of(pharmacopee));
        when(modeRetraitRepository.findById(10L)).thenReturn(Optional.of(modeLivraison));
        when(disponibiliteProduitRepository.findByPharmacopeeIdAndProduitId(1L, 5L))
                .thenReturn(Optional.of(disponibilite));
        when(commandeRepository.existsByNumero(any())).thenReturn(false);
        when(commandeRepository.save(any(Commande.class))).thenAnswer(invocation -> {
            Commande c = invocation.getArgument(0);
            c.setId(1001L);
            return c;
        });

        PopulationCommandeDetailResponse response = commandeService.passerCommande(request);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1001L);
        assertThat(response.getStatut()).isEqualTo(StatutCommande.CONFIRMEE);
        assertThat(response.getTotalProduit()).isEqualTo(5000.0);
        assertThat(response.getMontantLivraison()).isEqualTo(1500.0);
        assertThat(response.getMontantTotal()).isEqualTo(6500.0);
        assertThat(response.getStatutPaiement()).isEqualTo("REUSSI");
        assertThat(disponibilite.getQuantiteStock()).isEqualTo(48); // 50 - 2
        assertThat(panier.getLignes()).isEmpty();

        verify(disponibiliteProduitRepository).save(disponibilite);
        verify(paiementRepository).save(any());
        verify(lignePanierRepository).deleteByPanierId(20L);
        verify(panierRepository).save(panier);
    }

    @Test
    @DisplayName("passerCommande - Échec du paiement Mobile Money : aucune commande enregistrée, panier et stocks intacts")
    void passerCommande_PaymentFailure_NoOrderCreated() {
        PopulationCreateCommandeRequest request = PopulationCreateCommandeRequest.builder()
                .pharmacopeeId(1L)
                .modeRetraitId(10L)
                .adresseLivraison("Badalabougou Rue 12")
                .methode(MethodePaiement.MOBILE_MONEY)
                .operateur("ORANGE_MONEY")
                .telephoneMobileMoney("+22370112233")
                .simulerSucces(false) // Échec de paiement
                .build();

        when(populationAuthService.getCurrentPopulationUser()).thenReturn(currentUser);
        when(panierRepository.findByUtilisateurId(10L)).thenReturn(Optional.of(panier));
        when(pharmacopeeRepository.findById(1L)).thenReturn(Optional.of(pharmacopee));
        when(modeRetraitRepository.findById(10L)).thenReturn(Optional.of(modeLivraison));
        when(disponibiliteProduitRepository.findByPharmacopeeIdAndProduitId(1L, 5L))
                .thenReturn(Optional.of(disponibilite));
        when(commandeRepository.existsByNumero(any())).thenReturn(false);

        assertThatThrownBy(() -> commandeService.passerCommande(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Le règlement via Mobile Money a échoué");

        // Vérification stricte : aucun stock modifié, aucune commande sauvée, panier non vidé
        assertThat(disponibilite.getQuantiteStock()).isEqualTo(50);
        assertThat(panier.getLignes()).isNotEmpty();
    }

    @Test
    @DisplayName("passerCommande - Livraison sans adresse -> BadRequestException")
    void passerCommande_MissingAddress() {
        PopulationCreateCommandeRequest request = PopulationCreateCommandeRequest.builder()
                .pharmacopeeId(1L)
                .modeRetraitId(10L)
                .adresseLivraison(null)
                .methode(MethodePaiement.CASH)
                .build();

        when(populationAuthService.getCurrentPopulationUser()).thenReturn(currentUser);
        when(panierRepository.findByUtilisateurId(10L)).thenReturn(Optional.of(panier));
        when(pharmacopeeRepository.findById(1L)).thenReturn(Optional.of(pharmacopee));
        when(modeRetraitRepository.findById(10L)).thenReturn(Optional.of(modeLivraison));

        assertThatThrownBy(() -> commandeService.passerCommande(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("adresse de livraison est obligatoire");
    }

    @Test
    @DisplayName("getHistoriqueCommandes - Récupère l'historique paginé")
    void getHistoriqueCommandes_Success() {
        Commande c = Commande.builder()
                .id(1L)
                .numero("CMD-2026-001")
                .dateCommande(LocalDateTime.now())
                .statut(StatutCommande.EN_ATTENTE)
                .totalProduit(5000.0)
                .montantLivraison(0.0)
                .montantTotal(5000.0)
                .pharmacopee(pharmacopee)
                .modeRetrait(modePickup)
                .lignes(List.of())
                .build();

        Pageable pageable = PageRequest.of(0, 10);
        when(populationAuthService.getCurrentPopulationUser()).thenReturn(currentUser);
        when(commandeRepository.findByUtilisateurId(10L, pageable))
                .thenReturn(new PageImpl<>(List.of(c)));

        Page<PopulationCommandeSummaryResponse> page = commandeService.getHistoriqueCommandes(null, pageable);

        assertThat(page).isNotNull();
        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent().get(0).getNumero()).isEqualTo("CMD-2026-001");
    }

    @Test
    @DisplayName("getCommandeDetail - Récupère le détail d'une commande appartenant au client")
    void getCommandeDetail_Success() {
        Commande c = Commande.builder()
                .id(1L)
                .numero("CMD-2026-001")
                .dateCommande(LocalDateTime.now())
                .statut(StatutCommande.EN_ATTENTE)
                .totalProduit(5000.0)
                .montantLivraison(1500.0)
                .montantTotal(6500.0)
                .pharmacopee(pharmacopee)
                .modeRetrait(modeLivraison)
                .adresseLivraison("Bamako")
                .lignes(List.of())
                .build();

        when(populationAuthService.getCurrentPopulationUser()).thenReturn(currentUser);
        when(commandeRepository.findByIdAndUtilisateurId(1L, 10L)).thenReturn(Optional.of(c));

        PopulationCommandeDetailResponse response = commandeService.getCommandeDetail(1L);

        assertThat(response).isNotNull();
        assertThat(response.getNumero()).isEqualTo("CMD-2026-001");
        assertThat(response.getMontantTotal()).isEqualTo(6500.0);
    }

    @Test
    @DisplayName("getCommandeStatut - Renvoie le statut avec le message adapté")
    void getCommandeStatut_Success() {
        Commande c = Commande.builder()
                .id(1L)
                .numero("CMD-2026-001")
                .dateCommande(LocalDateTime.now())
                .statut(StatutCommande.EN_ATTENTE)
                .modeRetrait(modeLivraison)
                .build();

        when(populationAuthService.getCurrentPopulationUser()).thenReturn(currentUser);
        when(commandeRepository.findByIdAndUtilisateurId(1L, 10L)).thenReturn(Optional.of(c));

        PopulationCommandeStatutResponse response = commandeService.getCommandeStatut(1L);

        assertThat(response).isNotNull();
        assertThat(response.getStatut()).isEqualTo(StatutCommande.EN_ATTENTE);
        assertThat(response.getAnnulable()).isTrue();
        assertThat(response.getMessage()).contains("attend sa confirmation");
    }

    @Test
    @DisplayName("annulerCommande - Annulation réussie au statut EN_ATTENTE et réintégration du stock")
    void annulerCommande_Success() {
        LigneCommande lc = LigneCommande.builder()
                .id(501L)
                .produit(produit)
                .quantite(2)
                .prixUnitaire(2500.0)
                .sousTotal(5000.0)
                .build();

        Commande c = Commande.builder()
                .id(1L)
                .numero("CMD-2026-001")
                .statut(StatutCommande.EN_ATTENTE)
                .pharmacopee(pharmacopee)
                .modeRetrait(modeLivraison)
                .lignes(new ArrayList<>(List.of(lc)))
                .build();

        when(populationAuthService.getCurrentPopulationUser()).thenReturn(currentUser);
        when(commandeRepository.findByIdAndUtilisateurId(1L, 10L)).thenReturn(Optional.of(c));
        when(disponibiliteProduitRepository.findByPharmacopeeIdAndProduitId(1L, 5L))
                .thenReturn(Optional.of(disponibilite));
        when(commandeRepository.save(any(Commande.class))).thenReturn(c);

        disponibilite.setQuantiteStock(48);
        PopulationCommandeDetailResponse response = commandeService.annulerCommande(1L);

        assertThat(response).isNotNull();
        assertThat(c.getStatut()).isEqualTo(StatutCommande.ANNULEE);
        assertThat(disponibilite.getQuantiteStock()).isEqualTo(50); // réintégré +2
        verify(disponibiliteProduitRepository).save(disponibilite);
        verify(commandeRepository).save(c);
    }

    @Test
    @DisplayName("annulerCommande - Commande déjà préparée -> BadRequestException")
    void annulerCommande_AlreadyPrepared() {
        Commande c = Commande.builder()
                .id(1L)
                .numero("CMD-2026-001")
                .statut(StatutCommande.PREPAREE)
                .pharmacopee(pharmacopee)
                .modeRetrait(modeLivraison)
                .build();

        when(populationAuthService.getCurrentPopulationUser()).thenReturn(currentUser);
        when(commandeRepository.findByIdAndUtilisateurId(1L, 10L)).thenReturn(Optional.of(c));

        assertThatThrownBy(() -> commandeService.annulerCommande(1L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Impossible d'annuler cette commande");
    }
}
