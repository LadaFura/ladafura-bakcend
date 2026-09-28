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
@Schema(description = "Média disponible associé à la plante (photo officielle, photo ou audio terrain)")
public class PopulationPlanteMediaDto {

    @Schema(description = "Type de média", example = "PHOTO_OFFICIELLE")
    private String typeMedia;

    @Schema(description = "URL d'accès au média", example = "https://ladafura.ml/uploads/plantes/kinkeliba.jpg")
    private String url;

    @Schema(description = "Description ou légende du média", example = "Spécimen photographié en milieu naturel")
    private String description;
}
