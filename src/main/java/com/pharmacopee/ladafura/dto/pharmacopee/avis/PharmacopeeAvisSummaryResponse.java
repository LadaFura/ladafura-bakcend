package com.pharmacopee.ladafura.dto.pharmacopee.avis;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Synthèse et statistiques de satisfaction clients sur les produits de l'officine")
public class PharmacopeeAvisSummaryResponse {

    @Schema(description = "Nombre total d'avis publiés par la modération", example = "42")
    private long totalAvisPublies;

    @Schema(description = "Note moyenne globale sur 5.0", example = "4.7")
    private Double noteMoyenneGlobale;

    @Schema(description = "Nombre de produits différents ayant reçu au moins un avis", example = "8")
    private int nombreProduitsEvalues;

    @Schema(description = "Nombre d'avis 5 étoiles (Excellent)", example = "30")
    private long total5Etoiles;

    @Schema(description = "Nombre d'avis 4 étoiles (Très bon)", example = "8")
    private long total4Etoiles;

    @Schema(description = "Nombre d'avis 3 étoiles (Moyen)", example = "3")
    private long total3Etoiles;

    @Schema(description = "Nombre d'avis 2 étoiles (Décevant)", example = "1")
    private long total2Etoiles;

    @Schema(description = "Nombre d'avis 1 étoile (Insatisfaisant)", example = "0")
    private long total1Etoile;
}
