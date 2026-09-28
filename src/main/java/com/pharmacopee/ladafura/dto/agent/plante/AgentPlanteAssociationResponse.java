package com.pharmacopee.ladafura.dto.agent.plante;

import java.util.ArrayList;
import java.util.List;

import com.pharmacopee.ladafura.enums.StatutValidation;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Représentation de l'association d'une plante à une fiche de collecte")
public class AgentPlanteAssociationResponse {

    @Schema(description = "Identifiant de la fiche de collecte", example = "12")
    private Long collecteId;

    @Schema(description = "Identifiant de la plante identifiée", example = "3")
    private Long planteId;

    @Schema(description = "Nom scientifique de la plante", example = "Combretum micranthum")
    private String nomScientifique;

    @Schema(description = "Description botanique", example = "Arbuste buissonnant des zones rocheuses.")
    private String descriptionPlante;

    @Schema(description = "Photo de référence de la plante", example = "https://storage.ladafura.ml/plantes/kinkeliba.jpg")
    private String photoUrl;

    @Builder.Default
    @Schema(description = "Noms vernaculaires associés à la plante", example = "[\"Kinkéliba (Bambara)\", \"Sekeu (Soninké)\"]")
    private List<String> nomsVernaculaires = new ArrayList<>();

    @Schema(description = "Identifiant de l'enregistrement de liaison", example = "25")
    private Long vertuId;

    @Schema(description = "Usage traditionnel rapporté préliminaire", example = "Traitement des affections biliaires et hépatiques")
    private String usageRapporte;

    @Schema(description = "Statut de validation de la connaissance (BROUILLON, EN_VALIDATION, VALIDE, REJETE)", example = "BROUILLON")
    private StatutValidation statutAssociation;
}
