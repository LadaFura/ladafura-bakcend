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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.pharmacopee.ladafura.controllers.population.PopulationPaiementController;
import com.pharmacopee.ladafura.dto.population.paiement.PopulationMethodePaiementInfoDto;
import com.pharmacopee.ladafura.dto.population.paiement.PopulationPaiementResponse;
import com.pharmacopee.ladafura.dto.population.paiement.PopulationProcessPaiementRequest;
import com.pharmacopee.ladafura.enums.MethodePaiement;
import com.pharmacopee.ladafura.enums.StatutPaiement;
import com.pharmacopee.ladafura.services.interfaces.IPopulationPaiementService;

@ExtendWith(MockitoExtension.class)
class PopulationPaiementControllerTest {

    @Mock
    private IPopulationPaiementService paiementService;

    @InjectMocks
    private PopulationPaiementController paiementController;

    @Test
    @DisplayName("GET /api/v1/population/paiements/methodes - 200 OK")
    void getMethodesPaiement_200OK() {
        List<PopulationMethodePaiementInfoDto> methodes = List.of(
                PopulationMethodePaiementInfoDto.builder()
                        .code(MethodePaiement.MOBILE_MONEY)
                        .libelle("Mobile Money")
                        .disponible(true)
                        .build()
        );

        when(paiementService.getMethodesPaiement()).thenReturn(methodes);

        ResponseEntity<List<PopulationMethodePaiementInfoDto>> response = paiementController.getMethodesPaiement();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(1);
        verify(paiementService).getMethodesPaiement();
    }

    @Test
    @DisplayName("POST /api/v1/population/paiements - 200 OK")
    void payerCommande_200OK() {
        PopulationProcessPaiementRequest request = PopulationProcessPaiementRequest.builder()
                .commandeId(1L)
                .methode(MethodePaiement.MOBILE_MONEY)
                .build();

        PopulationPaiementResponse responseDto = PopulationPaiementResponse.builder()
                .id(10L)
                .reference("PAY-OM-001")
                .statut(StatutPaiement.REUSSI)
                .montant(6500.0)
                .build();

        when(paiementService.payerCommande(request)).thenReturn(responseDto);

        ResponseEntity<PopulationPaiementResponse> response = paiementController.payerCommande(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getReference()).isEqualTo("PAY-OM-001");
        verify(paiementService).payerCommande(request);
    }

    @Test
    @DisplayName("GET /api/v1/population/paiements/commandes/{commandeId} - 200 OK")
    void getPaiementByCommande_200OK() {
        PopulationPaiementResponse responseDto = PopulationPaiementResponse.builder()
                .id(10L)
                .reference("PAY-OM-001")
                .statut(StatutPaiement.REUSSI)
                .build();

        when(paiementService.getPaiementByCommande(1L)).thenReturn(responseDto);

        ResponseEntity<PopulationPaiementResponse> response = paiementController.getPaiementByCommande(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(10L);
        verify(paiementService).getPaiementByCommande(1L);
    }

    @Test
    @DisplayName("GET /api/v1/population/paiements - 200 OK")
    void getHistoriquePaiements_200OK() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<PopulationPaiementResponse> page = new PageImpl<>(List.of(
                PopulationPaiementResponse.builder()
                        .id(10L)
                        .reference("PAY-OM-001")
                        .statut(StatutPaiement.REUSSI)
                        .build()
        ));

        when(paiementService.getHistoriquePaiements(pageable)).thenReturn(page);

        ResponseEntity<Page<PopulationPaiementResponse>> response = paiementController.getHistoriquePaiements(pageable);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getTotalElements()).isEqualTo(1);
        verify(paiementService).getHistoriquePaiements(pageable);
    }
}
