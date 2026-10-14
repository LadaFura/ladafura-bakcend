package com.pharmacopee.ladafura.controllers.admin;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pharmacopee.ladafura.dto.admin.notification.AdminNotificationResponse;
import com.pharmacopee.ladafura.dto.admin.notification.AdminNotificationStatsResponse;
import com.pharmacopee.ladafura.enums.TypeNotification;
import com.pharmacopee.ladafura.services.interfaces.IAdminNotificationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/admin/notifications")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMINISTRATEUR')")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Admin - Notifications", description = "Endpoints de consultation et gestion des notifications d'administration de la plateforme")
public class AdminNotificationController {

    private final IAdminNotificationService notificationService;

    @GetMapping
    @Operation(summary = "Consulter les notifications administrateur avec filtres et pagination")
    @ApiResponse(responseCode = "200", description = "Notifications récupérées avec succès")
    public ResponseEntity<Page<AdminNotificationResponse>> getNotifications(
            @Parameter(description = "Filtrer par état de lecture (true = lues, false = non lues)")
            @RequestParam(required = false) Boolean lue,
            @Parameter(description = "Filtrer par type de notification")
            @RequestParam(required = false) TypeNotification type,
            @ParameterObject @PageableDefault(sort = "dateNotification", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("Consultation des notifications admin (lue={}, type={})", lue, type);
        return ResponseEntity.ok(notificationService.getAdminNotifications(lue, type, pageable));
    }

    @GetMapping("/stats")
    @Operation(summary = "Statistiques et synthèse des notifications admin")
    @ApiResponse(responseCode = "200", description = "Statistiques récupérées avec succès")
    public ResponseEntity<AdminNotificationStatsResponse> getStats() {
        log.info("Consultation des statistiques de notifications admin");
        return ResponseEntity.ok(notificationService.getStats());
    }

    @PatchMapping("/{id}/read")
    @Operation(summary = "Marquer une notification admin comme lue")
    @ApiResponse(responseCode = "200", description = "Notification marquée comme lue")
    public ResponseEntity<AdminNotificationResponse> markAsRead(@PathVariable Long id) {
        log.info("Marquage de la notification admin ID: {} comme lue", id);
        return ResponseEntity.ok(notificationService.markAsRead(id));
    }

    @PostMapping("/read-all")
    @Operation(summary = "Marquer toutes les notifications admin comme lues")
    @ApiResponse(responseCode = "204", description = "Toutes les notifications marquées comme lues")
    public ResponseEntity<Void> markAllAsRead() {
        log.info("Marquage de toutes les notifications admin comme lues");
        notificationService.markAllAsRead();
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer une notification admin")
    @ApiResponse(responseCode = "204", description = "Notification supprimée avec succès")
    public ResponseEntity<Void> deleteNotification(@PathVariable Long id) {
        log.info("Suppression de la notification admin ID: {}", id);
        notificationService.deleteNotification(id);
        return ResponseEntity.noContent().build();
    }
}

