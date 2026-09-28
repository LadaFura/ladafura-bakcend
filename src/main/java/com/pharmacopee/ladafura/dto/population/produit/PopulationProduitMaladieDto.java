package com.pharmacopee.ladafura.dto.population.produit;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Maladie ou indication ciblée par les plantes entrant dans le produit")
public class PopulationProduitMaladieDto {

    @Schema(description = "Identifiant de la maladie", example = "2")
    private Long id;

    @Schema(description = "Nom de la maladie ou du trouble", example = "Paludisme")
    private String nom;

    @Schema(description = "Description ou symptômes", example = "Accès fébriles, maux de tête, fatigue")
    private String description;
}
