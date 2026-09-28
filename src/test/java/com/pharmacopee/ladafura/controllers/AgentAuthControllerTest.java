package com.pharmacopee.ladafura.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.pharmacopee.ladafura.controllers.agent.AgentAuthController;
import com.pharmacopee.ladafura.dto.agent.auth.AgentAuthResponse;
import com.pharmacopee.ladafura.enums.Role;
import com.pharmacopee.ladafura.enums.StatutUtilisateur;
import com.pharmacopee.ladafura.services.interfaces.IAgentAuthService;

@ExtendWith(MockitoExtension.class)
class AgentAuthControllerTest {

    @Mock
    private IAgentAuthService agentAuthService;

    @InjectMocks
    private AgentAuthController agentAuthController;

    @Test
    @DisplayName("getMe - Retourne statut 200 et les informations d'authentification de l'agent")
    void getMe_Success() {
        // GIVEN
        AgentAuthResponse responseDto = AgentAuthResponse.builder()
                .id(1L)
                .matricule("AGT-2026-001")
                .zoneCouverture("Sikasso")
                .nom("Coulibaly")
                .prenom("Oumar")
                .email("oumar@ladafura.ml")
                .telephone("+223 70 00 00 01")
                .role(Role.AGENT_COLLECTE)
                .statut(StatutUtilisateur.ACTIF)
                .firebaseUid("uid-agent-1")
                .dateCreation(LocalDateTime.now())
                .build();

        when(agentAuthService.getMe()).thenReturn(responseDto);

        // WHEN
        ResponseEntity<AgentAuthResponse> response = agentAuthController.getMe();

        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMatricule()).isEqualTo("AGT-2026-001");
        assertThat(response.getBody().getRole()).isEqualTo(Role.AGENT_COLLECTE);
        verify(agentAuthService).getMe();
    }
}
