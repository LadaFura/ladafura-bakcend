package com.pharmacopee.ladafura.controllers;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.pharmacopee.ladafura.controllers.agent.AgentNotificationController;
import com.pharmacopee.ladafura.dto.agent.notification.AgentNotificationResponse;
import com.pharmacopee.ladafura.dto.agent.notification.AgentNotificationStatsResponse;
import com.pharmacopee.ladafura.enums.NiveauNotification;
import com.pharmacopee.ladafura.enums.TypeNotification;
import com.pharmacopee.ladafura.services.interfaces.IAgentNotificationService;

@ExtendWith(MockitoExtension.class)
class AgentNotificationControllerTest {

    @Mock
    private IAgentNotificationService notificationService;

    @InjectMocks
    private AgentNotificationController notificationController;

    @Test
    @DisplayName("GET /api/v1/agent/notifications - 200 OK")
    void getMyNotifications_200OK() {
        AgentNotificationResponse notif = AgentNotificationResponse.builder()
                .id(1L)
                .titre("Collecte validée")
                .type(TypeNotification.COLLECTE_VALIDEE)
                .niveau(NiveauNotification.SUCCES)
                .lue(false)
                .dateNotification(LocalDateTime.now())
                .build();
        Page<AgentNotificationResponse> page = new PageImpl<>(List.of(notif));

        when(notificationService.getMyNotifications(eq(false), eq(TypeNotification.COLLECTE_VALIDEE), any(Pageable.class)))
                .thenReturn(page);

        ResponseEntity<Page<AgentNotificationResponse>> response = notificationController.getMyNotifications(
                false, TypeNotification.COLLECTE_VALIDEE, PageRequest.of(0, 10));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent().get(0).getTitre()).isEqualTo("Collecte validée");
    }

    @Test
    @DisplayName("GET /api/v1/agent/notifications/stats - 200 OK")
    void getStats_200OK() {
        AgentNotificationStatsResponse stats = AgentNotificationStatsResponse.builder()
                .total(10L)
                .nonLues(3L)
                .lues(7L)
                .validations(4L)
                .rejets(2L)
                .build();

        when(notificationService.getStats()).thenReturn(stats);

        ResponseEntity<AgentNotificationStatsResponse> response = notificationController.getStats();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getTotal()).isEqualTo(10L);
        assertThat(response.getBody().getNonLues()).isEqualTo(3L);
    }

    @Test
    @DisplayName("GET /api/v1/agent/notifications/{id} - 200 OK")
    void getNotificationById_200OK() {
        AgentNotificationResponse notif = AgentNotificationResponse.builder()
                .id(5L)
                .titre("Demande de correction")
                .type(TypeNotification.COLLECTE_CORRECTION)
                .lue(true)
                .build();

        when(notificationService.getNotificationById(5L)).thenReturn(notif);

        ResponseEntity<AgentNotificationResponse> response = notificationController.getNotificationById(5L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(5L);
    }

    @Test
    @DisplayName("PATCH /api/v1/agent/notifications/{id}/read - 200 OK")
    void markAsRead_200OK() {
        AgentNotificationResponse notif = AgentNotificationResponse.builder()
                .id(5L)
                .lue(true)
                .build();

        when(notificationService.markAsRead(5L)).thenReturn(notif);

        ResponseEntity<AgentNotificationResponse> response = notificationController.markAsRead(5L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getLue()).isTrue();
    }

    @Test
    @DisplayName("POST /api/v1/agent/notifications/read-all - 204 No Content")
    void markAllAsRead_204NoContent() {
        ResponseEntity<Void> response = notificationController.markAllAsRead();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(notificationService).markAllAsRead();
    }

    @Test
    @DisplayName("DELETE /api/v1/agent/notifications/{id} - 204 No Content")
    void deleteNotification_204NoContent() {
        ResponseEntity<Void> response = notificationController.deleteNotification(5L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(notificationService).deleteNotification(5L);
    }
}
