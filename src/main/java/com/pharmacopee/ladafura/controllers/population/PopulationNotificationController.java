package com.pharmacopee.ladafura.controllers.population;

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

import com.pharmacopee.ladafura.dto.population.notification.PopulationNotificationCountResponse;
import com.pharmacopee.ladafura.dto.population.notification.PopulationNotificationResponse;
import com.pharmacopee.ladafura.dto.population.notification.PopulationNotificationStatsResponse;
import com.pharmacopee.ladafura.enums.TypeNotification;
import com.pharmacopee.ladafura.services.interfaces.IPopulationNotificationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/population/notifications")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasAuthority('ROLE_POPULATION')")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Population - Notifications", description = "Endpoints de gestion du centre de notifications de l'acteur Population (commandes, avis, alertes sanitaires)")
public class PopulationNotificationController {

    private final IPopulationNotificationService notificationService;

    @GetMapping
    @Operation(summary = "Consulter ses notifications avec filtres et pagination",
               description = "Renvoie la liste paginée des notifications reçues par l'utilisateur connecté. Filtres possibles : état de lecture (lue) et type de notification.")
    @ApiResponse(responseCode = "200", description = "Notifications récupérées avec succès")
    public ResponseEntity<Page<PopulationNotificationResponse>> getMesNotifications(
            @Parameter(description = "Filtrer par état de lecture (true = lues, false = non lues)")
            @RequestParam(required = false) Boolean lue,
            @Parameter(description = "Filtrer par type de notification (COMMANDE_STATUT, AVIS_NOUVEAU, etc.)")
            @RequestParam(required = false) TypeNotification type,
            @ParameterObject @PageableDefault(sort = "dateNotification", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("Consultation des notifications population (lue={}, type={})", lue, type);
        return ResponseEntity.ok(notificationService.getMesNotifications(lue, type, pageable));
    }

    @GetMapping("/stats")
    @Operation(summary = "Consulter les statistiques du centre de notifications",
               description = "Renvoie les compteurs de notifications : total, non lues, lues, et compteurs par thématique.")
    @ApiResponse(responseCode = "200", description = "Statistiques récupérées avec succès")
    public ResponseEntity<PopulationNotificationStatsResponse> getStats() {
        log.info("Consultation des statistiques de notifications population");
        return ResponseEntity.ok(notificationService.getStats());
    }

    @GetMapping("/unread-count")
    @Operation(summary = "Obtenir le nombre de notifications non lues (pour badge mobile/web)",
               description = "Renvoie un compteur rapide et léger du nombre de messages non lus.")
    @ApiResponse(responseCode = "200", description = "Compteur récupéré avec succès")
    public ResponseEntity<PopulationNotificationCountResponse> getUnreadCount() {
        log.info("Consultation du compteur de notifications non lues");
        return ResponseEntity.ok(notificationService.getUnreadCount());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulter le détail d'une notification",
               description = "Renvoie les informations de la notification et la marque automatiquement comme lue.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Notification récupérée avec succès"),
        @ApiResponse(responseCode = "404", description = "Notification introuvable ou n'appartenant pas à l'utilisateur")
    })
    public ResponseEntity<PopulationNotificationResponse> getNotificationById(@PathVariable Long id) {
        log.info("Consultation de la notification ID: {}", id);
        return ResponseEntity.ok(notificationService.getNotificationById(id));
    }

    @PatchMapping("/{id}/read")
    @Operation(summary = "Marquer explicitement une notification comme lue",
               description = "Met à jour l'état de la notification à 'lue' avec horodatage de lecture.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Notification marquée comme lue"),
        @ApiResponse(responseCode = "404", description = "Notification introuvable")
    })
    public ResponseEntity<PopulationNotificationResponse> markAsRead(@PathVariable Long id) {
        log.info("Marquage de la notification ID: {} comme lue", id);
        return ResponseEntity.ok(notificationService.markAsRead(id));
    }

    @PostMapping("/read-all")
    @Operation(summary = "Marquer toutes les notifications comme lues",
               description = "Met à jour en bloc toutes les notifications non lues de l'utilisateur.")
    @ApiResponse(responseCode = "204", description = "Toutes les notifications ont été marquées comme lues")
    public ResponseEntity<Void> markAllAsRead() {
        log.info("Marquage de toutes les notifications comme lues");
        notificationService.markAllAsRead();
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer une notification",
               description = "Supprime définitivement une notification de l'historique de l'utilisateur.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Notification supprimée"),
        @ApiResponse(responseCode = "404", description = "Notification introuvable")
    })
    public ResponseEntity<Void> deleteNotification(@PathVariable Long id) {
        log.info("Suppression de la notification ID: {}", id);
        notificationService.deleteNotification(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/clear-read")
    @Operation(summary = "Supprimer toutes les notifications déjà lues",
               description = "Nettoie la boîte de réception en supprimant en bloc les notifications lues.")
    @ApiResponse(responseCode = "204", description = "Notifications lues supprimées")
    public ResponseEntity<Void> clearAllReadNotifications() {
        log.info("Nettoyage de toutes les notifications déjà lues");
        notificationService.clearAllReadNotifications();
        return ResponseEntity.noContent().build();
    }
}
