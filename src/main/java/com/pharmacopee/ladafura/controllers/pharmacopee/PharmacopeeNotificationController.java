package com.pharmacopee.ladafura.controllers.pharmacopee;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pharmacopee.ladafura.dto.pharmacopee.notification.CreateNotificationRequest;
import com.pharmacopee.ladafura.dto.pharmacopee.notification.NotificationResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.notification.NotificationSummaryResponse;
import com.pharmacopee.ladafura.enums.TypeNotification;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeNotificationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/pharmacopee/notifications")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Pharmacopée - Notifications", description = "Endpoints de gestion du centre de notifications de l'officine (nouvelles commandes, statut, agrément, avis, alertes)")
@SecurityRequirement(name = "bearerAuth")
public class PharmacopeeNotificationController {

    private final IPharmacopeeNotificationService notificationService;

    @Operation(summary = "Consulter la liste paginée des notifications",
               description = "Renvoie les notifications de l'officine avec possibilité de filtrer sur les non-lues et par type.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notifications récupérées avec succès"),
            @ApiResponse(responseCode = "401", description = "Non authentifié"),
            @ApiResponse(responseCode = "403", description = "Accès refusé")
    })
    @GetMapping
    public ResponseEntity<Page<NotificationResponse>> getNotifications(
            @Parameter(description = "Ne récupérer que les notifications non lues", example = "true")
            @RequestParam(required = false) Boolean nonLuesSeulement,
            @Parameter(description = "Filtrer par type de notification", example = "COMMANDE_NOUVELLE")
            @RequestParam(required = false) TypeNotification type,
            @ParameterObject Pageable pageable) {
        log.info("Requête GET /api/v1/pharmacopee/notifications reçue (nonLues={}, type={})", nonLuesSeulement, type);
        return ResponseEntity.ok(notificationService.getNotifications(nonLuesSeulement, type, pageable));
    }

    @Operation(summary = "Synthèse et compteurs des notifications (Badge de cloche)",
               description = "Renvoie le nombre de notifications non lues (pour affichage dynamique du badge dans l'application) et la répartition par type.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Compteurs récupérés avec succès"),
            @ApiResponse(responseCode = "401", description = "Non authentifié"),
            @ApiResponse(responseCode = "403", description = "Accès refusé")
    })
    @GetMapping("/summary")
    public ResponseEntity<NotificationSummaryResponse> getSummary() {
        log.info("Requête GET /api/v1/pharmacopee/notifications/summary reçue");
        return ResponseEntity.ok(notificationService.getSummary());
    }

    @Operation(summary = "Marquer une notification comme lue",
               description = "Bascule l'état de la notification à lue et enregistre la date de première lecture.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notification marquée comme lue avec succès"),
            @ApiResponse(responseCode = "401", description = "Non authentifié"),
            @ApiResponse(responseCode = "404", description = "Notification non trouvée")
    })
    @PatchMapping("/{id}/lire")
    public ResponseEntity<NotificationResponse> marquerCommeLue(
            @Parameter(description = "Identifiant de la notification", example = "1")
            @PathVariable Long id) {
        log.info("Requête PATCH /api/v1/pharmacopee/notifications/{}/lire reçue", id);
        return ResponseEntity.ok(notificationService.marquerCommeLue(id));
    }

    @Operation(summary = "Marquer toutes les notifications comme lues",
               description = "Passe l'ensemble des notifications non lues de l'officine à l'état lu en une seule opération.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Toutes les notifications ont été marquées comme lues"),
            @ApiResponse(responseCode = "401", description = "Non authentifié"),
            @ApiResponse(responseCode = "403", description = "Accès refusé")
    })
    @PatchMapping("/lire-tout")
    public ResponseEntity<Void> marquerToutesCommeLues() {
        log.info("Requête PATCH /api/v1/pharmacopee/notifications/lire-tout reçue");
        notificationService.marquerToutesCommeLues();
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Supprimer une notification",
               description = "Supprime définitivement une notification de l'historique.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Notification supprimée avec succès"),
            @ApiResponse(responseCode = "401", description = "Non authentifié"),
            @ApiResponse(responseCode = "404", description = "Notification non trouvée")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimerNotification(
            @Parameter(description = "Identifiant de la notification", example = "1")
            @PathVariable Long id) {
        log.info("Requête DELETE /api/v1/pharmacopee/notifications/{} reçue", id);
        notificationService.supprimerNotification(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Créer une notification (Émission manuelle / Test)",
               description = "Permet de générer une notification pour tester le centre de notifications et les redirections.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Notification créée avec succès"),
            @ApiResponse(responseCode = "400", description = "Données invalides"),
            @ApiResponse(responseCode = "401", description = "Non authentifié")
    })
    @PostMapping
    public ResponseEntity<NotificationResponse> creerNotification(
            @Valid @RequestBody CreateNotificationRequest request) {
        log.info("Requête POST /api/v1/pharmacopee/notifications reçue : {}", request.getTitre());
        return ResponseEntity.status(HttpStatus.CREATED).body(notificationService.creerNotification(request));
    }
}
