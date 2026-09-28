package com.pharmacopee.ladafura.dto.population.produit;

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
@Schema(description = "Plante entrant dans la composition d'un produit traditionnel")
public class PopulationCompositionItemDto {

    @Schema(description = "Identifiant de la plante", example = "1")
    private Long planteId;

    @Schema(description = "Nom scientifique de la plante", example = "Combretum micranthum")
    private String nomScientifique;

    @Schema(description = "Noms vernaculaires de la plante", example = "[\"Kinkéliba\"]")
    private List<String> nomsVernaculaires;

    @Schema(description = "Quantité entrant dans la formulation", example = "50.0")
    private Double quantite;

    @Schema(description = "Unité de mesure de la quantité", example = "g")
    private String unite;

    @Schema(description = "URL de la photo de la plante", example = "https://ladafura.ml/uploads/kinkeliba.jpg")
    private String photoUrl;
}
