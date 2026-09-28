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

import com.pharmacopee.ladafura.controllers.population.PopulationRetraitController;
import com.pharmacopee.ladafura.dto.population.retrait.PopulationEstimationRetraitRequest;
import com.pharmacopee.ladafura.dto.population.retrait.PopulationEstimationRetraitResponse;
import com.pharmacopee.ladafura.dto.population.retrait.PopulationModeRetraitOptionDto;
import com.pharmacopee.ladafura.dto.population.retrait.PopulationPharmacopeeRetraitOptionsResponse;
import com.pharmacopee.ladafura.enums.TypeModeRetrait;
import com.pharmacopee.ladafura.services.interfaces.IPopulationRetraitService;

@ExtendWith(MockitoExtension.class)
class PopulationRetraitControllerTest {

    @Mock
    private IPopulationRetraitService retraitService;

    @InjectMocks
    private PopulationRetraitController retraitController;

    @Test
    @DisplayName("GET /api/v1/population/retrait/pharmacopees/{pharmacopeeId} - 200 OK")
    void getOptionsRetraitPharmacopee_200OK() {
        PopulationPharmacopeeRetraitOptionsResponse responseDto = PopulationPharmacopeeRetraitOptionsResponse.builder()
                .pharmacopeeId(1L)
                .nomPharmacopee("Pharmacie Mandé")
                .proposeLivraison(true)
                .fraisLivraison(1500.0)
                .proposePickup(true)
                .options(List.of(
                        PopulationModeRetraitOptionDto.builder()
                                .id(10L)
                                .type(TypeModeRetrait.LIVRAISON)
                                .libelle("Livraison à domicile")
                                .actif(true)
                                .frais(1500.0)
                                .build()
                ))
                .build();

        when(retraitService.getOptionsRetraitPharmacopee(1L)).thenReturn(responseDto);

        ResponseEntity<PopulationPharmacopeeRetraitOptionsResponse> response =
                retraitController.getOptionsRetraitPharmacopee(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getNomPharmacopee()).isEqualTo("Pharmacie Mandé");
        assertThat(response.getBody().getProposeLivraison()).isTrue();

        verify(retraitService).getOptionsRetraitPharmacopee(1L);
    }

    @Test
    @DisplayName("POST /api/v1/population/retrait/estimer - 200 OK")
    void estimerOptionRetrait_200OK() {
        PopulationEstimationRetraitRequest request = PopulationEstimationRetraitRequest.builder()
                .pharmacopeeId(1L)
                .type(TypeModeRetrait.PICKUP)
                .build();

        PopulationEstimationRetraitResponse responseDto = PopulationEstimationRetraitResponse.builder()
                .pharmacopeeId(1L)
                .nomPharmacopee("Pharmacie Mandé")
                .type(TypeModeRetrait.PICKUP)
                .eligible(true)
                .frais(0.0)
                .gratuit(true)
                .message("Retrait gratuit en officine")
                .build();

        when(retraitService.estimerOptionRetrait(request)).thenReturn(responseDto);

        ResponseEntity<PopulationEstimationRetraitResponse> response =
                retraitController.estimerOptionRetrait(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getEligible()).isTrue();
        assertThat(response.getBody().getFrais()).isEqualTo(0.0);

        verify(retraitService).estimerOptionRetrait(request);
    }
}
