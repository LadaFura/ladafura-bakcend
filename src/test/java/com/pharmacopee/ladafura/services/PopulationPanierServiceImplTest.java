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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import com.pharmacopee.ladafura.Models.LignePanier;
import com.pharmacopee.ladafura.Models.Panier;
import com.pharmacopee.ladafura.Models.Produit;
import com.pharmacopee.ladafura.Models.Utilisateur;
import com.pharmacopee.ladafura.dto.population.panier.PopulationAddToCartRequest;
import com.pharmacopee.ladafura.dto.population.panier.PopulationPanierResponse;
import com.pharmacopee.ladafura.dto.population.panier.PopulationUpdateQuantityRequest;
import com.pharmacopee.ladafura.enums.Role;
import com.pharmacopee.ladafura.enums.StatutProduit;
import com.pharmacopee.ladafura.enums.StatutUtilisateur;
import com.pharmacopee.ladafura.exceptions.ResourceNotFoundException;
import com.pharmacopee.ladafura.mappers.PopulationPanierMapper;
import com.pharmacopee.ladafura.repository.LignePanierRepository;
import com.pharmacopee.ladafura.repository.PanierRepository;
import com.pharmacopee.ladafura.repository.ProduitRepository;
import com.pharmacopee.ladafura.services.impl.PopulationPanierServiceImpl;
import com.pharmacopee.ladafura.services.interfaces.IPopulationAuthService;

@ExtendWith(MockitoExtension.class)
class PopulationPanierServiceImplTest {

    @Mock
    private IPopulationAuthService populationAuthService;

    @Mock
    private PanierRepository panierRepository;

    @Mock
    private LignePanierRepository lignePanierRepository;

    @Mock
    private ProduitRepository produitRepository;

    @Spy
    private PopulationPanierMapper mapper = new PopulationPanierMapper();

    @InjectMocks
    private PopulationPanierServiceImpl panierService;

    private Utilisateur currentUser;
    private Produit produit1;
    private Panier panier;

    @BeforeEach
    void setUp() {
        currentUser = new Utilisateur();
        currentUser.setId(10L);
        currentUser.setFirebaseUid("uid-pop-123");
        currentUser.setEmail("moussa@example.com");
        currentUser.setNom("Traoré");
        currentUser.setPrenom("Moussa");
        currentUser.setRole(Role.POPULATION);
        currentUser.setStatut(StatutUtilisateur.ACTIF);

        produit1 = Produit.builder()
                .id(1L)
                .nom("Sirop d'Artemisia")
                .forme("Sirop 250ml")
                .prix(2500.0)
                .statut(StatutProduit.VALIDE)
                .build();

        panier = Panier.builder()
                .id(100L)
                .utilisateur(currentUser)
                .dateCreation(LocalDateTime.now())
                .dateModification(LocalDateTime.now())
                .lignes(new ArrayList<>())
                .build();
    }

    @Test
    @DisplayName("getPanier - Récupère le panier existant avec ses lignes")
    void getPanier_ExistingPanier() {
        LignePanier ligne = LignePanier.builder()
                .id(11L)
                .panier(panier)
                .produit(produit1)
                .quantite(2)
                .prixUnitaire(2500.0)
                .sousTotal(5000.0)
                .build();
        panier.getLignes().add(ligne);

        when(populationAuthService.getCurrentPopulationUser()).thenReturn(currentUser);
        when(panierRepository.findByUtilisateurId(10L)).thenReturn(Optional.of(panier));

        PopulationPanierResponse response = panierService.getPanier();

        assertThat(response).isNotNull();
        assertThat(response.getPanierId()).isEqualTo(100L);
        assertThat(response.getNombreArticles()).isEqualTo(2);
        assertThat(response.getMontantTotal()).isEqualTo(5000.0);
        assertThat(response.getLignes()).hasSize(1);
        assertThat(response.getLignes().get(0).getNomProduit()).isEqualTo("Sirop d'Artemisia");
    }

    @Test
    @DisplayName("getPanier - Panier non créé encore, renvoie un panier vide sans crash")
    void getPanier_NoPanierYet() {
        when(populationAuthService.getCurrentPopulationUser()).thenReturn(currentUser);
        when(panierRepository.findByUtilisateurId(10L)).thenReturn(Optional.empty());

        PopulationPanierResponse response = panierService.getPanier();

        assertThat(response).isNotNull();
        assertThat(response.getNombreArticles()).isEqualTo(0);
        assertThat(response.getMontantTotal()).isEqualTo(0.0);
        assertThat(response.getLignes()).isEmpty();
    }

    @Test
    @DisplayName("ajouterProduitAuPanier - Nouveau produit dans un panier existant")
    void ajouterProduitAuPanier_NewItem() {
        PopulationAddToCartRequest request = PopulationAddToCartRequest.builder()
                .produitId(1L)
                .quantite(2)
                .build();

        when(populationAuthService.getCurrentPopulationUser()).thenReturn(currentUser);
        when(produitRepository.findByIdAndStatut(1L, StatutProduit.VALIDE)).thenReturn(Optional.of(produit1));
        when(panierRepository.findByUtilisateurId(10L)).thenReturn(Optional.of(panier));
        when(lignePanierRepository.findByPanierIdAndProduitId(100L, 1L)).thenReturn(Optional.empty());
        when(lignePanierRepository.save(any(LignePanier.class))).thenAnswer(invocation -> {
            LignePanier lp = invocation.getArgument(0);
            lp.setId(21L);
            return lp;
        });
        when(panierRepository.save(any(Panier.class))).thenReturn(panier);

        PopulationPanierResponse response = panierService.ajouterProduitAuPanier(request);

        assertThat(response).isNotNull();
        assertThat(response.getNombreArticles()).isEqualTo(2);
        assertThat(response.getMontantTotal()).isEqualTo(5000.0);
        verify(lignePanierRepository).save(any(LignePanier.class));
        verify(panierRepository).save(panier);
    }

    @Test
    @DisplayName("ajouterProduitAuPanier - Produit déjà présent dans le panier -> incrémente la quantité")
    void ajouterProduitAuPanier_ExistingItemIncrement() {
        LignePanier existingLigne = LignePanier.builder()
                .id(22L)
                .panier(panier)
                .produit(produit1)
                .quantite(1)
                .prixUnitaire(2500.0)
                .sousTotal(2500.0)
                .build();
        panier.getLignes().add(existingLigne);

        PopulationAddToCartRequest request = PopulationAddToCartRequest.builder()
                .produitId(1L)
                .quantite(2)
                .build();

        when(populationAuthService.getCurrentPopulationUser()).thenReturn(currentUser);
        when(produitRepository.findByIdAndStatut(1L, StatutProduit.VALIDE)).thenReturn(Optional.of(produit1));
        when(panierRepository.findByUtilisateurId(10L)).thenReturn(Optional.of(panier));
        when(lignePanierRepository.findByPanierIdAndProduitId(100L, 1L)).thenReturn(Optional.of(existingLigne));
        when(panierRepository.save(any(Panier.class))).thenReturn(panier);

        PopulationPanierResponse response = panierService.ajouterProduitAuPanier(request);

        assertThat(response).isNotNull();
        assertThat(existingLigne.getQuantite()).isEqualTo(3);
        assertThat(existingLigne.getSousTotal()).isEqualTo(7500.0);
        verify(lignePanierRepository).save(existingLigne);
    }

    @Test
    @DisplayName("ajouterProduitAuPanier - Produit introuvable ou non VALIDE -> ResourceNotFoundException")
    void ajouterProduitAuPanier_ProductNotFound() {
        PopulationAddToCartRequest request = PopulationAddToCartRequest.builder()
                .produitId(999L)
                .quantite(1)
                .build();

        when(populationAuthService.getCurrentPopulationUser()).thenReturn(currentUser);
        when(produitRepository.findByIdAndStatut(999L, StatutProduit.VALIDE)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> panierService.ajouterProduitAuPanier(request))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(panierRepository, never()).save(any(Panier.class));
    }

    @Test
    @DisplayName("modifierQuantiteLigne - Modifie avec succès la quantité et recalcule")
    void modifierQuantiteLigne_Success() {
        LignePanier ligne = LignePanier.builder()
                .id(30L)
                .panier(panier)
                .produit(produit1)
                .quantite(1)
                .prixUnitaire(2500.0)
                .sousTotal(2500.0)
                .build();
        panier.getLignes().add(ligne);

        PopulationUpdateQuantityRequest request = PopulationUpdateQuantityRequest.builder()
                .quantite(4)
                .build();

        when(populationAuthService.getCurrentPopulationUser()).thenReturn(currentUser);
        when(panierRepository.findByUtilisateurId(10L)).thenReturn(Optional.of(panier));
        when(lignePanierRepository.findByIdAndPanierId(30L, 100L)).thenReturn(Optional.of(ligne));
        when(panierRepository.save(any(Panier.class))).thenReturn(panier);

        PopulationPanierResponse response = panierService.modifierQuantiteLigne(30L, request);

        assertThat(response).isNotNull();
        assertThat(ligne.getQuantite()).isEqualTo(4);
        assertThat(ligne.getSousTotal()).isEqualTo(10000.0);
        verify(lignePanierRepository).save(ligne);
    }

    @Test
    @DisplayName("modifierQuantiteLigne - Quantité <= 0 supprime la ligne du panier")
    void modifierQuantiteLigne_ZeroQuantityRemovesLine() {
        LignePanier ligne = LignePanier.builder()
                .id(31L)
                .panier(panier)
                .produit(produit1)
                .quantite(2)
                .prixUnitaire(2500.0)
                .sousTotal(5000.0)
                .build();
        panier.getLignes().add(ligne);

        PopulationUpdateQuantityRequest request = PopulationUpdateQuantityRequest.builder()
                .quantite(0)
                .build();

        when(populationAuthService.getCurrentPopulationUser()).thenReturn(currentUser);
        when(panierRepository.findByUtilisateurId(10L)).thenReturn(Optional.of(panier));
        when(lignePanierRepository.findByIdAndPanierId(31L, 100L)).thenReturn(Optional.of(ligne));
        when(panierRepository.save(any(Panier.class))).thenReturn(panier);

        PopulationPanierResponse response = panierService.modifierQuantiteLigne(31L, request);

        assertThat(response).isNotNull();
        assertThat(panier.getLignes()).doesNotContain(ligne);
        verify(lignePanierRepository).delete(ligne);
    }

    @Test
    @DisplayName("supprimerLignePanier - Supprime la ligne et recalcule le panier")
    void supprimerLignePanier_Success() {
        LignePanier ligne = LignePanier.builder()
                .id(40L)
                .panier(panier)
                .produit(produit1)
                .quantite(2)
                .prixUnitaire(2500.0)
                .sousTotal(5000.0)
                .build();
        panier.getLignes().add(ligne);

        when(populationAuthService.getCurrentPopulationUser()).thenReturn(currentUser);
        when(panierRepository.findByUtilisateurId(10L)).thenReturn(Optional.of(panier));
        when(lignePanierRepository.findByIdAndPanierId(40L, 100L)).thenReturn(Optional.of(ligne));
        when(panierRepository.save(any(Panier.class))).thenReturn(panier);

        PopulationPanierResponse response = panierService.supprimerLignePanier(40L);

        assertThat(response).isNotNull();
        assertThat(panier.getLignes()).doesNotContain(ligne);
        verify(lignePanierRepository).delete(ligne);
    }

    @Test
    @DisplayName("viderPanier - Supprime toutes les lignes et met à jour le panier")
    void viderPanier_Success() {
        LignePanier ligne = LignePanier.builder()
                .id(50L)
                .panier(panier)
                .produit(produit1)
                .quantite(2)
                .prixUnitaire(2500.0)
                .sousTotal(5000.0)
                .build();
        panier.getLignes().add(ligne);

        when(populationAuthService.getCurrentPopulationUser()).thenReturn(currentUser);
        when(panierRepository.findByUtilisateurId(10L)).thenReturn(Optional.of(panier));

        panierService.viderPanier();

        assertThat(panier.getLignes()).isEmpty();
        verify(lignePanierRepository).deleteByPanierId(100L);
        verify(panierRepository).save(panier);
    }
}
