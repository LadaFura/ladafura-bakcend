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
@Schema(description = "Fiche détaillée complète d'une plante médicinale pour la Population avec stricte distinction savoirs traditionnels / études scientifiques")
public class PopulationPlanteDetailResponse {

    @Schema(description = "Identifiant de la plante", example = "1")
    private Long id;

    @Schema(description = "Nom scientifique botanique", example = "Combretum micranthum")
    private String nomScientifique;

    @Schema(description = "Description botanique générale", example = "Arbuste buissonnant des régions sahéliennes et soudanaises...")
    private String description;

    @Schema(description = "URL de la photo officielle", example = "https://ladafura.ml/uploads/plantes/kinkeliba.jpg")
    private String photoUrl;

    @Schema(description = "Avertissement de non-substitution médicale et clarification scientifique",
            example = "Les savoirs traditionnels répertoriés sont issus de la pharmacopée malienne et du patrimoine culturel. Ils ne constituent en aucun cas une preuve scientifique d'efficacité clinique ni une prescription médicale. Consultez toujours un professionnel de santé.")
    private String avertissementMedical;

    @Schema(description = "Noms vernaculaires dans les langues locales maliennes")
    private List<PopulationNomVernaculaireDto> nomsVernaculaires;

    @Schema(description = "Connaissances et savoirs traditionnels validés (séparés des études scientifiques)")
    private List<PopulationConnaissanceTraditionnelleDto> connaissancesTraditionnelles;

    @Schema(description = "Études scientifiques universitaires et publications cliniques répertoriées")
    private List<PopulationEtudeScientifiqueDto> etudesScientifiques;

    @Schema(description = "Maladies ou indications traditionnelles associées")
    private List<PopulationPlanteMaladieDto> maladiesAssociees;

    @Schema(description = "Médias disponibles (photos officielles, photos et enregistrements audio de terrain)")
    private List<PopulationPlanteMediaDto> medias;

    @Schema(description = "Localités maliennes répertoriées où la plante est présente")
    private List<PopulationPlanteLocaliteDto> localites;
}
