package com.pharmacopee.ladafura.dto.agent.connaissance;

import java.util.HashSet;
import java.util.Set;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Requête de modification d'une connaissance traditionnelle recueillie sur le terrain")
public class AgentConnaissanceUpdateRequest {

    @NotBlank(message = "L'usage traditionnel rapporté est obligatoire")
    @Size(max = 2000, message = "L'usage rapporté ne peut pas dépasser 2000 caractères")
    @Schema(description = "Usage traditionnel rapporté sur le terrain", example = "Utilisé traditionnellement en décoction pour traiter les fièvres intermittentes.", requiredMode = Schema.RequiredMode.REQUIRED)
    private String usageRapporte;

    @Size(max = 255, message = "La partie utilisée ne peut pas dépasser 255 caractères")
    @Schema(description = "Organe ou partie de la plante utilisée", example = "Écorce de racine")
    private String partieUtilisee;

    @Size(max = 500, message = "Le mode de préparation ne peut pas dépasser 500 caractères")
    @Schema(description = "Mode de préparation traditionnel", example = "Décoction prolongée à feu doux")
    private String preparation;

    @Size(max = 1000, message = "Les précautions ne peuvent pas dépasser 1000 caractères")
    @Schema(description = "Précautions d'emploi ou contre-indications rapportées", example = "Contre-indiqué chez les enfants de moins de 5 ans.")
    private String precaution;

    @Size(max = 2000, message = "La description ne peut pas dépasser 2000 caractères")
    @Schema(description = "Observations complémentaires de terrain", example = "Écorce récoltée au lever du jour selon les indications du praticien.")
    private String description;

    @Schema(description = "Identifiant de la source interrogée", example = "3")
    private Long sourceId;

    @Builder.Default
    @Schema(description = "Identifiants des maladies ou symptômes traditionnellement ciblés", example = "[1]")
    private Set<Long> maladieIds = new HashSet<>();
}
