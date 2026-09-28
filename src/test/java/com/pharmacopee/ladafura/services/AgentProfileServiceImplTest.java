package com.pharmacopee.ladafura.services;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.pharmacopee.ladafura.Models.AgentCollecte;
import com.pharmacopee.ladafura.dto.agent.profil.AgentProfileResponse;
import com.pharmacopee.ladafura.dto.agent.profil.AgentUpdateProfileRequest;
import com.pharmacopee.ladafura.enums.Role;
import com.pharmacopee.ladafura.enums.StatutUtilisateur;
import com.pharmacopee.ladafura.repository.AgentCollecteRepository;
import com.pharmacopee.ladafura.repository.CollecteRepository;
import com.pharmacopee.ladafura.services.impl.AgentProfileServiceImpl;
import com.pharmacopee.ladafura.services.interfaces.IAgentAuthService;

@ExtendWith(MockitoExtension.class)
class AgentProfileServiceImplTest {

    @Mock
    private IAgentAuthService agentAuthService;

    @Mock
    private AgentCollecteRepository agentCollecteRepository;

    @Mock
    private CollecteRepository collecteRepository;

    @InjectMocks
    private AgentProfileServiceImpl profileService;

    private AgentCollecte testAgent;

    @BeforeEach
    void setUp() {
        testAgent = new AgentCollecte();
        testAgent.setId(10L);
        testAgent.setNom("Coulibaly");
        testAgent.setPrenom("Oumar");
        testAgent.setEmail("oumar.coulibaly@ladafura.ml");
        testAgent.setTelephone("+223 75 00 11 22");
        testAgent.setMatricule("AGT-2026-001");
        testAgent.setZoneCouverture("Sikasso - Cercle de Koutiala");
        testAgent.setRole(Role.AGENT_COLLECTE);
        testAgent.setStatut(StatutUtilisateur.ACTIF);
        testAgent.setFirebaseUid("firebase-uid-agent-123");
        testAgent.setDateCreation(LocalDateTime.now().minusMonths(3));
    }

    @Test
    @DisplayName("getProfile - Retourne les détails du profil avec le compteur de collectes")
    void getProfile_Success() {
        // GIVEN
        when(agentAuthService.getCurrentAgent()).thenReturn(testAgent);
        when(collecteRepository.countByAgentCollecteId(10L)).thenReturn(8L);

        // WHEN
        AgentProfileResponse response = profileService.getProfile();

        // THEN
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(10L);
        assertThat(response.getMatricule()).isEqualTo("AGT-2026-001");
        assertThat(response.getNom()).isEqualTo("Coulibaly");
        assertThat(response.getPrenom()).isEqualTo("Oumar");
        assertThat(response.getEmail()).isEqualTo("oumar.coulibaly@ladafura.ml");
        assertThat(response.getTelephone()).isEqualTo("+223 75 00 11 22");
        assertThat(response.getZoneCouverture()).isEqualTo("Sikasso - Cercle de Koutiala");
        assertThat(response.getRole()).isEqualTo(Role.AGENT_COLLECTE);
        assertThat(response.getStatut()).isEqualTo(StatutUtilisateur.ACTIF);
        assertThat(response.getNombreTotalCollectes()).isEqualTo(8L);
    }

    @Test
    @DisplayName("updateProfile - Met à jour les informations autorisées sans altérer rôle ou statut")
    void updateProfile_Success() {
        // GIVEN
        when(agentAuthService.getCurrentAgent()).thenReturn(testAgent);
        when(agentCollecteRepository.save(any(AgentCollecte.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(collecteRepository.countByAgentCollecteId(10L)).thenReturn(8L);

        AgentUpdateProfileRequest request = AgentUpdateProfileRequest.builder()
                .nom("Coulibaly Modifié")
                .prenom("Oumar Junior")
                .telephone("+223 70 99 88 77")
                .zoneCouverture("Nouvelle Zone - Région de Koulikoro")
                .build();

        // WHEN
        AgentProfileResponse response = profileService.updateProfile(request);

        // THEN
        assertThat(response).isNotNull();
        assertThat(response.getNom()).isEqualTo("Coulibaly Modifié");
        assertThat(response.getPrenom()).isEqualTo("Oumar Junior");
        assertThat(response.getTelephone()).isEqualTo("+223 70 99 88 77");
        assertThat(response.getZoneCouverture()).isEqualTo("Nouvelle Zone - Région de Koulikoro");

        // Vérification de la non-altération du rôle, statut, matricule
        assertThat(response.getRole()).isEqualTo(Role.AGENT_COLLECTE);
        assertThat(response.getStatut()).isEqualTo(StatutUtilisateur.ACTIF);
        assertThat(response.getMatricule()).isEqualTo("AGT-2026-001");
        assertThat(response.getEmail()).isEqualTo("oumar.coulibaly@ladafura.ml");

        verify(agentCollecteRepository).save(testAgent);
    }
}
