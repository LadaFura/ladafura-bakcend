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

import com.pharmacopee.ladafura.controllers.population.PopulationNotificationController;
import com.pharmacopee.ladafura.dto.population.notification.PopulationNotificationCountResponse;
import com.pharmacopee.ladafura.dto.population.notification.PopulationNotificationResponse;
import com.pharmacopee.ladafura.dto.population.notification.PopulationNotificationStatsResponse;
import com.pharmacopee.ladafura.enums.NiveauNotification;
import com.pharmacopee.ladafura.enums.TypeNotification;
import com.pharmacopee.ladafura.services.interfaces.IPopulationNotificationService;

@ExtendWith(MockitoExtension.class)
class PopulationNotificationControllerTest {

    @Mock
    private IPopulationNotificationService notificationService;

    @InjectMocks
    private PopulationNotificationController notificationController;

    @Test
    @DisplayName("GET /api/v1/population/notifications - 200 OK")
    void getMesNotifications_200OK() {
        PopulationNotificationResponse response = PopulationNotificationResponse.builder()
                .id(1L)
                .titre("Commande validée")
                .type(TypeNotification.COMMANDE_STATUT)
                .niveau(NiveauNotification.SUCCES)
                .lue(false)
                .dateNotification(LocalDateTime.now())
                .build();

        Pageable pageable = PageRequest.of(0, 10);
        when(notificationService.getMesNotifications(eq(false), eq(TypeNotification.COMMANDE_STATUT), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(response)));

        ResponseEntity<Page<PopulationNotificationResponse>> result =
                notificationController.getMesNotifications(false, TypeNotification.COMMANDE_STATUT, pageable);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getContent()).hasSize(1);
        assertThat(result.getBody().getContent().get(0).getTitre()).isEqualTo("Commande validée");
    }

    @Test
    @DisplayName("GET /api/v1/population/notifications/stats - 200 OK")
    void getStats_200OK() {
        PopulationNotificationStatsResponse stats = PopulationNotificationStatsResponse.builder()
                .total(12)
                .nonLues(4)
                .lues(8)
                .build();

        when(notificationService.getStats()).thenReturn(stats);

        ResponseEntity<PopulationNotificationStatsResponse> result = notificationController.getStats();

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getTotal()).isEqualTo(12);
    }

    @Test
    @DisplayName("GET /api/v1/population/notifications/unread-count - 200 OK")
    void getUnreadCount_200OK() {
        PopulationNotificationCountResponse count = PopulationNotificationCountResponse.builder()
                .count(3)
                .build();

        when(notificationService.getUnreadCount()).thenReturn(count);

        ResponseEntity<PopulationNotificationCountResponse> result = notificationController.getUnreadCount();

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getCount()).isEqualTo(3);
    }

    @Test
    @DisplayName("GET /api/v1/population/notifications/{id} - 200 OK")
    void getNotificationById_200OK() {
        PopulationNotificationResponse response = PopulationNotificationResponse.builder()
                .id(1L)
                .titre("Commande livrée")
                .lue(true)
                .build();

        when(notificationService.getNotificationById(1L)).thenReturn(response);

        ResponseEntity<PopulationNotificationResponse> result = notificationController.getNotificationById(1L);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("PATCH /api/v1/population/notifications/{id}/read - 200 OK")
    void markAsRead_200OK() {
        PopulationNotificationResponse response = PopulationNotificationResponse.builder()
                .id(1L)
                .lue(true)
                .build();

        when(notificationService.markAsRead(1L)).thenReturn(response);

        ResponseEntity<PopulationNotificationResponse> result = notificationController.markAsRead(1L);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getLue()).isTrue();
    }

    @Test
    @DisplayName("POST /api/v1/population/notifications/read-all - 204 No Content")
    void markAllAsRead_204NoContent() {
        ResponseEntity<Void> result = notificationController.markAllAsRead();

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(notificationService).markAllAsRead();
    }

    @Test
    @DisplayName("DELETE /api/v1/population/notifications/{id} - 204 No Content")
    void deleteNotification_204NoContent() {
        ResponseEntity<Void> result = notificationController.deleteNotification(1L);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(notificationService).deleteNotification(1L);
    }

    @Test
    @DisplayName("DELETE /api/v1/population/notifications/clear-read - 204 No Content")
    void clearAllReadNotifications_204NoContent() {
        ResponseEntity<Void> result = notificationController.clearAllReadNotifications();

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(notificationService).clearAllReadNotifications();
    }
}
