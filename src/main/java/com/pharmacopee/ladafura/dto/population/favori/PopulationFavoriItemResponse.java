package com.pharmacopee.ladafura.dto.population.favori;

import java.time.LocalDateTime;

import com.pharmacopee.ladafura.dto.population.pharmacopee.PopulationPharmacopeeSummaryResponse;
import com.pharmacopee.ladafura.dto.population.plante.PopulationPlanteSummaryResponse;
import com.pharmacopee.ladafura.dto.population.produit.PopulationProduitSummaryResponse;
import com.pharmacopee.ladafura.enums.TypeFavori;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Élément d'historique de favori avec contenu détaillé")
public class PopulationFavoriItemResponse {

    @Schema(description = "Identifiant unique de la ligne de favori", example = "10")
    private Long id;

    @Schema(description = "Type du favori (PLANTE, PRODUIT, PHARMACOPEE)", example = "PLANTE")
    private TypeFavori type;

    @Schema(description = "Date et heure de mise en favori", example = "2026-09-28T14:30:00")
    private LocalDateTime dateAjout;

    @Schema(description = "Résumé de la plante (si type == PLANTE)")
    private PopulationPlanteSummaryResponse plante;

    @Schema(description = "Résumé du produit (si type == PRODUIT)")
    private PopulationProduitSummaryResponse produit;

    @Schema(description = "Résumé de la pharmacopée (si type == PHARMACOPEE)")
    private PopulationPharmacopeeSummaryResponse pharmacopee;
}
