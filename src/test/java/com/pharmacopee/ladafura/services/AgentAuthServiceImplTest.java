package com.pharmacopee.ladafura.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import com.pharmacopee.ladafura.Models.AgentCollecte;
import com.pharmacopee.ladafura.Models.Utilisateur;
import com.pharmacopee.ladafura.dto.agent.auth.AgentAuthResponse;
import com.pharmacopee.ladafura.enums.Role;
import com.pharmacopee.ladafura.enums.StatutUtilisateur;
import com.pharmacopee.ladafura.exceptions.ForbiddenException;
import com.pharmacopee.ladafura.exceptions.UnauthorizedException;
import com.pharmacopee.ladafura.repository.AgentCollecteRepository;
import com.pharmacopee.ladafura.repository.UtilisateurRepository;
import com.pharmacopee.ladafura.services.impl.AgentAuthServiceImpl;

@ExtendWith(MockitoExtension.class)
class AgentAuthServiceImplTest {

    @Mock
    private AgentCollecteRepository agentCollecteRepository;

    @Mock
    private UtilisateurRepository utilisateurRepository;

    @InjectMocks
    private AgentAuthServiceImpl agentAuthService;

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
        testAgent.setZoneCouverture("Sikasso");
        testAgent.setRole(Role.AGENT_COLLECTE);
        testAgent.setStatut(StatutUtilisateur.ACTIF);
        testAgent.setFirebaseUid("firebase-uid-agent-123");
        testAgent.setDateCreation(LocalDateTime.now());
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("getCurrentAgent - Succès avec identifiant authentifié dans le SecurityContext")
    void getCurrentAgent_Success() {
        // GIVEN
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(testAgent.getEmail(), "mock-token", List.of(new SimpleGrantedAuthority("ROLE_AGENT_COLLECTE")))
        );
        when(agentCollecteRepository.findByEmail(testAgent.getEmail())).thenReturn(Optional.of(testAgent));

        // WHEN
        AgentCollecte currentAgent = agentAuthService.getCurrentAgent();

        // THEN
        assertThat(currentAgent).isNotNull();
        assertThat(currentAgent.getId()).isEqualTo(10L);
        assertThat(currentAgent.getMatricule()).isEqualTo("AGT-2026-001");
        assertThat(currentAgent.getRole()).isEqualTo(Role.AGENT_COLLECTE);
        assertThat(currentAgent.getStatut()).isEqualTo(StatutUtilisateur.ACTIF);
    }

    @Test
    @DisplayName("getCurrentAgent - Échec si non authentifié dans le SecurityContext")
    void getCurrentAgent_Unauthenticated() {
        // GIVEN: Aucun contexte d'authentification

        // WHEN & THEN
        assertThatThrownBy(() -> agentAuthService.getCurrentAgent())
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Authentification requise");
    }

    @Test
    @DisplayName("getCurrentAgent - Échec si utilisateur introuvable en base")
    void getCurrentAgent_NotFound() {
        // GIVEN
        String unknownEmail = "inconnu@ladafura.ml";
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(unknownEmail, "mock-token", List.of(new SimpleGrantedAuthority("ROLE_AGENT_COLLECTE")))
        );
        when(agentCollecteRepository.findByEmail(unknownEmail)).thenReturn(Optional.empty());
        when(agentCollecteRepository.findByFirebaseUid(unknownEmail)).thenReturn(Optional.empty());
        when(utilisateurRepository.findByEmail(unknownEmail)).thenReturn(Optional.empty());
        when(utilisateurRepository.findByFirebaseUid(unknownEmail)).thenReturn(Optional.empty());

        // WHEN & THEN
        assertThatThrownBy(() -> agentAuthService.getCurrentAgent())
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Aucun compte utilisateur trouvé");
    }

    @Test
    @DisplayName("getCurrentAgent - Échec si l'utilisateur possède un rôle autre que AGENT_COLLECTE")
    void getCurrentAgent_WrongRole() {
        // GIVEN
        Utilisateur otherUser = new Utilisateur();
        otherUser.setId(20L);
        otherUser.setEmail("pharma@ladafura.ml");
        otherUser.setRole(Role.PHARMACOPEE);
        otherUser.setStatut(StatutUtilisateur.ACTIF);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(otherUser.getEmail(), "mock-token", List.of(new SimpleGrantedAuthority("ROLE_AGENT_COLLECTE")))
        );
        when(agentCollecteRepository.findByEmail(otherUser.getEmail())).thenReturn(Optional.empty());
        when(agentCollecteRepository.findByFirebaseUid(otherUser.getEmail())).thenReturn(Optional.empty());
        when(utilisateurRepository.findByEmail(otherUser.getEmail())).thenReturn(Optional.of(otherUser));

        // WHEN & THEN
        assertThatThrownBy(() -> agentAuthService.getCurrentAgent())
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("votre compte ne dispose pas du rôle AGENT_COLLECTE");
    }

    @Test
    @DisplayName("getCurrentAgent - Échec si le compte de l'agent est inactif")
    void getCurrentAgent_InactiveAccount() {
        // GIVEN
        testAgent.setStatut(StatutUtilisateur.INACTIF);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(testAgent.getEmail(), "mock-token", List.of(new SimpleGrantedAuthority("ROLE_AGENT_COLLECTE")))
        );
        when(agentCollecteRepository.findByEmail(testAgent.getEmail())).thenReturn(Optional.of(testAgent));

        // WHEN & THEN
        assertThatThrownBy(() -> agentAuthService.getCurrentAgent())
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("inactif ou suspendu");
    }

    @Test
    @DisplayName("getMe - Retourne les informations complètes de l'agent connecté")
    void getMe_Success() {
        // GIVEN
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(testAgent.getEmail(), "mock-token", List.of(new SimpleGrantedAuthority("ROLE_AGENT_COLLECTE")))
        );
        when(agentCollecteRepository.findByEmail(testAgent.getEmail())).thenReturn(Optional.of(testAgent));

        // WHEN
        AgentAuthResponse response = agentAuthService.getMe();

        // THEN
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(10L);
        assertThat(response.getMatricule()).isEqualTo("AGT-2026-001");
        assertThat(response.getZoneCouverture()).isEqualTo("Sikasso");
        assertThat(response.getNom()).isEqualTo("Coulibaly");
        assertThat(response.getPrenom()).isEqualTo("Oumar");
        assertThat(response.getEmail()).isEqualTo("oumar.coulibaly@ladafura.ml");
        assertThat(response.getTelephone()).isEqualTo("+223 75 00 11 22");
        assertThat(response.getRole()).isEqualTo(Role.AGENT_COLLECTE);
        assertThat(response.getStatut()).isEqualTo(StatutUtilisateur.ACTIF);
        assertThat(response.getFirebaseUid()).isEqualTo("firebase-uid-agent-123");
    }

    @Test
    @DisplayName("verifyAgentOwnership - Succès si l'ID correspond à l'agent connecté")
    void verifyAgentOwnership_Success() {
        // GIVEN
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(testAgent.getEmail(), "mock-token", List.of(new SimpleGrantedAuthority("ROLE_AGENT_COLLECTE")))
        );
        when(agentCollecteRepository.findByEmail(testAgent.getEmail())).thenReturn(Optional.of(testAgent));

        // WHEN & THEN: Ne doit lever aucune exception
        agentAuthService.verifyAgentOwnership(10L);
    }

    @Test
    @DisplayName("verifyAgentOwnership - Échec si l'ID ne correspond pas à l'agent connecté")
    void verifyAgentOwnership_Mismatch() {
        // GIVEN
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(testAgent.getEmail(), "mock-token", List.of(new SimpleGrantedAuthority("ROLE_AGENT_COLLECTE")))
        );
        when(agentCollecteRepository.findByEmail(testAgent.getEmail())).thenReturn(Optional.of(testAgent));

        // WHEN & THEN
        assertThatThrownBy(() -> agentAuthService.verifyAgentOwnership(999L))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("vous n'êtes pas autorisé à accéder aux données d'un autre agent");
    }

    @Test
    @DisplayName("verifyAgentOwnership - Échec si l'ID demandé est null")
    void verifyAgentOwnership_NullId() {
        // WHEN & THEN
        assertThatThrownBy(() -> agentAuthService.verifyAgentOwnership(null))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("Identifiant de l'agent manquant");
    }
}
