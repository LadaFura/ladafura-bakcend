package com.pharmacopee.ladafura.dto.agent.source;

import com.pharmacopee.ladafura.enums.Role;

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
@Schema(description = "Requête d'enregistrement d'une nouvelle source d'information de terrain (thérapeute traditionnel ou herboriste)")
public class AgentCreateSourceRequest {

    @NotBlank(message = "Le nom de famille de la source est obligatoire")
    @Size(max = 100, message = "Le nom ne peut pas dépasser 100 caractères")
    @Schema(description = "Nom de famille de la source de terrain", example = "Diarra", requiredMode = Schema.RequiredMode.REQUIRED)
    private String nom;

    @NotBlank(message = "Le prénom de la source est obligatoire")
    @Size(max = 100, message = "Le prénom ne peut pas dépasser 100 caractères")
    @Schema(description = "Prénom de la source de terrain", example = "Amadou", requiredMode = Schema.RequiredMode.REQUIRED)
    private String prenom;

    @Size(max = 30, message = "Le numéro de téléphone ne peut pas dépasser 30 caractères")
    @Schema(description = "Numéro de téléphone de contact pour traçabilité", example = "+223 70 22 33 44")
    private String telephone;

    @Schema(description = "Adresse email de contact (optionnel, une référence unique traçable sera générée si omise)", example = "amadou.diarra@village.ml")
    private String email;

    @Size(max = 255, message = "L'adresse ne peut pas dépasser 255 caractères")
    @Schema(description = "Adresse ou village de résidence de la source", example = "Village de Finkolo, Commune de Finkolo, Cercle de Koutiala")
    private String adresse;

    @Size(max = 255, message = "La spécialité ne peut pas dépasser 255 caractères")
    @Schema(description = "Spécialité traditionnelle ou savoir-faire particulier", example = "Tradipraticien spécialisé dans le traitement des fièvres et ictères")
    private String specialite;

    @Schema(description = "Nombre d'années d'expérience dans la pratique traditionnelle", example = "25")
    private Integer anneesExperience;

    @Builder.Default
    @Schema(description = "Statut ou profil de la source (THERAPEUTE ou HERBORISTE)", example = "THERAPEUTE")
    private Role role = Role.THERAPEUTE;

    @Schema(description = "Identifiant optionnel d'une fiche de collecte à associer immédiatement", example = "12")
    private Long collecteId;
}
