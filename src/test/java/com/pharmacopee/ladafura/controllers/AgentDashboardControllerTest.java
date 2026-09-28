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

import com.pharmacopee.ladafura.controllers.agent.AgentDashboardController;
import com.pharmacopee.ladafura.dto.agent.collecte.AgentCollecteSummaryResponse;
import com.pharmacopee.ladafura.dto.agent.dashboard.AgentDashboardResponse;
import com.pharmacopee.ladafura.dto.agent.notification.AgentNotificationResponse;
import com.pharmacopee.ladafura.enums.StatutCollecte;
import com.pharmacopee.ladafura.services.interfaces.IAgentDashboardService;

@ExtendWith(MockitoExtension.class)
class AgentDashboardControllerTest {

    @Mock
    private IAgentDashboardService dashboardService;

    @InjectMocks
    private AgentDashboardController dashboardController;

    @Test
    @DisplayName("GET /api/v1/agent/dashboard - 200 OK")
    void getDashboard_200OK() {
        AgentDashboardResponse mockDashboard = AgentDashboardResponse.builder()
                .agent(AgentDashboardResponse.AgentInfoSection.builder()
                        .agentId(10L)
                        .nom("Traoré")
                        .prenom("Bakary")
                        .email("agent.traore@ladafura.ml")
                        .matricule("AGT-2026-0042")
                        .build())
                .statistiques(AgentDashboardResponse.CollectesStatsSection.builder()
                        .total(20L)
                        .brouillons(5L)
                        .enAttente(5L)
                        .validees(8L)
                        .rejetees(2L)
                        .tauxValidation(80.0)
                        .build())
                .notifications(AgentDashboardResponse.NotificationsSection.builder()
                        .total(8L)
                        .nonLues(2L)
                        .dernieresNotifications(List.of(
                                AgentNotificationResponse.builder()
                                        .id(1L)
                                        .titre("Collecte validée")
                                        .build()))
                        .build())
                .dernieresCollectes(List.of(
                        AgentCollecteSummaryResponse.builder()
                                .id(100L)
                                .statut(StatutCollecte.VALIDEE)
                                .build()))
                .build();

        when(dashboardService.getDashboard()).thenReturn(mockDashboard);

        ResponseEntity<AgentDashboardResponse> response = dashboardController.getDashboard();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getAgent().getMatricule()).isEqualTo("AGT-2026-0042");
        assertThat(response.getBody().getStatistiques().getTotal()).isEqualTo(20L);
        assertThat(response.getBody().getStatistiques().getTauxValidation()).isEqualTo(80.0);
        assertThat(response.getBody().getNotifications().getNonLues()).isEqualTo(2L);
        assertThat(response.getBody().getDernieresCollectes()).hasSize(1);

        verify(dashboardService).getDashboard();
    }
}
