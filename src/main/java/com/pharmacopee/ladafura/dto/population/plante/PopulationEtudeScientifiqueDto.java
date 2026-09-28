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
@Schema(description = "Étude scientifique universitaire ou clinique disponible sur la plante")
public class PopulationEtudeScientifiqueDto {

    @Schema(description = "Identifiant de l'étude scientifique", example = "3")
    private Long id;

    @Schema(description = "Titre de la publication scientifique", example = "Propriétés antioxydantes et hépato-protectrices de Combretum micranthum")
    private String titre;

    @Schema(description = "Auteurs ou chercheurs", example = "Dr. Sanogo R., Diallo D., et al.")
    private String auteurs;

    @Schema(description = "Année de publication", example = "2021")
    private Integer annee;

    @Schema(description = "Référence bibliographique / Journal", example = "Journal of Ethnopharmacology, Vol 142")
    private String reference;

    @Schema(description = "Résumé de l'étude", example = "L'étude démontre in vitro une activité hépatoprotectrice significative...")
    private String resume;

    @Schema(description = "Lien ou document PDF de l'étude", example = "https://ladafura.ml/etudes/combretum-2021.pdf")
    private String documentUrl;
}
