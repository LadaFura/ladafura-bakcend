package com.pharmacopee.ladafura.dto.admin.notification;

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
@Schema(description = "Représentation d'une notification pour l'espace Administration")
public class AdminNotificationResponse {

    @Schema(description = "Identifiant de la notification", example = "1")
    private Long id;

    @Schema(description = "Titre de la notification", example = "Nouvelle demande d'agrément")
    private String titre;

    @Schema(description = "Message complet", example = "La pharmacopée Fura Danbé a soumis son dossier d'agrément.")
    private String message;

    @Schema(description = "Type métier de la notification", example = "AGREMENT_VALIDATION")
    private TypeNotification type;

    @Schema(description = "Niveau de sévérité", example = "INFO")
    private NiveauNotification niveau;

    @Schema(description = "Statut de lecture", example = "false")
    private Boolean lue;

    @Schema(description = "Date et heure de réception")
    private LocalDateTime dateNotification;

    @Schema(description = "Date et heure de consultation")
    private LocalDateTime dateLecture;

    @Schema(description = "Référence métier associée", example = "PHARMA-12")
    private String referenceId;

    @Schema(description = "Lien de redirection", example = "/admin/moderation/12")
    private String lien;
}

