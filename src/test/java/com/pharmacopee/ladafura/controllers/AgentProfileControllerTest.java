package com.pharmacopee.ladafura.controllers;

import java.time.LocalDateTime;

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

import com.pharmacopee.ladafura.controllers.agent.AgentProfileController;
import com.pharmacopee.ladafura.dto.agent.profil.AgentProfileResponse;
import com.pharmacopee.ladafura.dto.agent.profil.AgentUpdateProfileRequest;
import com.pharmacopee.ladafura.enums.Role;
import com.pharmacopee.ladafura.enums.StatutUtilisateur;
import com.pharmacopee.ladafura.services.interfaces.IAgentProfileService;

@ExtendWith(MockitoExtension.class)
class AgentProfileControllerTest {

    @Mock
    private IAgentProfileService agentProfileService;

    @InjectMocks
    private AgentProfileController agentProfileController;

    @Test
    @DisplayName("getProfile - Retourne statut 200 et les détails du profil de l'agent")
    void getProfile_Success() {
        // GIVEN
        AgentProfileResponse responseDto = AgentProfileResponse.builder()
                .id(4L)
                .matricule("AGT-2026-004")
                .nom("Coulibaly")
                .prenom("Oumar")
                .email("oumar@ladafura.ml")
                .role(Role.AGENT_COLLECTE)
                .statut(StatutUtilisateur.ACTIF)
                .nombreTotalCollectes(15L)
                .dateCreation(LocalDateTime.now())
                .build();

        when(agentProfileService.getProfile()).thenReturn(responseDto);

        // WHEN
        ResponseEntity<AgentProfileResponse> response = agentProfileController.getProfile();

        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMatricule()).isEqualTo("AGT-2026-004");
        assertThat(response.getBody().getNombreTotalCollectes()).isEqualTo(15L);
        verify(agentProfileService).getProfile();
    }

    @Test
    @DisplayName("updateProfile - Met à jour les informations et retourne statut 200")
    void updateProfile_Success() {
        // GIVEN
        AgentUpdateProfileRequest request = AgentUpdateProfileRequest.builder()
                .nom("Traoré")
                .prenom("Bakary")
                .telephone("+223 76 11 22 33")
                .zoneCouverture("Koutiala")
                .build();

        AgentProfileResponse updatedDto = AgentProfileResponse.builder()
                .id(4L)
                .matricule("AGT-2026-004")
                .nom("Traoré")
                .prenom("Bakary")
                .telephone("+223 76 11 22 33")
                .zoneCouverture("Koutiala")
                .role(Role.AGENT_COLLECTE)
                .statut(StatutUtilisateur.ACTIF)
                .build();

        when(agentProfileService.updateProfile(request)).thenReturn(updatedDto);

        // WHEN
        ResponseEntity<AgentProfileResponse> response = agentProfileController.updateProfile(request);

        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getNom()).isEqualTo("Traoré");
        assertThat(response.getBody().getPrenom()).isEqualTo("Bakary");
        verify(agentProfileService).updateProfile(request);
    }
}
