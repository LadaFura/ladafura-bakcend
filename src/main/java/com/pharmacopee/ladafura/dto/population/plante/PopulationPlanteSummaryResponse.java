package com.pharmacopee.ladafura.dto.population.plante;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Aperçu synthétique d'une plante médicinale validée dans le catalogue Population")
public class PopulationPlanteSummaryResponse {

    @Schema(description = "Identifiant unique de la plante", example = "1")
    private Long id;

    @Schema(description = "Nom scientifique botanique", example = "Combretum micranthum")
    private String nomScientifique;

    @Schema(description = "Description botanique", example = "Arbuste buissonnant des régions sahéliennes...")
    private String description;

    @Schema(description = "URL de la photo officielle", example = "https://ladafura.ml/uploads/plantes/kinkeliba.jpg")
    private String photoUrl;

    @Schema(description = "Noms vernaculaires principaux (ex: Kinkéliba)", example = "[\"Kinkéliba\", \"Kinkeliba\"]")
    private List<String> nomsVernaculaires;

    @Schema(description = "Noms des maladies associées", example = "[\"Paludisme\", \"Hypertension\"]")
    private List<String> maladies;

    @Schema(description = "Nombre de savoirs traditionnels validés disponibles", example = "3")
    private int nombreConnaissances;

    @Schema(description = "Nombre d'études scientifiques disponibles", example = "2")
    private int nombreEtudesScientifiques;
}
