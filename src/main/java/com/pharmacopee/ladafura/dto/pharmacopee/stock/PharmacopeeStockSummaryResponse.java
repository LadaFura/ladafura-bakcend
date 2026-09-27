package com.pharmacopee.ladafura.dto.pharmacopee.stock;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Tableau de synthèse globale de l'inventaire et des stocks de l'officine")
public class PharmacopeeStockSummaryResponse {

    @Schema(description = "Nombre total de références associées à l'officine", example = "24")
    private int totalReferences;

    @Schema(description = "Nombre de références en stock (quantité > 0)", example = "20")
    private int totalEnStock;

    @Schema(description = "Nombre de références en rupture totale (quantité <= 0)", example = "4")
    private int totalRupture;

    @Schema(description = "Nombre de références actuellement activées à la vente", example = "19")
    private int totalActifsVente;

    @Schema(description = "Nombre de références temporairement désactivées", example = "5")
    private int totalDesactives;

    @Schema(description = "Valeur monétaire marchande totale du stock actuel (FCFA)", example = "750000.0")
    private double valeurTotaleStock;
}
