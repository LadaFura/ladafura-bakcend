package com.pharmacopee.ladafura.dto.population.favori;

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
@Schema(description = "Synthèse et comptage des favoris de l'utilisateur")
public class PopulationFavoriCountResponse {

    @Schema(description = "Nombre total de favoris enregistrés", example = "8")
    private long total;

    @Schema(description = "Nombre total de plantes en favori", example = "3")
    private long totalPlantes;

    @Schema(description = "Nombre total de produits en favori", example = "4")
    private long totalProduits;

    @Schema(description = "Nombre total de pharmacopées en favori", example = "1")
    private long totalPharmacopees;
}
