package com.pharmacopee.ladafura.dto.agent.collecte;

import java.time.LocalDateTime;

import com.pharmacopee.ladafura.enums.StatutCollecte;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Résumé d'une collecte réalisée par l'Agent de Collecte")
public class AgentCollecteSummaryResponse {

    @Schema(description = "Identifiant unique de la collecte", example = "12")
    private Long id;

    @Schema(description = "Date de réalisation de la collecte sur le terrain", example = "2026-09-28T10:30:00")
    private LocalDateTime dateCollecte;

    @Schema(description = "Description synthétique de la collecte", example = "Observation de terrain sur les feuilles de kinkéliba.")
    private String description;

    @Schema(description = "Statut actuel de la collecte dans le workflow", example = "BROUILLON")
    private StatutCollecte statut;

    @Schema(description = "URL de la photo de l'échantillon", example = "https://storage.ladafura.ml/collectes/photos/img_12.jpg")
    private String photoUrl;

    @Schema(description = "URL de l'enregistrement audio", example = "https://storage.ladafura.ml/collectes/audios/rec_12.mp3")
    private String audioUrl;

    @Schema(description = "Date de soumission pour examen administratif", example = "2026-09-28T14:00:00")
    private LocalDateTime dateSoumission;

    @Schema(description = "Motif en cas de rejet par l'administrateur", example = "Coordonnées géographiques imprécises, merci de rectifier.")
    private String motifRejet;

    @Schema(description = "Nom complet de la source rencontrée", example = "Amadou Diarra")
    private String nomSource;

    @Schema(description = "Localité de recueil", example = "Finkolo")
    private String localite;

    @Schema(description = "Cercle administratif", example = "Sikasso")
    private String cercle;

    @Schema(description = "Région administrative", example = "Sikasso")
    private String region;

    @Schema(description = "Nombre de vertus ou connaissances traditionnelles associées", example = "2")
    private int nombreVertus;
}
