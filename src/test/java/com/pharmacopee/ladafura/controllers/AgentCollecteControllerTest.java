package com.pharmacopee.ladafura.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
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

import com.pharmacopee.ladafura.controllers.agent.AgentCollecteController;
import com.pharmacopee.ladafura.dto.agent.collecte.AgentCollecteDetailResponse;
import com.pharmacopee.ladafura.dto.agent.collecte.AgentCollecteSummaryResponse;
import com.pharmacopee.ladafura.dto.agent.collecte.AgentCreateCollecteRequest;
import com.pharmacopee.ladafura.dto.agent.collecte.AgentUpdateCollecteRequest;
import com.pharmacopee.ladafura.enums.StatutCollecte;
import com.pharmacopee.ladafura.services.interfaces.IAgentCollecteService;

@ExtendWith(MockitoExtension.class)
class AgentCollecteControllerTest {

    @Mock
    private IAgentCollecteService collecteService;

    @InjectMocks
    private AgentCollecteController collecteController;

    @Test
    @DisplayName("getMyCollectes - Retourne statut 200 et la liste paginée")
    void getMyCollectes_Success() {
        // GIVEN
        Pageable pageable = PageRequest.of(0, 10);
        AgentCollecteSummaryResponse summary = AgentCollecteSummaryResponse.builder()
                .id(1L)
                .statut(StatutCollecte.BROUILLON)
                .build();
        Page<AgentCollecteSummaryResponse> page = new PageImpl<>(List.of(summary), pageable, 1);
        when(collecteService.getMyCollectes(StatutCollecte.BROUILLON, pageable)).thenReturn(page);

        // WHEN
        ResponseEntity<Page<AgentCollecteSummaryResponse>> response =
                collecteController.getMyCollectes(StatutCollecte.BROUILLON, pageable);

        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("createCollecte - Retourne statut 201 Created")
    void createCollecte_Success() {
        // GIVEN
        AgentCreateCollecteRequest request = AgentCreateCollecteRequest.builder()
                .description("Test collecte")
                .soumettre(false)
                .build();
        AgentCollecteDetailResponse detail = AgentCollecteDetailResponse.builder()
                .id(1L)
                .statut(StatutCollecte.BROUILLON)
                .build();
        when(collecteService.createCollecte(request)).thenReturn(detail);

        // WHEN
        ResponseEntity<AgentCollecteDetailResponse> response = collecteController.createCollecte(request);

        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("submitCollecte - Retourne statut 200 OK avec nouveau statut SOUMISE")
    void submitCollecte_Success() {
        // GIVEN
        AgentCollecteDetailResponse detail = AgentCollecteDetailResponse.builder()
                .id(1L)
                .statut(StatutCollecte.SOUMISE)
                .dateSoumission(LocalDateTime.now())
                .build();
        when(collecteService.submitCollecte(1L)).thenReturn(detail);

        // WHEN
        ResponseEntity<AgentCollecteDetailResponse> response = collecteController.submitCollecte(1L);

        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatut()).isEqualTo(StatutCollecte.SOUMISE);
    }

    @Test
    @DisplayName("deleteDraft - Retourne statut 204 No Content")
    void deleteDraft_Success() {
        // WHEN
        ResponseEntity<Void> response = collecteController.deleteDraft(1L);

        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(collecteService).deleteDraft(1L);
    }
}
