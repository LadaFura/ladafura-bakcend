package com.pharmacopee.ladafura.dto.population.pharmacopee;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Localisation géographique détaillée d'une officine de pharmacopée")
public class PopulationPharmacopeeLocalisationDto {

    @Schema(description = "Région administrative", example = "Koulikoro")
    private String region;

    @Schema(description = "Cercle administratif", example = "Kati")
    private String cercle;

    @Schema(description = "Commune", example = "Siby")
    private String commune;

    @Schema(description = "Adresse ou localité précise", example = "Quartier Commercial, Rue 12")
    private String localite;

    @Schema(description = "Latitude GPS", example = "12.3847")
    private Double latitude;

    @Schema(description = "Longitude GPS", example = "-8.3341")
    private Double longitude;
}
