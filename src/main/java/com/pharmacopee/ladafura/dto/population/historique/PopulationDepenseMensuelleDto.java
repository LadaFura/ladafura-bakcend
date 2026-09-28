package com.pharmacopee.ladafura.dto.population.historique;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Dépenses mensuelles agrégées du client")
public class PopulationDepenseMensuelleDto {

    @Schema(description = "Année", example = "2026")
    private int annee;

    @Schema(description = "Numéro du mois (1 à 12)", example = "9")
    private int mois;

    @Schema(description = "Libellé lisible du mois", example = "Septembre 2026")
    private String libelleMois;

    @Schema(description = "Montant total dépensé durant le mois en FCFA", example = "25000.0")
    private Double montant;

    @Schema(description = "Nombre de commandes / transactions du mois", example = "4")
    private long nombreTransactions;
}
