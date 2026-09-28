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
@Schema(description = "Données pour la modification d'une collecte (autorisée uniquement si statut BROUILLON ou REJETEE)")
public class AgentUpdateCollecteRequest {

    @Schema(description = "Date et heure de la collecte sur le terrain", example = "2026-09-28T10:30:00")
    private LocalDateTime dateCollecte;

    @Size(max = 4000, message = "La description ne doit pas dépasser 4000 caractères")
    @Schema(description = "Description mise à jour des observations", example = "Correction des indications sur la récolte des écorces et feuilles.")
    private String description;

    @Schema(description = "URL ou chemin de la photo mise à jour", example = "https://storage.ladafura.ml/collectes/photos/finkolo_01_corrige.jpg")
    private String photoUrl;

    @Schema(description = "URL ou chemin de l'audio mis à jour", example = "https://storage.ladafura.ml/collectes/audios/rec_finkolo_01.mp3")
    private String audioUrl;

    @Schema(description = "Identifiant de la source interrogée", example = "3")
    private Long sourceId;

    @Schema(description = "Identifiant de la localisation géographique", example = "5")
    private Long localisationId;
}
