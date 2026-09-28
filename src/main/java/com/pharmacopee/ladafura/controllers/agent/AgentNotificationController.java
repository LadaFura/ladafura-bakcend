package com.pharmacopee.ladafura.controllers.agent;

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

import com.pharmacopee.ladafura.dto.agent.notification.AgentNotificationResponse;
import com.pharmacopee.ladafura.dto.agent.notification.AgentNotificationStatsResponse;
import com.pharmacopee.ladafura.enums.TypeNotification;
import com.pharmacopee.ladafura.services.interfaces.IAgentNotificationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/agent/notifications")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasAuthority('ROLE_AGENT_COLLECTE')")
@Tag(name = "Agent - Notifications", description = "Endpoints de gestion du centre de notifications de l'Agent de Collecte (alertes de validation, rejet, demande de correction, changement de statut et communications administratives).")
public class AgentNotificationController {

    private final IAgentNotificationService notificationService;

    @GetMapping
    @Operation(summary = "Consulter ses notifications avec filtres et pagination",
               description = "Renvoie la liste paginée des notifications reçues par l'agent connecté. Filtres possibles : état de lecture (lue) et type de notification.")
    @ApiResponse(responseCode = "200", description = "Notifications récupérées avec succès")
    public ResponseEntity<Page<AgentNotificationResponse>> getMyNotifications(
            @Parameter(description = "Filtrer par état de lecture (true = lues, false = non lues)")
            @RequestParam(required = false) Boolean lue,
            @Parameter(description = "Filtrer par type de notification (COLLECTE_VALIDEE, COLLECTE_REJETEE, etc.)")
            @RequestParam(required = false) TypeNotification type,
            @ParameterObject @PageableDefault(sort = "dateNotification", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("Consultation des notifications agent (lue={}, type={})", lue, type);
        return ResponseEntity.ok(notificationService.getMyNotifications(lue, type, pageable));
    }

    @GetMapping("/stats")
    @Operation(summary = "Consulter les statistiques du centre de notifications",
               description = "Renvoie les compteurs de notifications de l'agent connecté : total, non lues, lues, et compteurs par type.")
    @ApiResponse(responseCode = "200", description = "Statistiques de notifications récupérées avec succès")
    public ResponseEntity<AgentNotificationStatsResponse> getStats() {
        log.info("Consultation des statistiques de notifications agent");
        return ResponseEntity.ok(notificationService.getStats());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulter le détail d'une notification",
               description = "Renvoie les informations de la notification et la marque automatiquement comme lue.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Notification récupérée avec succès"),
        @ApiResponse(responseCode = "404", description = "Notification introuvable ou n'appartenant pas à l'agent")
    })
    public ResponseEntity<AgentNotificationResponse> getNotificationById(@PathVariable Long id) {
        log.info("Consultation notification ID {}", id);
        return ResponseEntity.ok(notificationService.getNotificationById(id));
    }

    @PatchMapping("/{id}/read")
    @Operation(summary = "Marquer une notification comme lue",
               description = "Met à jour l'état de la notification à 'lue' avec horodatage.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Notification marquée comme lue"),
        @ApiResponse(responseCode = "404", description = "Notification introuvable")
    })
    public ResponseEntity<AgentNotificationResponse> markAsRead(@PathVariable Long id) {
        log.info("Marquage de la notification ID {} comme lue", id);
        return ResponseEntity.ok(notificationService.markAsRead(id));
    }

    @PostMapping("/read-all")
    @Operation(summary = "Marquer toutes les notifications comme lues",
               description = "Met à jour en bloc toutes les notifications non lues de l'agent connecté.")
    @ApiResponse(responseCode = "204", description = "Toutes les notifications marquées comme lues")
    public ResponseEntity<Void> markAllAsRead() {
        log.info("Marquage de toutes les notifications comme lues");
        notificationService.markAllAsRead();
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer une notification",
               description = "Supprime définitivement une notification de l'historique de l'agent.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Notification supprimée"),
        @ApiResponse(responseCode = "404", description = "Notification introuvable")
    })
    public ResponseEntity<Void> deleteNotification(@PathVariable Long id) {
        log.info("Suppression de la notification ID {}", id);
        notificationService.deleteNotification(id);
        return ResponseEntity.noContent().build();
    }
}
