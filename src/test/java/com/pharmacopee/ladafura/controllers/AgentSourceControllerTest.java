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

import com.pharmacopee.ladafura.controllers.agent.AgentSourceController;
import com.pharmacopee.ladafura.dto.agent.source.AgentCreateSourceRequest;
import com.pharmacopee.ladafura.dto.agent.source.AgentSourceResponse;
import com.pharmacopee.ladafura.dto.agent.source.AgentUpdateSourceRequest;
import com.pharmacopee.ladafura.enums.Role;
import com.pharmacopee.ladafura.services.interfaces.IAgentSourceService;

@ExtendWith(MockitoExtension.class)
class AgentSourceControllerTest {

    @Mock
    private IAgentSourceService agentSourceService;

    @InjectMocks
    private AgentSourceController agentSourceController;

    @Test
    @DisplayName("POST /api/v1/agent/sources - 201 Created")
    void creerSource_Created() {
        AgentCreateSourceRequest request = AgentCreateSourceRequest.builder()
                .nom("Diarra")
                .prenom("Amadou")
                .specialite("Herboriste")
                .build();

        AgentSourceResponse responseDto = AgentSourceResponse.builder()
                .id(5L)
                .nom("Diarra")
                .prenom("Amadou")
                .nomComplet("Amadou Diarra")
                .estCompteUtilisateur(false)
                .build();

        when(agentSourceService.creerSource(request)).thenReturn(responseDto);

        ResponseEntity<AgentSourceResponse> response = agentSourceController.creerSource(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(5L);
        assertThat(response.getBody().isEstCompteUtilisateur()).isFalse();
    }

    @Test
    @DisplayName("PUT /api/v1/agent/sources/{id} - 200 OK")
    void modifierSource_Ok() {
        AgentUpdateSourceRequest request = AgentUpdateSourceRequest.builder()
                .specialite("Expert des plantes")
                .build();

        AgentSourceResponse responseDto = AgentSourceResponse.builder()
                .id(5L)
                .specialite("Expert des plantes")
                .build();

        when(agentSourceService.modifierSource(5L, request)).thenReturn(responseDto);

        ResponseEntity<AgentSourceResponse> response = agentSourceController.modifierSource(5L, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getSpecialite()).isEqualTo("Expert des plantes");
    }

    @Test
    @DisplayName("GET /api/v1/agent/sources/{id} - 200 OK")
    void getSourceById_Ok() {
        AgentSourceResponse responseDto = AgentSourceResponse.builder()
                .id(5L)
                .nomComplet("Amadou Diarra")
                .build();

        when(agentSourceService.getSourceById(5L)).thenReturn(responseDto);

        ResponseEntity<AgentSourceResponse> response = agentSourceController.getSourceById(5L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(5L);
    }

    @Test
    @DisplayName("GET /api/v1/agent/sources - 200 OK")
    void rechercherSources_Ok() {
        Pageable pageable = PageRequest.of(0, 20);
        AgentSourceResponse responseDto = AgentSourceResponse.builder().id(5L).nom("Diarra").build();
        Page<AgentSourceResponse> page = new PageImpl<>(List.of(responseDto), pageable, 1);

        when(agentSourceService.rechercherSources("Diarra", Role.HERBORISTE, pageable)).thenReturn(page);

        ResponseEntity<Page<AgentSourceResponse>> response = agentSourceController.rechercherSources("Diarra", Role.HERBORISTE, pageable);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("POST /api/v1/agent/collectes/{collecteId}/sources/{sourceId} - 200 OK")
    void associerSourceACollecte_Ok() {
        ResponseEntity<Void> response = agentSourceController.associerSourceACollecte(100L, 5L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(agentSourceService).associerSourceACollecte(100L, 5L);
    }

    @Test
    @DisplayName("DELETE /api/v1/agent/collectes/{collecteId}/source - 204 No Content")
    void dissocierSourceDeCollecte_NoContent() {
        ResponseEntity<Void> response = agentSourceController.dissocierSourceDeCollecte(100L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(agentSourceService).dissocierSourceDeCollecte(100L);
    }
}
