package com.pharmacopee.ladafura.dto.pharmacopee.notification;

import com.pharmacopee.ladafura.enums.NiveauNotification;
import com.pharmacopee.ladafura.enums.TypeNotification;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Formulaire de création d'une notification")
public class CreateNotificationRequest {

    @NotBlank(message = "Le titre est obligatoire")
    @Schema(description = "Titre résumé", example = "Rappel de réapprovisionnement")
    private String titre;

    @NotBlank(message = "Le message est obligatoire")
    @Schema(description = "Contenu textuel du message", example = "Le stock de Tisane Kinkéliba est sous le seuil critique.")
    private String message;

    @NotNull(message = "Le type de notification est obligatoire")
    @Schema(description = "Type fonctionnel", example = "STOCK_ALERTE")
    private TypeNotification type;

    @Builder.Default
    @Schema(description = "Niveau de sévérité", example = "ATTENTION")
    private NiveauNotification niveau = NiveauNotification.INFO;

    @Schema(description = "Identifiant de la ressource liée", example = "5")
    private String referenceId;

    @Schema(description = "Lien ou route de redirection", example = "/stock/5")
    private String lien;
}
