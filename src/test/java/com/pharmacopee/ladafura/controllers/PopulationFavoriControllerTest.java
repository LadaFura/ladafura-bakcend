package com.pharmacopee.ladafura.controllers;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.pharmacopee.ladafura.controllers.population.PopulationFavoriController;
import com.pharmacopee.ladafura.dto.population.favori.PopulationFavoriCheckResponse;
import com.pharmacopee.ladafura.dto.population.favori.PopulationFavoriCountResponse;
import com.pharmacopee.ladafura.dto.population.favori.PopulationFavoriItemResponse;
import com.pharmacopee.ladafura.dto.population.favori.PopulationToggleFavoriRequest;
import com.pharmacopee.ladafura.dto.population.favori.PopulationToggleFavoriResponse;
import com.pharmacopee.ladafura.dto.population.pharmacopee.PopulationPharmacopeeSummaryResponse;
import com.pharmacopee.ladafura.dto.population.plante.PopulationPlanteSummaryResponse;
import com.pharmacopee.ladafura.dto.population.produit.PopulationProduitSummaryResponse;
import com.pharmacopee.ladafura.enums.TypeFavori;
import com.pharmacopee.ladafura.services.interfaces.IPopulationFavoriService;

@ExtendWith(MockitoExtension.class)
class PopulationFavoriControllerTest {

    @Mock
    private IPopulationFavoriService favoriService;

    @InjectMocks
    private PopulationFavoriController favoriController;

    @Test
    @DisplayName("POST /api/v1/population/favoris/toggle - 200 OK")
    void toggleFavori_200OK() {
        PopulationToggleFavoriRequest request = PopulationToggleFavoriRequest.builder()
                .type(TypeFavori.PRODUIT)
                .cibleId(5L)
                .build();

        PopulationToggleFavoriResponse response = PopulationToggleFavoriResponse.builder()
                .type(TypeFavori.PRODUIT)
                .cibleId(5L)
                .favori(true)
                .favoriId(101L)
                .message("Produit ajouté aux favoris avec succès.")
                .build();

        when(favoriService.toggleFavori(any(PopulationToggleFavoriRequest.class))).thenReturn(response);

        ResponseEntity<PopulationToggleFavoriResponse> result = favoriController.toggleFavori(request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().isFavori()).isTrue();
        assertThat(result.getBody().getFavoriId()).isEqualTo(101L);
    }

    @Test
    @DisplayName("POST /api/v1/population/favoris/plantes/{id} - 200 OK")
    void ajouterPlanteFavori_200OK() {
        PopulationToggleFavoriResponse response = PopulationToggleFavoriResponse.builder()
                .type(TypeFavori.PLANTE)
                .cibleId(1L)
                .favori(true)
                .favoriId(100L)
                .build();

        when(favoriService.ajouterPlanteFavori(1L)).thenReturn(response);

        ResponseEntity<PopulationToggleFavoriResponse> result = favoriController.ajouterPlanteFavori(1L);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody().isFavori()).isTrue();
    }

    @Test
    @DisplayName("DELETE /api/v1/population/favoris/plantes/{id} - 200 OK")
    void supprimerPlanteFavori_200OK() {
        PopulationToggleFavoriResponse response = PopulationToggleFavoriResponse.builder()
                .type(TypeFavori.PLANTE)
                .cibleId(1L)
                .favori(false)
                .build();

        when(favoriService.supprimerPlanteFavori(1L)).thenReturn(response);

        ResponseEntity<PopulationToggleFavoriResponse> result = favoriController.supprimerPlanteFavori(1L);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody().isFavori()).isFalse();
    }

    @Test
    @DisplayName("POST /api/v1/population/favoris/produits/{id} - 200 OK")
    void ajouterProduitFavori_200OK() {
        PopulationToggleFavoriResponse response = PopulationToggleFavoriResponse.builder()
                .type(TypeFavori.PRODUIT)
                .cibleId(5L)
                .favori(true)
                .build();

        when(favoriService.ajouterProduitFavori(5L)).thenReturn(response);

        ResponseEntity<PopulationToggleFavoriResponse> result = favoriController.ajouterProduitFavori(5L);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody().isFavori()).isTrue();
    }

    @Test
    @DisplayName("DELETE /api/v1/population/favoris/produits/{id} - 200 OK")
    void supprimerProduitFavori_200OK() {
        PopulationToggleFavoriResponse response = PopulationToggleFavoriResponse.builder()
                .type(TypeFavori.PRODUIT)
                .cibleId(5L)
                .favori(false)
                .build();

        when(favoriService.supprimerProduitFavori(5L)).thenReturn(response);

        ResponseEntity<PopulationToggleFavoriResponse> result = favoriController.supprimerProduitFavori(5L);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody().isFavori()).isFalse();
    }

    @Test
    @DisplayName("POST /api/v1/population/favoris/pharmacopees/{id} - 200 OK")
    void ajouterPharmacopeeFavori_200OK() {
        PopulationToggleFavoriResponse response = PopulationToggleFavoriResponse.builder()
                .type(TypeFavori.PHARMACOPEE)
                .cibleId(3L)
                .favori(true)
                .build();

        when(favoriService.ajouterPharmacopeeFavori(3L)).thenReturn(response);

        ResponseEntity<PopulationToggleFavoriResponse> result = favoriController.ajouterPharmacopeeFavori(3L);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody().isFavori()).isTrue();
    }

    @Test
    @DisplayName("DELETE /api/v1/population/favoris/pharmacopees/{id} - 200 OK")
    void supprimerPharmacopeeFavori_200OK() {
        PopulationToggleFavoriResponse response = PopulationToggleFavoriResponse.builder()
                .type(TypeFavori.PHARMACOPEE)
                .cibleId(3L)
                .favori(false)
                .build();

        when(favoriService.supprimerPharmacopeeFavori(3L)).thenReturn(response);

        ResponseEntity<PopulationToggleFavoriResponse> result = favoriController.supprimerPharmacopeeFavori(3L);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody().isFavori()).isFalse();
    }

    @Test
    @DisplayName("DELETE /api/v1/population/favoris/{favoriId} - 204 No Content")
    void supprimerFavoriById_204NoContent() {
        ResponseEntity<Void> result = favoriController.supprimerFavoriById(100L);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(favoriService).supprimerFavoriById(100L);
    }

    @Test
    @DisplayName("GET /api/v1/population/favoris/check - 200 OK")
    void checkFavori_200OK() {
        PopulationFavoriCheckResponse response = PopulationFavoriCheckResponse.builder()
                .type(TypeFavori.PLANTE)
                .cibleId(1L)
                .favori(true)
                .favoriId(100L)
                .build();

        when(favoriService.checkFavori(TypeFavori.PLANTE, 1L)).thenReturn(response);

        ResponseEntity<PopulationFavoriCheckResponse> result = favoriController.checkFavori(TypeFavori.PLANTE, 1L);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody().isFavori()).isTrue();
    }

    @Test
    @DisplayName("GET /api/v1/population/favoris/count - 200 OK")
    void getFavoriCounts_200OK() {
        PopulationFavoriCountResponse response = PopulationFavoriCountResponse.builder()
                .total(5L)
                .totalPlantes(2L)
                .totalProduits(2L)
                .totalPharmacopees(1L)
                .build();

        when(favoriService.getFavoriCounts()).thenReturn(response);

        ResponseEntity<PopulationFavoriCountResponse> result = favoriController.getFavoriCounts();

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody().getTotal()).isEqualTo(5L);
    }

    @Test
    @DisplayName("GET /api/v1/population/favoris - 200 OK")
    void getMesFavoris_200OK() {
        PopulationFavoriItemResponse item = PopulationFavoriItemResponse.builder()
                .id(100L)
                .type(TypeFavori.PLANTE)
                .dateAjout(LocalDateTime.now())
                .build();

        Pageable pageable = PageRequest.of(0, 10);
        when(favoriService.getMesFavoris(eq(TypeFavori.PLANTE), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(item)));

        ResponseEntity<Page<PopulationFavoriItemResponse>> result = favoriController.getMesFavoris(TypeFavori.PLANTE, pageable);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody().getContent()).hasSize(1);
    }

    @Test
    @DisplayName("GET /api/v1/population/favoris/plantes - 200 OK")
    void getMesPlantesFavorites_200OK() {
        PopulationPlanteSummaryResponse item = PopulationPlanteSummaryResponse.builder()
                .id(1L)
                .nomScientifique("Artemisia annua")
                .build();

        Pageable pageable = PageRequest.of(0, 10);
        when(favoriService.getMesPlantesFavorites(eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(item)));

        ResponseEntity<Page<PopulationPlanteSummaryResponse>> result = favoriController.getMesPlantesFavorites(pageable);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody().getContent()).hasSize(1);
    }

    @Test
    @DisplayName("GET /api/v1/population/favoris/produits - 200 OK")
    void getMesProduitsFavoris_200OK() {
        PopulationProduitSummaryResponse item = PopulationProduitSummaryResponse.builder()
                .id(5L)
                .nom("Sirop d'Artemisia")
                .build();

        Pageable pageable = PageRequest.of(0, 10);
        when(favoriService.getMesProduitsFavoris(eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(item)));

        ResponseEntity<Page<PopulationProduitSummaryResponse>> result = favoriController.getMesProduitsFavoris(pageable);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody().getContent()).hasSize(1);
    }

    @Test
    @DisplayName("GET /api/v1/population/favoris/pharmacopees - 200 OK")
    void getMesPharmacopeesFavorites_200OK() {
        PopulationPharmacopeeSummaryResponse item = PopulationPharmacopeeSummaryResponse.builder()
                .id(3L)
                .nom("Pharmacie Mandé")
                .build();

        Pageable pageable = PageRequest.of(0, 10);
        when(favoriService.getMesPharmacopeesFavorites(eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(item)));

        ResponseEntity<Page<PopulationPharmacopeeSummaryResponse>> result = favoriController.getMesPharmacopeesFavorites(pageable);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody().getContent()).hasSize(1);
    }
}
