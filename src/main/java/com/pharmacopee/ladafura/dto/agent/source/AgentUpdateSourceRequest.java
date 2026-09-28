package com.pharmacopee.ladafura.dto.agent.source;

import com.pharmacopee.ladafura.enums.Role;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Requête de mise à jour des coordonnées et informations d'une source de terrain")
public class AgentUpdateSourceRequest {

    @Size(max = 100, message = "Le nom ne peut pas dépasser 100 caractères")
    @Schema(description = "Nom de famille", example = "Diarra")
    private String nom;

    @Size(max = 100, message = "Le prénom ne peut pas dépasser 100 caractères")
    @Schema(description = "Prénom", example = "Amadou")
    private String prenom;

    @Size(max = 30, message = "Le numéro de téléphone ne peut pas dépasser 30 caractères")
    @Schema(description = "Numéro de téléphone de contact", example = "+223 70 22 33 44")
    private String telephone;

    @Schema(description = "Adresse email de contact", example = "amadou.diarra@village.ml")
    private String email;

    @Size(max = 255, message = "L'adresse ne peut pas dépasser 255 caractères")
    @Schema(description = "Adresse ou village de résidence", example = "Village de Finkolo, Cercle de Koutiala")
    private String adresse;

    @Size(max = 255, message = "La spécialité ne peut pas dépasser 255 caractères")
    @Schema(description = "Spécialité ou domaine de compétence", example = "Herboriste tradipraticien certifié localement")
    private String specialite;

    @Schema(description = "Nombre d'années d'expérience", example = "30")
    private Integer anneesExperience;

    @Schema(description = "Profil de la source (THERAPEUTE ou HERBORISTE)", example = "HERBORISTE")
    private Role role;
}
