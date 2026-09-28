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
@Schema(description = "Résultat consolidé de la recherche universelle pour la Population")
public class PopulationGlobalSearchResponse {

    @Schema(description = "Mot-clé ou expression recherchée", example = "kinkeliba")
    private String query;

    @Schema(description = "Nombre total cumulé de résultats trouvés", example = "7")
    private int totalResultats;

    @Schema(description = "Plantes médicinales correspondantes")
    private List<PopulationPlanteSearchItem> plantes;

    @Schema(description = "Noms vernaculaires locaux correspondants")
    private List<PopulationVernaculaireSearchItem> nomsVernaculaires;

    @Schema(description = "Maladies ou pathologies correspondantes")
    private List<PopulationMaladieSearchItem> maladies;

    @Schema(description = "Produits traditionnels correspondants")
    private List<PopulationProduitSearchItem> produits;

    @Schema(description = "Officines de pharmacopées correspondantes")
    private List<PopulationPharmacopeeSearchItem> pharmacopees;
}
