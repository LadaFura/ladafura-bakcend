package com.pharmacopee.ladafura.dto.population.recherche;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Élément résultat de recherche pour un nom vernaculaire")
public class PopulationVernaculaireSearchItem {

    @Schema(description = "Identifiant du nom vernaculaire", example = "5")
    private Long id;

    @Schema(description = "Nom vernaculaire dans la langue locale", example = "Kinkéliba")
    private String nom;

    @Schema(description = "Langue locale malienne", example = "Bambara")
    private String langue;

    @Schema(description = "Identifiant de la plante associée", example = "1")
    private Long planteId;

    @Schema(description = "Nom scientifique de la plante associée", example = "Combretum micranthum")
    private String nomScientifiquePlante;
}
