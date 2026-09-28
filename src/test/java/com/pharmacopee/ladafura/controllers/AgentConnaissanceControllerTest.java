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

import com.pharmacopee.ladafura.controllers.agent.AgentConnaissanceController;
import com.pharmacopee.ladafura.dto.agent.connaissance.AgentConnaissanceRequest;
import com.pharmacopee.ladafura.dto.agent.connaissance.AgentConnaissanceResponse;
import com.pharmacopee.ladafura.dto.agent.connaissance.AgentConnaissanceUpdateRequest;
import com.pharmacopee.ladafura.services.interfaces.IAgentConnaissanceService;

@ExtendWith(MockitoExtension.class)
class AgentConnaissanceControllerTest {

    @Mock
    private IAgentConnaissanceService agentConnaissanceService;

    @InjectMocks
    private AgentConnaissanceController agentConnaissanceController;

    @Test
    @DisplayName("POST /api/v1/agent/connaissances - 201 Created")
    void enregistrerConnaissance_Created() {
        AgentConnaissanceRequest request = AgentConnaissanceRequest.builder()
                .collecteId(100L)
                .planteId(1L)
                .usageRapporte("Traitement traditionnel des fièvres")
                .partieUtilisee("Feuilles")
                .preparation("Décoction")
                .build();

        AgentConnaissanceResponse responseDto = AgentConnaissanceResponse.builder()
                .id(20L)
                .collecteId(100L)
                .planteId(1L)
                .usageRapporte(request.getUsageRapporte())
                .partieUtilisee(request.getPartieUtilisee())
                .preuveScientifique(false)
                .typeInformation("CONNAISSANCE_TRADITIONNELLE")
                .build();

        when(agentConnaissanceService.enregistrerConnaissance(request)).thenReturn(responseDto);

        ResponseEntity<AgentConnaissanceResponse> response = agentConnaissanceController.enregistrerConnaissance(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(20L);
        assertThat(response.getBody().isPreuveScientifique()).isFalse();
    }

    @Test
    @DisplayName("PUT /api/v1/agent/connaissances/{id} - 200 OK")
    void modifierConnaissance_Ok() {
        AgentConnaissanceUpdateRequest request = AgentConnaissanceUpdateRequest.builder()
                .usageRapporte("Usage révisé")
                .partieUtilisee("Racines")
                .build();

        AgentConnaissanceResponse responseDto = AgentConnaissanceResponse.builder()
                .id(20L)
                .usageRapporte("Usage révisé")
                .partieUtilisee("Racines")
                .build();

        when(agentConnaissanceService.modifierConnaissance(20L, request)).thenReturn(responseDto);

        ResponseEntity<AgentConnaissanceResponse> response = agentConnaissanceController.modifierConnaissance(20L, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getUsageRapporte()).isEqualTo("Usage révisé");
    }

    @Test
    @DisplayName("GET /api/v1/agent/connaissances/{id} - 200 OK")
    void getConnaissanceById_Ok() {
        AgentConnaissanceResponse responseDto = AgentConnaissanceResponse.builder()
                .id(20L)
                .usageRapporte("Traitement traditionnel")
                .planteNomScientifique("Combretum micranthum")
                .build();

        when(agentConnaissanceService.getConnaissanceById(20L)).thenReturn(responseDto);

        ResponseEntity<AgentConnaissanceResponse> response = agentConnaissanceController.getConnaissanceById(20L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(20L);
    }

    @Test
    @DisplayName("GET /api/v1/agent/collectes/{collecteId}/connaissances - 200 OK")
    void getConnaissancesByCollecte_Ok() {
        AgentConnaissanceResponse r1 = AgentConnaissanceResponse.builder().id(1L).usageRapporte("U1").build();
        when(agentConnaissanceService.getConnaissancesByCollecte(100L)).thenReturn(List.of(r1));

        ResponseEntity<List<AgentConnaissanceResponse>> response = agentConnaissanceController.getConnaissancesByCollecte(100L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
    }

    @Test
    @DisplayName("DELETE /api/v1/agent/connaissances/{id} - 204 No Content")
    void supprimerConnaissance_NoContent() {
        ResponseEntity<Void> response = agentConnaissanceController.supprimerConnaissance(20L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(agentConnaissanceService).supprimerConnaissance(20L);
    }
}
