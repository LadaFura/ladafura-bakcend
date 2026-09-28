package com.pharmacopee.ladafura.dto.population.recherche;

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
@Schema(description = "Élément résultat de recherche pour une plante médicinale")
public class PopulationPlanteSearchItem {

    @Schema(description = "Identifiant de la plante", example = "1")
    private Long id;

    @Schema(description = "Nom scientifique de la plante", example = "Combretum micranthum")
    private String nomScientifique;

    @Schema(description = "Description botanique ou thérapeutique", example = "Arbuste buissonnant des savanes d'Afrique de l'Ouest...")
    private String description;

    @Schema(description = "URL de la photo de la plante", example = "https://storage.ladafura.ml/plantes/kinkeliba.jpg")
    private String photoUrl;

    @Schema(description = "Noms vernaculaires locaux principaux", example = "[\"Kinkéliba (Bambara)\", \"Sékéou (Peul)\"]")
    private List<String> nomsVernaculaires;
}
