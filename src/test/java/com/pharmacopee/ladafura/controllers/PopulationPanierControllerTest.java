package com.pharmacopee.ladafura.controllers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.pharmacopee.ladafura.controllers.population.PopulationPanierController;
import com.pharmacopee.ladafura.dto.population.panier.PopulationAddToCartRequest;
import com.pharmacopee.ladafura.dto.population.panier.PopulationLignePanierResponse;
import com.pharmacopee.ladafura.dto.population.panier.PopulationPanierResponse;
import com.pharmacopee.ladafura.dto.population.panier.PopulationUpdateQuantityRequest;
import com.pharmacopee.ladafura.services.interfaces.IPopulationPanierService;

@ExtendWith(MockitoExtension.class)
class PopulationPanierControllerTest {

    @Mock
    private IPopulationPanierService panierService;

    @InjectMocks
    private PopulationPanierController panierController;

    @Test
    @DisplayName("GET /api/v1/population/panier - 200 OK")
    void getPanier_200OK() {
        PopulationPanierResponse responseDto = PopulationPanierResponse.builder()
                .panierId(10L)
                .nombreArticles(2)
                .montantTotal(5000.0)
                .lignes(List.of(
                        PopulationLignePanierResponse.builder()
                                .ligneId(1L)
                                .produitId(5L)
                                .nomProduit("Artemisia")
                                .quantite(2)
                                .prixUnitaire(2500.0)
                                .sousTotal(5000.0)
                                .disponible(true)
                                .build()
                ))
                .build();

        when(panierService.getPanier()).thenReturn(responseDto);

        ResponseEntity<PopulationPanierResponse> response = panierController.getPanier();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getPanierId()).isEqualTo(10L);
        assertThat(response.getBody().getNombreArticles()).isEqualTo(2);
        assertThat(response.getBody().getMontantTotal()).isEqualTo(5000.0);
        assertThat(response.getBody().getLignes()).hasSize(1);

        verify(panierService).getPanier();
    }

    @Test
    @DisplayName("POST /api/v1/population/panier/lignes - 200 OK")
    void ajouterProduitAuPanier_200OK() {
        PopulationAddToCartRequest request = PopulationAddToCartRequest.builder()
                .produitId(5L)
                .quantite(1)
                .build();

        PopulationPanierResponse responseDto = PopulationPanierResponse.builder()
                .panierId(10L)
                .nombreArticles(1)
                .montantTotal(2500.0)
                .build();

        when(panierService.ajouterProduitAuPanier(request)).thenReturn(responseDto);

        ResponseEntity<PopulationPanierResponse> response = panierController.ajouterProduitAuPanier(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getNombreArticles()).isEqualTo(1);

        verify(panierService).ajouterProduitAuPanier(request);
    }

    @Test
    @DisplayName("PUT /api/v1/population/panier/lignes/{ligneId} - 200 OK")
    void modifierQuantiteLigne_200OK() {
        PopulationUpdateQuantityRequest request = PopulationUpdateQuantityRequest.builder()
                .quantite(3)
                .build();

        PopulationPanierResponse responseDto = PopulationPanierResponse.builder()
                .panierId(10L)
                .nombreArticles(3)
                .montantTotal(7500.0)
                .build();

        when(panierService.modifierQuantiteLigne(1L, request)).thenReturn(responseDto);

        ResponseEntity<PopulationPanierResponse> response = panierController.modifierQuantiteLigne(1L, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getNombreArticles()).isEqualTo(3);

        verify(panierService).modifierQuantiteLigne(1L, request);
    }

    @Test
    @DisplayName("DELETE /api/v1/population/panier/lignes/{ligneId} - 200 OK")
    void supprimerLignePanier_200OK() {
        PopulationPanierResponse responseDto = PopulationPanierResponse.builder()
                .panierId(10L)
                .nombreArticles(0)
                .montantTotal(0.0)
                .lignes(List.of())
                .build();

        when(panierService.supprimerLignePanier(1L)).thenReturn(responseDto);

        ResponseEntity<PopulationPanierResponse> response = panierController.supprimerLignePanier(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getNombreArticles()).isEqualTo(0);

        verify(panierService).supprimerLignePanier(1L);
    }

    @Test
    @DisplayName("DELETE /api/v1/population/panier - 204 No Content")
    void viderPanier_204NoContent() {
        ResponseEntity<Void> response = panierController.viderPanier();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(response.getBody()).isNull();

        verify(panierService).viderPanier();
    }
}
