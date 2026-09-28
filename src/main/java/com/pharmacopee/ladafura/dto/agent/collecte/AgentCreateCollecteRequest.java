package com.pharmacopee.ladafura.dto.agent.collecte;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Données pour la création d'une nouvelle fiche de collecte sur le terrain")
public class AgentCreateCollecteRequest {

    @Schema(description = "Date et heure de la collecte sur le terrain (par défaut date actuelle si omise)", example = "2026-09-28T10:30:00")
    private LocalDateTime dateCollecte;

    @Size(max = 4000, message = "La description ne doit pas dépasser 4000 caractères")
    @Schema(description = "Description générale des observations et conditions de collecte sur le terrain", example = "Échantillons de feuilles fraîches récoltés en lisière de forêt près du village de Finkolo.")
    private String description;

    @Schema(description = "URL ou chemin de la photo de la plante ou de l'échantillon", example = "https://storage.ladafura.ml/collectes/photos/finkolo_01.jpg")
    private String photoUrl;

    @Schema(description = "URL ou chemin de l'enregistrement audio des déclarations de la source", example = "https://storage.ladafura.ml/collectes/audios/rec_finkolo_01.mp3")
    private String audioUrl;

    @Schema(description = "Identifiant de la source interrogée (thérapeute traditionnel ou herboriste, optionnel)", example = "3")
    private Long sourceId;

    @Schema(description = "Identifiant de la localisation géographique associée (optionnel)", example = "5")
    private Long localisationId;

    @Builder.Default
    @Schema(description = "Indique si la collecte doit être immédiatement soumise (true) ou enregistrée comme brouillon (false)", example = "false")
    private boolean soumettre = false;
}
