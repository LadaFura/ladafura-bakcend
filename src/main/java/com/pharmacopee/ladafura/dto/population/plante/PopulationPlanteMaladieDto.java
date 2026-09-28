package com.pharmacopee.ladafura.dto.population.plante;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Maladie ou symptôme associé à la plante médicinale")
public class PopulationPlanteMaladieDto {

    @Schema(description = "Identifiant de la maladie", example = "2")
    private Long id;

    @Schema(description = "Nom de la maladie", example = "Paludisme")
    private String nom;

    @Schema(description = "Description ou symptômes généraux", example = "Fièvre récurrente, frissons, céphalées")
    private String description;
}
