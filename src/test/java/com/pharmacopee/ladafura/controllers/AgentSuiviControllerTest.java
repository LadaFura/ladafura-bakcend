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

import com.pharmacopee.ladafura.controllers.agent.AgentSuiviController;
import com.pharmacopee.ladafura.dto.agent.collecte.AgentCollecteSummaryResponse;
import com.pharmacopee.ladafura.dto.agent.suivi.AgentCollecteRejetDetailResponse;
import com.pharmacopee.ladafura.dto.agent.suivi.AgentSuiviStatsResponse;
import com.pharmacopee.ladafura.enums.StatutCollecte;
import com.pharmacopee.ladafura.services.interfaces.IAgentSuiviCollecteService;

@ExtendWith(MockitoExtension.class)
class AgentSuiviControllerTest {

    @Mock
    private IAgentSuiviCollecteService suiviCollecteService;

    @InjectMocks
    private AgentSuiviController agentSuiviController;

    @Test
    @DisplayName("GET /api/v1/agent/suivi/stats - 200 OK")
    void getStatsSuivi_200OK() {
        AgentSuiviStatsResponse stats = AgentSuiviStatsResponse.builder()
                .total(15L)
                .brouillons(2L)
                .soumises(3L)
                .enExamen(2L)
                .validees(6L)
                .rejetees(2L)
                .necessitantCorrection(2L)
                .tauxValidation(75.0)
                .build();

        when(suiviCollecteService.getStatsSuivi()).thenReturn(stats);

        ResponseEntity<AgentSuiviStatsResponse> response = agentSuiviController.getStatsSuivi();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getTotal()).isEqualTo(15L);
        assertThat(response.getBody().getTauxValidation()).isEqualTo(75.0);
        verify(suiviCollecteService).getStatsSuivi();
    }

    @Test
    @DisplayName("GET /api/v1/agent/suivi/brouillons - 200 OK")
    void getBrouillons_200OK() {
        AgentCollecteSummaryResponse item = AgentCollecteSummaryResponse.builder()
                .id(1L)
                .statut(StatutCollecte.BROUILLON)
                .build();
        Page<AgentCollecteSummaryResponse> page = new PageImpl<>(List.of(item));

        when(suiviCollecteService.getBrouillons(any(Pageable.class))).thenReturn(page);

        ResponseEntity<Page<AgentCollecteSummaryResponse>> response = agentSuiviController.getBrouillons(PageRequest.of(0, 10));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent().get(0).getStatut()).isEqualTo(StatutCollecte.BROUILLON);
    }

    @Test
    @DisplayName("GET /api/v1/agent/suivi/rejetees - 200 OK")
    void getRejetees_200OK() {
        AgentCollecteSummaryResponse item = AgentCollecteSummaryResponse.builder()
                .id(2L)
                .statut(StatutCollecte.REJETEE)
                .motifRejet("Informations incomplètes")
                .build();
        Page<AgentCollecteSummaryResponse> page = new PageImpl<>(List.of(item));

        when(suiviCollecteService.getRejetees(any(Pageable.class))).thenReturn(page);

        ResponseEntity<Page<AgentCollecteSummaryResponse>> response = agentSuiviController.getRejetees(PageRequest.of(0, 10));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getContent().get(0).getStatut()).isEqualTo(StatutCollecte.REJETEE);
    }

    @Test
    @DisplayName("GET /api/v1/agent/suivi/{collecteId}/motif-rejet - 200 OK")
    void getDetailRejet_200OK() {
        AgentCollecteRejetDetailResponse detail = AgentCollecteRejetDetailResponse.builder()
                .collecteId(100L)
                .statut(StatutCollecte.REJETEE)
                .motifRejet("Photo inexploitable")
                .modifiable(true)
                .guideCorrection(List.of("1. Consulter le motif", "2. Corriger la fiche"))
                .build();

        when(suiviCollecteService.getDetailRejet(100L)).thenReturn(detail);

        ResponseEntity<AgentCollecteRejetDetailResponse> response = agentSuiviController.getDetailRejet(100L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCollecteId()).isEqualTo(100L);
        assertThat(response.getBody().getMotifRejet()).isEqualTo("Photo inexploitable");
        assertThat(response.getBody().isModifiable()).isTrue();
    }

    @Test
    @DisplayName("GET /api/v1/agent/suivi - Recherche multi-critères 200 OK")
    void searchSuivi_200OK() {
        AgentCollecteSummaryResponse item = AgentCollecteSummaryResponse.builder()
                .id(1L)
                .description("Mission Sikasso")
                .statut(StatutCollecte.VALIDEE)
                .build();
        Page<AgentCollecteSummaryResponse> page = new PageImpl<>(List.of(item));

        when(suiviCollecteService.searchSuiviCollectes(eq(StatutCollecte.VALIDEE), eq("Sikasso"), any(Pageable.class)))
                .thenReturn(page);

        ResponseEntity<Page<AgentCollecteSummaryResponse>> response = agentSuiviController.searchSuivi(
                StatutCollecte.VALIDEE, "Sikasso", PageRequest.of(0, 10));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getTotalElements()).isEqualTo(1);
    }
}
