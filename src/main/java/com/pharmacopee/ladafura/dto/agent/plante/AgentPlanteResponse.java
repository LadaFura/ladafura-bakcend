package com.pharmacopee.ladafura.dto.agent.plante;

import java.util.ArrayList;
import java.util.List;

import com.pharmacopee.ladafura.dto.admin.plante.NomPlanteDto;
import com.pharmacopee.ladafura.enums.StatutPlante;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Informations sur une plante médicinale accessible à l'Agent de Collecte")
public class AgentPlanteResponse {

    @Schema(description = "Identifiant unique de la plante", example = "1")
    private Long id;

    @Schema(description = "Nom scientifique botanique", example = "Combretum micranthum")
    private String nomScientifique;

    @Schema(description = "Description botanique et caractéristiques visuelles", example = "Arbuste buissonnant à feuilles caduques poussant dans les zones sahéliennes.")
    private String description;

    @Schema(description = "URL de l'image de référence", example = "https://storage.ladafura.ml/plantes/kinkeliba.jpg")
    private String photoUrl;

    @Schema(description = "Statut de modération de la fiche plante (BROUILLON, EN_VALIDATION, VALIDEE, REJETEE)", example = "VALIDEE")
    private StatutPlante statut;

    @Builder.Default
    @Schema(description = "Liste des noms vernaculaires enregistrés dans les langues locales")
    private List<NomPlanteDto> nomsVernaculaires = new ArrayList<>();

    @Schema(description = "Nombre de vertus ou connaissances traditionnelles associées à cette plante", example = "5")
    private int nombreConnaissancesTraditionnelles;
}
