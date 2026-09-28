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

import com.pharmacopee.ladafura.controllers.agent.AgentNomVernaculaireController;
import com.pharmacopee.ladafura.dto.agent.vernaculaire.AgentNomVernaculaireRequest;
import com.pharmacopee.ladafura.dto.agent.vernaculaire.AgentNomVernaculaireResponse;
import com.pharmacopee.ladafura.services.interfaces.IAgentNomVernaculaireService;

@ExtendWith(MockitoExtension.class)
class AgentNomVernaculaireControllerTest {

    @Mock
    private IAgentNomVernaculaireService service;

    @InjectMocks
    private AgentNomVernaculaireController controller;

    @Test
    @DisplayName("getNomsByPlanteId - Retourne statut 200 et la liste")
    void getNomsByPlanteId_Success() {
        // GIVEN
        AgentNomVernaculaireResponse responseDto = AgentNomVernaculaireResponse.builder()
                .id(1L)
                .nom("Kinkéliba")
                .langue("Bambara")
                .build();
        when(service.getNomsByPlanteId(10L)).thenReturn(List.of(responseDto));

        // WHEN
        ResponseEntity<List<AgentNomVernaculaireResponse>> response = controller.getNomsByPlanteId(10L);

        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
    }

    @Test
    @DisplayName("addNomVernaculaire - Retourne statut 201 Created")
    void addNomVernaculaire_Success() {
        // GIVEN
        AgentNomVernaculaireRequest request = AgentNomVernaculaireRequest.builder()
                .nom("Sekeu")
                .langue("Soninké")
                .pays("Mali")
                .build();
        AgentNomVernaculaireResponse responseDto = AgentNomVernaculaireResponse.builder()
                .id(2L)
                .nom("Sekeu")
                .langue("Soninké")
                .build();
        when(service.addNomVernaculaire(10L, request)).thenReturn(responseDto);

        // WHEN
        ResponseEntity<AgentNomVernaculaireResponse> response = controller.addNomVernaculaire(10L, request);

        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getNom()).isEqualTo("Sekeu");
    }

    @Test
    @DisplayName("updateNomVernaculaire - Retourne statut 200 OK")
    void updateNomVernaculaire_Success() {
        // GIVEN
        AgentNomVernaculaireRequest request = AgentNomVernaculaireRequest.builder()
                .nom("Kinkeliba")
                .langue("Bambara")
                .build();
        AgentNomVernaculaireResponse responseDto = AgentNomVernaculaireResponse.builder()
                .id(1L)
                .nom("Kinkeliba")
                .build();
        when(service.updateNomVernaculaire(10L, 1L, request)).thenReturn(responseDto);

        // WHEN
        ResponseEntity<AgentNomVernaculaireResponse> response = controller.updateNomVernaculaire(10L, 1L, request);

        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    @DisplayName("deleteNomVernaculaire - Retourne statut 204 No Content")
    void deleteNomVernaculaire_Success() {
        // WHEN
        ResponseEntity<Void> response = controller.deleteNomVernaculaire(10L, 1L);

        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(service).deleteNomVernaculaire(10L, 1L);
    }
}
