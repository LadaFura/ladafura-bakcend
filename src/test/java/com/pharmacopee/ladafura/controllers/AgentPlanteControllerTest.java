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

import com.pharmacopee.ladafura.controllers.agent.AgentPlanteController;
import com.pharmacopee.ladafura.dto.agent.plante.AgentCreatePlanteRequest;
import com.pharmacopee.ladafura.dto.agent.plante.AgentPlanteAssociationResponse;
import com.pharmacopee.ladafura.dto.agent.plante.AgentPlanteResponse;
import com.pharmacopee.ladafura.enums.StatutPlante;
import com.pharmacopee.ladafura.services.interfaces.IAgentPlanteService;

@ExtendWith(MockitoExtension.class)
class AgentPlanteControllerTest {

    @Mock
    private IAgentPlanteService agentPlanteService;

    @InjectMocks
    private AgentPlanteController agentPlanteController;

    @Test
    @DisplayName("searchPlantes - Retourne statut 200 et la liste paginée de plantes")
    void searchPlantes_Success() {
        // GIVEN
        Pageable pageable = PageRequest.of(0, 10);
        AgentPlanteResponse responseDto = AgentPlanteResponse.builder()
                .id(1L)
                .nomScientifique("Combretum micranthum")
                .statut(StatutPlante.VALIDE)
                .build();
        Page<AgentPlanteResponse> page = new PageImpl<>(List.of(responseDto), pageable, 1);
        when(agentPlanteService.searchPlantes("kinkeliba", pageable)).thenReturn(page);

        // WHEN
        ResponseEntity<Page<AgentPlanteResponse>> response = agentPlanteController.searchPlantes("kinkeliba", pageable);

        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("getPlanteById - Retourne statut 200 et les détails de la plante")
    void getPlanteById_Success() {
        // GIVEN
        AgentPlanteResponse responseDto = AgentPlanteResponse.builder()
                .id(1L)
                .nomScientifique("Combretum micranthum")
                .build();
        when(agentPlanteService.getPlanteById(1L)).thenReturn(responseDto);

        // WHEN
        ResponseEntity<AgentPlanteResponse> response = agentPlanteController.getPlanteById(1L);

        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("createPlante - Retourne statut 201 Created")
    void createPlante_Success() {
        // GIVEN
        AgentCreatePlanteRequest request = AgentCreatePlanteRequest.builder()
                .nomScientifique("Ziziphus mauritiana")
                .build();
        AgentPlanteResponse responseDto = AgentPlanteResponse.builder()
                .id(2L)
                .nomScientifique("Ziziphus mauritiana")
                .statut(StatutPlante.BROUILLON)
                .build();
        when(agentPlanteService.createPlante(request)).thenReturn(responseDto);

        // WHEN
        ResponseEntity<AgentPlanteResponse> response = agentPlanteController.createPlante(request);

        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getNomScientifique()).isEqualTo("Ziziphus mauritiana");
    }

    @Test
    @DisplayName("associatePlanteToCollecte - Retourne statut 200 OK")
    void associatePlanteToCollecte_Success() {
        // GIVEN
        AgentPlanteAssociationResponse assoc = AgentPlanteAssociationResponse.builder()
                .collecteId(50L)
                .planteId(1L)
                .nomScientifique("Combretum micranthum")
                .build();
        when(agentPlanteService.associatePlanteToCollecte(50L, 1L)).thenReturn(assoc);

        // WHEN
        ResponseEntity<AgentPlanteAssociationResponse> response = agentPlanteController.associatePlanteToCollecte(50L, 1L);

        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCollecteId()).isEqualTo(50L);
    }

    @Test
    @DisplayName("dissociatePlanteFromCollecte - Retourne statut 204 No Content")
    void dissociatePlanteFromCollecte_Success() {
        // WHEN
        ResponseEntity<Void> response = agentPlanteController.dissociatePlanteFromCollecte(50L, 1L);

        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(agentPlanteService).dissociatePlanteFromCollecte(50L, 1L);
    }
}
