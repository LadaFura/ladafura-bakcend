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

import com.pharmacopee.ladafura.controllers.agent.AgentLocalisationController;
import com.pharmacopee.ladafura.dto.agent.localisation.AgentLocalisationRequest;
import com.pharmacopee.ladafura.dto.agent.localisation.AgentLocalisationResponse;
import com.pharmacopee.ladafura.services.interfaces.IAgentLocalisationService;

@ExtendWith(MockitoExtension.class)
class AgentLocalisationControllerTest {

    @Mock
    private IAgentLocalisationService agentLocalisationService;

    @InjectMocks
    private AgentLocalisationController agentLocalisationController;

    @Test
    @DisplayName("POST /api/v1/agent/collectes/{collecteId}/localisation - 201 Created")
    void enregistrerLocalisation_Created() {
        AgentLocalisationRequest request = AgentLocalisationRequest.builder()
                .region("Sikasso")
                .cercle("Koutiala")
                .commune("Finkolo")
                .localite("Finkolo Centre")
                .latitude(12.3812)
                .longitude(-5.4590)
                .build();

        AgentLocalisationResponse responseDto = AgentLocalisationResponse.builder()
                .collecteId(100L)
                .localisationId(1L)
                .region("Sikasso")
                .cercle("Koutiala")
                .commune("Finkolo")
                .localite("Finkolo Centre")
                .build();

        when(agentLocalisationService.saveOrUpdateLocalisation(100L, request)).thenReturn(responseDto);

        ResponseEntity<AgentLocalisationResponse> response = agentLocalisationController.enregistrerLocalisation(100L, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getLocalisationId()).isEqualTo(1L);
        assertThat(response.getBody().getRegion()).isEqualTo("Sikasso");
    }

    @Test
    @DisplayName("PUT /api/v1/agent/collectes/{collecteId}/localisation - 200 OK")
    void modifierLocalisation_Ok() {
        AgentLocalisationRequest request = AgentLocalisationRequest.builder()
                .region("Sikasso")
                .cercle("Koutiala")
                .commune("Finkolo")
                .localite("Finkolo Modifié")
                .build();

        AgentLocalisationResponse responseDto = AgentLocalisationResponse.builder()
                .collecteId(100L)
                .localisationId(1L)
                .localite("Finkolo Modifié")
                .build();

        when(agentLocalisationService.saveOrUpdateLocalisation(100L, request)).thenReturn(responseDto);

        ResponseEntity<AgentLocalisationResponse> response = agentLocalisationController.modifierLocalisation(100L, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getLocalite()).isEqualTo("Finkolo Modifié");
    }

    @Test
    @DisplayName("GET /api/v1/agent/collectes/{collecteId}/localisation - 200 OK")
    void getLocalisationByCollecte_Ok() {
        AgentLocalisationResponse responseDto = AgentLocalisationResponse.builder()
                .collecteId(100L)
                .localisationId(1L)
                .region("Sikasso")
                .build();

        when(agentLocalisationService.getLocalisationByCollecte(100L)).thenReturn(responseDto);

        ResponseEntity<AgentLocalisationResponse> response = agentLocalisationController.getLocalisationByCollecte(100L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getRegion()).isEqualTo("Sikasso");
    }

    @Test
    @DisplayName("DELETE /api/v1/agent/collectes/{collecteId}/localisation - 204 No Content")
    void supprimerLocalisation_NoContent() {
        ResponseEntity<Void> response = agentLocalisationController.supprimerLocalisation(100L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(agentLocalisationService).supprimerLocalisation(100L);
    }

    @Test
    @DisplayName("GET /api/v1/agent/localisations - 200 OK")
    void rechercherLocalisations_Ok() {
        Pageable pageable = PageRequest.of(0, 20);
        AgentLocalisationResponse responseDto = AgentLocalisationResponse.builder().localisationId(1L).region("Sikasso").build();
        Page<AgentLocalisationResponse> page = new PageImpl<>(List.of(responseDto), pageable, 1);

        when(agentLocalisationService.rechercherLocalisations("Sikasso", pageable)).thenReturn(page);

        ResponseEntity<Page<AgentLocalisationResponse>> response = agentLocalisationController.rechercherLocalisations("Sikasso", pageable);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getTotalElements()).isEqualTo(1);
    }
}
