package com.pharmacopee.ladafura.dto.population.profil;

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
@Schema(description = "Données modifiables du profil d'un utilisateur Population (nom, prénom et téléphone uniquement)")
public class PopulationUpdateProfileRequest {

    @NotBlank(message = "Le nom de famille est obligatoire")
    @Size(min = 2, max = 100, message = "Le nom doit comporter entre 2 et 100 caractères")
    @Schema(description = "Nom de famille de l'utilisateur", example = "Diarra")
    private String nom;

    @NotBlank(message = "Le prénom est obligatoire")
    @Size(min = 2, max = 100, message = "Le prénom doit comporter entre 2 et 100 caractères")
    @Schema(description = "Prénom de l'utilisateur", example = "Fatoumata")
    private String prenom;

    @Size(max = 25, message = "Le numéro de téléphone ne doit pas dépasser 25 caractères")
    @Schema(description = "Numéro de téléphone de contact", example = "+223 70 12 34 56")
    private String telephone;
}
