package com.pharmacopee.ladafura.dto.agent.media;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Récapitulatif des médias (photos, audios) rattachés à une fiche de collecte")
public class AgentMediaResponse {

    @Schema(description = "Identifiant de la collecte", example = "12")
    private Long collecteId;

    @Schema(description = "URL de la photo associée", example = "/uploads/collectes/photos/f9b2c3d4_kinkeliba.jpg")
    private String photoUrl;

    @Schema(description = "URL de l'enregistrement audio associé", example = "/uploads/collectes/audios/a1b2c3d4_temoignage.mp3")
    private String audioUrl;

    @Schema(description = "Indique si une photo est présente", example = "true")
    private boolean photoPresente;

    @Schema(description = "Indique si un enregistrement audio est présent", example = "true")
    private boolean audioPresent;

    @Schema(description = "Description ou message de retour", example = "Médias synchronisés avec succès")
    private String message;
}
