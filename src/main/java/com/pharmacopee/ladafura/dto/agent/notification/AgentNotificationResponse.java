package com.pharmacopee.ladafura.dto.agent.notification;

import java.time.LocalDateTime;

import com.pharmacopee.ladafura.enums.NiveauNotification;
import com.pharmacopee.ladafura.enums.TypeNotification;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Représentation d'une notification destinée à l'Agent de Collecte")
public class AgentNotificationResponse {

    @Schema(description = "Identifiant unique de la notification", example = "42")
    private Long id;

    @Schema(description = "Titre explicite de la notification", example = "Collecte validée avec succès")
    private String titre;

    @Schema(description = "Message complet contenant les détails ou directives", example = "Votre collecte #100 sur le Combretum micranthum a été validée et intégrée à la pharmacopée.")
    private String message;

    @Schema(description = "Type métier de notification", example = "COLLECTE_VALIDEE")
    private TypeNotification type;

    @Schema(description = "Niveau d'importance (INFO, SUCCES, ATTENTION, URGENT)", example = "SUCCES")
    private NiveauNotification niveau;

    @Schema(description = "Indique si la notification a déjà été consultée/lue", example = "false")
    private Boolean lue;

    @Schema(description = "Date et heure de réception de la notification", example = "2026-09-28T14:30:00")
    private LocalDateTime dateNotification;

    @Schema(description = "Date et heure de première lecture", example = "2026-09-28T15:00:00")
    private LocalDateTime dateLecture;

    @Schema(description = "Identifiant de l'entité de référence (ex: identifiant de la collecte)", example = "100")
    private String referenceId;

    @Schema(description = "Lien d'action ou de consultation directe", example = "/api/v1/agent/collectes/100")
    private String lien;
}
