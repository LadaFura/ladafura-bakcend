package com.pharmacopee.ladafura.services;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.pharmacopee.ladafura.Models.AgentCollecte;
import com.pharmacopee.ladafura.Models.Collecte;
import com.pharmacopee.ladafura.Models.Notification;
import com.pharmacopee.ladafura.dto.agent.collecte.AgentCollecteSummaryResponse;
import com.pharmacopee.ladafura.dto.agent.dashboard.AgentDashboardResponse;
import com.pharmacopee.ladafura.dto.agent.notification.AgentNotificationResponse;
import com.pharmacopee.ladafura.enums.NiveauNotification;
import com.pharmacopee.ladafura.enums.Role;
import com.pharmacopee.ladafura.enums.StatutCollecte;
import com.pharmacopee.ladafura.enums.StatutUtilisateur;
import com.pharmacopee.ladafura.enums.TypeNotification;
import com.pharmacopee.ladafura.mappers.AgentCollecteMapper;
import com.pharmacopee.ladafura.mappers.AgentNotificationMapper;
import com.pharmacopee.ladafura.repository.CollecteRepository;
import com.pharmacopee.ladafura.repository.NotificationRepository;
import com.pharmacopee.ladafura.services.impl.AgentDashboardServiceImpl;
import com.pharmacopee.ladafura.services.interfaces.IAgentAuthService;

@ExtendWith(MockitoExtension.class)
class AgentDashboardServiceImplTest {

    @Mock
    private IAgentAuthService agentAuthService;

    @Mock
    private CollecteRepository collecteRepository;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private AgentCollecteMapper agentCollecteMapper;

    @Mock
    private AgentNotificationMapper agentNotificationMapper;

    @InjectMocks
    private AgentDashboardServiceImpl agentDashboardService;

    private AgentCollecte agentConnecte;

    @BeforeEach
    void setUp() {
        agentConnecte = new AgentCollecte();
        agentConnecte.setId(10L);
        agentConnecte.setNom("Traoré");
        agentConnecte.setPrenom("Bakary");
        agentConnecte.setEmail("agent.traore@ladafura.ml");
        agentConnecte.setTelephone("+223 76 00 11 22");
        agentConnecte.setMatricule("AGT-2026-0042");
        agentConnecte.setZoneCouverture("Région de Sikasso");
        agentConnecte.setRole(Role.AGENT_COLLECTE);
        agentConnecte.setStatut(StatutUtilisateur.ACTIF);
    }

    @Test
    @DisplayName("getDashboard - Succès avec calcul réel complet des statistiques et top 5")
    void getDashboard_succes() {
        when(agentAuthService.getCurrentAgent()).thenReturn(agentConnecte);

        // Stats collectes
        when(collecteRepository.countByAgentCollecteId(10L)).thenReturn(15L);
        when(collecteRepository.countByAgentCollecteIdAndStatut(10L, StatutCollecte.BROUILLON)).thenReturn(3L);
        when(collecteRepository.countByAgentCollecteIdAndStatut(10L, StatutCollecte.SOUMISE)).thenReturn(2L);
        when(collecteRepository.countByAgentCollecteIdAndStatut(10L, StatutCollecte.EN_EXAMEN)).thenReturn(2L);
        when(collecteRepository.countByAgentCollecteIdAndStatut(10L, StatutCollecte.VALIDEE)).thenReturn(6L);
        when(collecteRepository.countByAgentCollecteIdAndStatut(10L, StatutCollecte.REJETEE)).thenReturn(2L);

        // Stats notifications
        when(notificationRepository.countByUtilisateurId(10L)).thenReturn(12L);
        when(notificationRepository.countByUtilisateurIdAndLueFalse(10L)).thenReturn(4L);

        // Mock notifications page
        Notification notif = Notification.builder()
                .id(1L)
                .titre("Collecte validée")
                .message("Votre fiche a été validée.")
                .type(TypeNotification.COLLECTE_VALIDEE)
                .niveau(NiveauNotification.SUCCES)
                .lue(false)
                .dateNotification(LocalDateTime.now())
                .build();
        Page<Notification> notifPage = new PageImpl<>(List.of(notif));
        when(notificationRepository.findByUtilisateurId(eq(10L), any(Pageable.class))).thenReturn(notifPage);

        AgentNotificationResponse notifDto = AgentNotificationResponse.builder()
                .id(1L)
                .titre("Collecte validée")
                .type(TypeNotification.COLLECTE_VALIDEE)
                .niveau(NiveauNotification.SUCCES)
                .lue(false)
                .build();
        when(agentNotificationMapper.toDto(notif)).thenReturn(notifDto);

        // Mock collectes page
        Collecte collecte = Collecte.builder()
                .id(100L)
                .dateCollecte(LocalDateTime.now())
                .description("Observation Kinkéliba")
                .statut(StatutCollecte.VALIDEE)
                .build();
        Page<Collecte> collectePage = new PageImpl<>(List.of(collecte));
        when(collecteRepository.findByAgentCollecteId(eq(10L), any(Pageable.class))).thenReturn(collectePage);

        AgentCollecteSummaryResponse collecteDto = AgentCollecteSummaryResponse.builder()
                .id(100L)
                .description("Observation Kinkéliba")
                .statut(StatutCollecte.VALIDEE)
                .build();
        when(agentCollecteMapper.toSummaryDto(collecte)).thenReturn(collecteDto);

        AgentDashboardResponse response = agentDashboardService.getDashboard();

        assertThat(response).isNotNull();

        // 1. Volet Agent
        assertThat(response.getAgent()).isNotNull();
        assertThat(response.getAgent().getAgentId()).isEqualTo(10L);
        assertThat(response.getAgent().getNom()).isEqualTo("Traoré");
        assertThat(response.getAgent().getPrenom()).isEqualTo("Bakary");
        assertThat(response.getAgent().getEmail()).isEqualTo("agent.traore@ladafura.ml");
        assertThat(response.getAgent().getMatricule()).isEqualTo("AGT-2026-0042");
        assertThat(response.getAgent().getZoneCouverture()).isEqualTo("Région de Sikasso");

        // 2. Volet Statistiques Collectes
        assertThat(response.getStatistiques()).isNotNull();
        assertThat(response.getStatistiques().getTotal()).isEqualTo(15L);
        assertThat(response.getStatistiques().getBrouillons()).isEqualTo(3L);
        assertThat(response.getStatistiques().getEnAttente()).isEqualTo(4L); // 2 soumises + 2 en examen
        assertThat(response.getStatistiques().getSoumises()).isEqualTo(2L);
        assertThat(response.getStatistiques().getEnExamen()).isEqualTo(2L);
        assertThat(response.getStatistiques().getValidees()).isEqualTo(6L);
        assertThat(response.getStatistiques().getRejetees()).isEqualTo(2L);
        // Taux de validation : 6 / (6 + 2) * 100 = 75.0%
        assertThat(response.getStatistiques().getTauxValidation()).isEqualTo(75.0);

        // 3. Volet Notifications
        assertThat(response.getNotifications()).isNotNull();
        assertThat(response.getNotifications().getTotal()).isEqualTo(12L);
        assertThat(response.getNotifications().getNonLues()).isEqualTo(4L);
        assertThat(response.getNotifications().getDernieresNotifications()).hasSize(1);
        assertThat(response.getNotifications().getDernieresNotifications().get(0).getTitre()).isEqualTo("Collecte validée");

        // 4. Dernières Collectes
        assertThat(response.getDernieresCollectes()).hasSize(1);
        assertThat(response.getDernieresCollectes().get(0).getId()).isEqualTo(100L);

        verify(agentAuthService).getCurrentAgent();
        verify(collecteRepository).countByAgentCollecteId(10L);
        verify(notificationRepository).countByUtilisateurId(10L);
    }

    @Test
    @DisplayName("getDashboard - Taux de validation nul lorsque aucune collecte n'est instruite")
    void getDashboard_tauxValidationNulSiPasDInstruites() {
        when(agentAuthService.getCurrentAgent()).thenReturn(agentConnecte);

        when(collecteRepository.countByAgentCollecteId(10L)).thenReturn(2L);
        when(collecteRepository.countByAgentCollecteIdAndStatut(10L, StatutCollecte.BROUILLON)).thenReturn(1L);
        when(collecteRepository.countByAgentCollecteIdAndStatut(10L, StatutCollecte.SOUMISE)).thenReturn(1L);
        when(collecteRepository.countByAgentCollecteIdAndStatut(10L, StatutCollecte.EN_EXAMEN)).thenReturn(0L);
        when(collecteRepository.countByAgentCollecteIdAndStatut(10L, StatutCollecte.VALIDEE)).thenReturn(0L);
        when(collecteRepository.countByAgentCollecteIdAndStatut(10L, StatutCollecte.REJETEE)).thenReturn(0L);

        when(notificationRepository.countByUtilisateurId(10L)).thenReturn(0L);
        when(notificationRepository.countByUtilisateurIdAndLueFalse(10L)).thenReturn(0L);
        when(notificationRepository.findByUtilisateurId(eq(10L), any(Pageable.class))).thenReturn(Page.empty());
        when(collecteRepository.findByAgentCollecteId(eq(10L), any(Pageable.class))).thenReturn(Page.empty());

        AgentDashboardResponse response = agentDashboardService.getDashboard();

        assertThat(response).isNotNull();
        assertThat(response.getStatistiques().getTauxValidation()).isEqualTo(0.0);
        assertThat(response.getStatistiques().getEnAttente()).isEqualTo(1L);
        assertThat(response.getStatistiques().getTotal()).isEqualTo(2L);
        assertThat(response.getNotifications().getDernieresNotifications()).isEmpty();
        assertThat(response.getDernieresCollectes()).isEmpty();
    }
}
