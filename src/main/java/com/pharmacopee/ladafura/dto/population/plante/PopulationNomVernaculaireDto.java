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
@Schema(description = "Nom vernaculaire de la plante dans une langue locale")
public class PopulationNomVernaculaireDto {

    @Schema(description = "Identifiant du nom vernaculaire", example = "1")
    private Long id;

    @Schema(description = "Nom dans la langue locale", example = "Kinkéliba")
    private String nom;

    @Schema(description = "Langue locale malienne", example = "Bambara")
    private String langue;

    @Schema(description = "Pays", example = "Mali")
    private String pays;
}
