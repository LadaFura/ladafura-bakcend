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
@Schema(description = "Localité géographique malienne où la plante a été recensée")
public class PopulationPlanteLocaliteDto {

    @Schema(description = "Région administrative", example = "Koulikoro")
    private String region;

    @Schema(description = "Cercle administratif", example = "Kati")
    private String cercle;

    @Schema(description = "Commune", example = "Siby")
    private String commune;

    @Schema(description = "Nom de la localité ou village", example = "Djissoumala")
    private String localite;

    @Schema(description = "Latitude GPS", example = "12.3847")
    private Double latitude;

    @Schema(description = "Longitude GPS", example = "-8.3341")
    private Double longitude;
}
