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

import com.pharmacopee.ladafura.controllers.population.PopulationCarteController;
import com.pharmacopee.ladafura.dto.population.cartographie.PopulationCarteDetailPharmacopeeResponse;
import com.pharmacopee.ladafura.dto.population.cartographie.PopulationCartePharmacopeeItem;
import com.pharmacopee.ladafura.dto.population.cartographie.PopulationCarteProduitItem;
import com.pharmacopee.ladafura.dto.population.pharmacopee.PopulationPharmacopeeLocalisationDto;
import com.pharmacopee.ladafura.dto.population.pharmacopee.PopulationPharmacopeeModeRetraitDto;
import com.pharmacopee.ladafura.services.interfaces.IPopulationCarteService;

@ExtendWith(MockitoExtension.class)
class PopulationCarteControllerTest {

    @Mock
    private IPopulationCarteService carteService;

    @InjectMocks
    private PopulationCarteController carteController;

    @Test
    @DisplayName("GET /api/v1/population/carte/pharmacopees - 200 OK")
    void getPharmacopeesSurCarte_200OK() {
        List<PopulationCartePharmacopeeItem> items = List.of(
                PopulationCartePharmacopeeItem.builder()
                        .pharmacopeeId(1L)
                        .nom("Pharmacie Mandé")
                        .latitude(12.38)
                        .longitude(-8.33)
                        .distanceKm(3.5)
                        .build()
        );

        when(carteService.getPharmacopeesSurCarte("mandé", "Koulikoro", "Kati", "Siby", 12.38, -8.33, 20.0))
                .thenReturn(items);

        ResponseEntity<List<PopulationCartePharmacopeeItem>> response =
                carteController.getPharmacopeesSurCarte("mandé", "Koulikoro", "Kati", "Siby", 12.38, -8.33, 20.0);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).getNom()).isEqualTo("Pharmacie Mandé");

        verify(carteService).getPharmacopeesSurCarte("mandé", "Koulikoro", "Kati", "Siby", 12.38, -8.33, 20.0);
    }

    @Test
    @DisplayName("GET /api/v1/population/carte/produit - 200 OK")
    void localiserProduitSurCarte_200OK() {
        List<PopulationCarteProduitItem> items = List.of(
                PopulationCarteProduitItem.builder()
                        .pharmacopeeId(1L)
                        .nomPharmacopee("Pharmacie Mandé")
                        .produitId(5L)
                        .nomProduit("Tisane Kinkéliba")
                        .latitude(12.38)
                        .longitude(-8.33)
                        .prix(2500.0)
                        .build()
        );

        when(carteService.localiserProduitSurCarte(5L, "tisane", 12.38, -8.33, 25.0, true))
                .thenReturn(items);

        ResponseEntity<List<PopulationCarteProduitItem>> response =
                carteController.localiserProduitSurCarte(5L, "tisane", 12.38, -8.33, 25.0, true);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).getNomProduit()).isEqualTo("Tisane Kinkéliba");

        verify(carteService).localiserProduitSurCarte(5L, "tisane", 12.38, -8.33, 25.0, true);
    }

    @Test
    @DisplayName("GET /api/v1/population/carte/pharmacopees/{id} - 200 OK")
    void getPharmacopeeCarteDetail_200OK() {
        PopulationCarteDetailPharmacopeeResponse detail = PopulationCarteDetailPharmacopeeResponse.builder()
                .pharmacopeeId(1L)
                .nom("Pharmacie Mandé")
                .distanceKm(4.2)
                .localisation(PopulationPharmacopeeLocalisationDto.builder().commune("Siby").build())
                .modesRetrait(List.of(
                        PopulationPharmacopeeModeRetraitDto.builder().type("PICKUP").actif(true).frais(0.0).build()
                ))
                .build();

        when(carteService.getPharmacopeeCarteDetail(1L, 12.38, -8.33)).thenReturn(detail);

        ResponseEntity<PopulationCarteDetailPharmacopeeResponse> response =
                carteController.getPharmacopeeCarteDetail(1L, 12.38, -8.33);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getPharmacopeeId()).isEqualTo(1L);
        assertThat(response.getBody().getNom()).isEqualTo("Pharmacie Mandé");
        assertThat(response.getBody().getDistanceKm()).isEqualTo(4.2);

        verify(carteService).getPharmacopeeCarteDetail(1L, 12.38, -8.33);
    }
}
