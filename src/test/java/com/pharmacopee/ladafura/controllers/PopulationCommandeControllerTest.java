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

import com.pharmacopee.ladafura.controllers.population.PopulationCommandeController;
import com.pharmacopee.ladafura.dto.population.commande.PopulationCommandeDetailResponse;
import com.pharmacopee.ladafura.dto.population.commande.PopulationCommandeRecapitulatifRequest;
import com.pharmacopee.ladafura.dto.population.commande.PopulationCommandeRecapitulatifResponse;
import com.pharmacopee.ladafura.dto.population.commande.PopulationCommandeStatutResponse;
import com.pharmacopee.ladafura.dto.population.commande.PopulationCommandeSummaryResponse;
import com.pharmacopee.ladafura.dto.population.commande.PopulationCreateCommandeRequest;
import com.pharmacopee.ladafura.enums.MethodePaiement;
import com.pharmacopee.ladafura.enums.StatutCommande;
import com.pharmacopee.ladafura.services.interfaces.IPopulationCommandeService;

@ExtendWith(MockitoExtension.class)
class PopulationCommandeControllerTest {

    @Mock
    private IPopulationCommandeService commandeService;

    @InjectMocks
    private PopulationCommandeController commandeController;

    @Test
    @DisplayName("POST /api/v1/population/commandes/recapitulatif - 200 OK")
    void getRecapitulatif_200OK() {
        PopulationCommandeRecapitulatifRequest request = PopulationCommandeRecapitulatifRequest.builder()
                .pharmacopeeId(1L)
                .modeRetraitId(10L)
                .build();

        PopulationCommandeRecapitulatifResponse responseDto = PopulationCommandeRecapitulatifResponse.builder()
                .pharmacopeeId(1L)
                .nomPharmacopee("Pharmacie Mandé")
                .modeRetrait("LIVRAISON")
                .fraisLivraison(1500.0)
                .totalProduits(5000.0)
                .montantTotal(6500.0)
                .nombreArticles(2)
                .build();

        when(commandeService.getRecapitulatif(request)).thenReturn(responseDto);

        ResponseEntity<PopulationCommandeRecapitulatifResponse> response = commandeController.getRecapitulatif(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMontantTotal()).isEqualTo(6500.0);
        verify(commandeService).getRecapitulatif(request);
    }

    @Test
    @DisplayName("POST /api/v1/population/commandes - 201 Created")
    void passerCommande_201Created() {
        PopulationCreateCommandeRequest request = PopulationCreateCommandeRequest.builder()
                .pharmacopeeId(1L)
                .modeRetraitId(10L)
                .adresseLivraison("Bamako")
                .methode(MethodePaiement.MOBILE_MONEY)
                .build();

        PopulationCommandeDetailResponse responseDto = PopulationCommandeDetailResponse.builder()
                .id(100L)
                .numero("CMD-2026-001")
                .statut(StatutCommande.CONFIRMEE)
                .montantTotal(6500.0)
                .build();

        when(commandeService.passerCommande(request)).thenReturn(responseDto);

        ResponseEntity<PopulationCommandeDetailResponse> response = commandeController.passerCommande(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(100L);
        assertThat(response.getBody().getNumero()).isEqualTo("CMD-2026-001");
        verify(commandeService).passerCommande(request);
    }

    @Test
    @DisplayName("GET /api/v1/population/commandes - 200 OK")
    void getHistorique_200OK() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<PopulationCommandeSummaryResponse> page = new PageImpl<>(List.of(
                PopulationCommandeSummaryResponse.builder()
                        .id(1L)
                        .numero("CMD-2026-001")
                        .statut(StatutCommande.EN_ATTENTE)
                        .montantTotal(5000.0)
                        .build()
        ));

        when(commandeService.getHistoriqueCommandes(StatutCommande.EN_ATTENTE, pageable)).thenReturn(page);

        ResponseEntity<Page<PopulationCommandeSummaryResponse>> response =
                commandeController.getHistorique(StatutCommande.EN_ATTENTE, pageable);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getTotalElements()).isEqualTo(1);
        verify(commandeService).getHistoriqueCommandes(StatutCommande.EN_ATTENTE, pageable);
    }

    @Test
    @DisplayName("GET /api/v1/population/commandes/{id} - 200 OK")
    void getCommandeDetail_200OK() {
        PopulationCommandeDetailResponse responseDto = PopulationCommandeDetailResponse.builder()
                .id(1L)
                .numero("CMD-2026-001")
                .statut(StatutCommande.EN_ATTENTE)
                .build();

        when(commandeService.getCommandeDetail(1L)).thenReturn(responseDto);

        ResponseEntity<PopulationCommandeDetailResponse> response = commandeController.getCommandeDetail(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(1L);
        verify(commandeService).getCommandeDetail(1L);
    }

    @Test
    @DisplayName("GET /api/v1/population/commandes/{id}/statut - 200 OK")
    void getCommandeStatut_200OK() {
        PopulationCommandeStatutResponse responseDto = PopulationCommandeStatutResponse.builder()
                .id(1L)
                .numero("CMD-2026-001")
                .statut(StatutCommande.EN_ATTENTE)
                .annulable(true)
                .message("Commande en attente de confirmation")
                .build();

        when(commandeService.getCommandeStatut(1L)).thenReturn(responseDto);

        ResponseEntity<PopulationCommandeStatutResponse> response = commandeController.getCommandeStatut(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).contains("en attente");
        verify(commandeService).getCommandeStatut(1L);
    }

    @Test
    @DisplayName("POST /api/v1/population/commandes/{id}/annuler - 200 OK")
    void annulerCommande_200OK() {
        PopulationCommandeDetailResponse responseDto = PopulationCommandeDetailResponse.builder()
                .id(1L)
                .numero("CMD-2026-001")
                .statut(StatutCommande.ANNULEE)
                .build();

        when(commandeService.annulerCommande(1L)).thenReturn(responseDto);

        ResponseEntity<PopulationCommandeDetailResponse> response = commandeController.annulerCommande(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatut()).isEqualTo(StatutCommande.ANNULEE);
        verify(commandeService).annulerCommande(1L);
    }
}
