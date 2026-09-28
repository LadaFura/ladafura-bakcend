package com.pharmacopee.ladafura.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.pharmacopee.ladafura.controllers.population.PopulationPharmacopeeController;
import com.pharmacopee.ladafura.dto.population.pharmacopee.PopulationPharmacopeeDetailResponse;
import com.pharmacopee.ladafura.dto.population.pharmacopee.PopulationPharmacopeeLocalisationDto;
import com.pharmacopee.ladafura.dto.population.pharmacopee.PopulationPharmacopeeModeRetraitDto;
import com.pharmacopee.ladafura.dto.population.pharmacopee.PopulationPharmacopeeProduitItemResponse;
import com.pharmacopee.ladafura.dto.population.pharmacopee.PopulationPharmacopeeSummaryResponse;
import com.pharmacopee.ladafura.services.interfaces.IPopulationPharmacopeeService;

@ExtendWith(MockitoExtension.class)
class PopulationPharmacopeeControllerTest {

    @Mock
    private IPopulationPharmacopeeService pharmacopeeService;

    @InjectMocks
    private PopulationPharmacopeeController pharmacopeeController;

    @Test
    @DisplayName("GET /api/v1/population/pharmacopees - 200 OK")
    void listerPharmacopees_200OK() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<PopulationPharmacopeeSummaryResponse> page = new PageImpl<>(List.of(
                PopulationPharmacopeeSummaryResponse.builder()
                        .id(3L)
                        .nom("Pharmacie Mandé")
                        .region("Koulikoro")
                        .proposeLivraison(true)
                        .proposePickup(true)
                        .nombreProduits(10L)
                        .build()
        ));

        when(pharmacopeeService.listerPharmacopees("mandé", "Koulikoro", "Kati", "Siby", pageable))
                .thenReturn(page);

        ResponseEntity<Page<PopulationPharmacopeeSummaryResponse>> response =
                pharmacopeeController.listerPharmacopees("mandé", "Koulikoro", "Kati", "Siby", pageable);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).hasSize(1);
        assertThat(response.getBody().getContent().get(0).getNom()).isEqualTo("Pharmacie Mandé");

        verify(pharmacopeeService).listerPharmacopees("mandé", "Koulikoro", "Kati", "Siby", pageable);
    }

    @Test
    @DisplayName("GET /api/v1/population/pharmacopees/{id} - 200 OK")
    void getPharmacopeeDetail_200OK() {
        PopulationPharmacopeeDetailResponse detail = PopulationPharmacopeeDetailResponse.builder()
                .id(3L)
                .nom("Pharmacie Mandé")
                .telephone("+223 70 00 11 22")
                .email("contact@mande.ml")
                .localisation(PopulationPharmacopeeLocalisationDto.builder().commune("Siby").build())
                .modesRetrait(List.of(
                        PopulationPharmacopeeModeRetraitDto.builder().type("LIVRAISON").actif(true).frais(1500.0).build()
                ))
                .build();

        when(pharmacopeeService.getPharmacopeeDetail(3L)).thenReturn(detail);

        ResponseEntity<PopulationPharmacopeeDetailResponse> response = pharmacopeeController.getPharmacopeeDetail(3L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(3L);
        assertThat(response.getBody().getNom()).isEqualTo("Pharmacie Mandé");

        verify(pharmacopeeService).getPharmacopeeDetail(3L);
    }

    @Test
    @DisplayName("GET /api/v1/population/pharmacopees/{id}/produits - 200 OK")
    void getProduitsByPharmacopee_200OK() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<PopulationPharmacopeeProduitItemResponse> page = new PageImpl<>(List.of(
                PopulationPharmacopeeProduitItemResponse.builder()
                        .produitId(5L)
                        .nom("Tisane Kinkéliba")
                        .prix(2500.0)
                        .build()
        ));

        when(pharmacopeeService.getProduitsByPharmacopee(3L, true, pageable)).thenReturn(page);

        ResponseEntity<Page<PopulationPharmacopeeProduitItemResponse>> response =
                pharmacopeeController.getProduitsByPharmacopee(3L, true, pageable);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).hasSize(1);
        assertThat(response.getBody().getContent().get(0).getNom()).isEqualTo("Tisane Kinkéliba");

        verify(pharmacopeeService).getProduitsByPharmacopee(3L, true, pageable);
    }

    @Test
    @DisplayName("GET /api/v1/population/pharmacopees/{id}/modes-retrait - 200 OK")
    void getModesRetraitByPharmacopee_200OK() {
        List<PopulationPharmacopeeModeRetraitDto> modes = List.of(
                PopulationPharmacopeeModeRetraitDto.builder().id(1L).type("PICKUP").actif(true).frais(0.0).build()
        );

        when(pharmacopeeService.getModesRetraitByPharmacopee(3L)).thenReturn(modes);

        ResponseEntity<List<PopulationPharmacopeeModeRetraitDto>> response =
                pharmacopeeController.getModesRetraitByPharmacopee(3L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).getType()).isEqualTo("PICKUP");

        verify(pharmacopeeService).getModesRetraitByPharmacopee(3L);
    }
}
