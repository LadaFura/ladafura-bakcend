package com.pharmacopee.ladafura.dto.agent.connaissance;

import java.util.HashSet;
import java.util.Set;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Requête d'enregistrement d'une connaissance traditionnelle recueillie sur le terrain")
public class AgentConnaissanceRequest {

    @NotNull(message = "L'identifiant de la collecte est obligatoire")
    @Schema(description = "Identifiant de la fiche de collecte de rattachement", example = "12", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long collecteId;

    @NotNull(message = "L'identifiant de la plante est obligatoire")
    @Schema(description = "Identifiant de la plante concernée", example = "5", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long planteId;

    @NotBlank(message = "L'usage traditionnel rapporté est obligatoire")
    @Size(max = 2000, message = "L'usage rapporté ne peut pas dépasser 2000 caractères")
    @Schema(description = "Usage traditionnel rapporté sur le terrain par la source (thérapeute/herboriste). Note : information empirique distincte d'une validation clinique.",
            example = "Utilisé traditionnellement en infusion pour soulager les fièvres paludéennes et les maux d'estomac.",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String usageRapporte;

    @Size(max = 255, message = "La partie utilisée ne peut pas dépasser 255 caractères")
    @Schema(description = "Organe ou partie de la plante utilisée (ex: Feuilles séchées, Écorce de racine, Fleurs, Graines)", example = "Feuilles séchées")
    private String partieUtilisee;

    @Size(max = 500, message = "Le mode de préparation ne peut pas dépasser 500 caractères")
    @Schema(description = "Mode de préparation traditionnel (ex: Décoction, Infusion, Poudre, Macération aqueuse)", example = "Décoction de 20g de feuilles dans 1L d'eau pendant 15 minutes")
    private String preparation;

    @Size(max = 1000, message = "Les précautions ne peuvent pas dépasser 1000 caractères")
    @Schema(description = "Précautions d'emploi, contre-indications ou posologie rapportées", example = "Déconseillé aux femmes enceintes. Ne pas dépasser 2 tasses par jour.")
    private String precaution;

    @Size(max = 2000, message = "La description ne peut pas dépasser 2000 caractères")
    @Schema(description = "Observations complémentaires de terrain ou contexte de recueil", example = "Recueilli auprès des anciens du village de Finkolo pendant la saison des pluies.")
    private String description;

    @Schema(description = "Identifiant de la source interrogée sur le terrain (thérapeute traditionnel ou herboriste)", example = "3")
    private Long sourceId;

    @Builder.Default
    @Schema(description = "Identifiants des maladies ou symptômes traditionnellement ciblés selon la tradition orale", example = "[1, 2]")
    private Set<Long> maladieIds = new HashSet<>();
}
