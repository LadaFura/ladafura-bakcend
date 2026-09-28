package com.pharmacopee.ladafura.dto.population.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
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
@Schema(description = "Formulaire d'inscription d'un nouvel utilisateur de la Population")
public class PopulationRegisterRequest {

    @NotBlank(message = "Le nom est obligatoire")
    @Schema(description = "Nom de famille de l'utilisateur", example = "Diarra")
    private String nom;

    @NotBlank(message = "Le prénom est obligatoire")
    @Schema(description = "Prénom de l'utilisateur", example = "Fatoumata")
    private String prenom;

    @NotBlank(message = "L'adresse email est obligatoire")
    @Email(message = "Format d'adresse email invalide")
    @Schema(description = "Adresse email de l'utilisateur", example = "fatoumata.diarra@gmail.com")
    private String email;

    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 6, message = "Le mot de passe doit comporter au moins 6 caractères")
    @Schema(description = "Mot de passe sécurisé (minimum 6 caractères)", example = "Mali2026!")
    private String motDePasse;

    @Schema(description = "Numéro de téléphone de contact", example = "+223 70 12 34 56")
    private String telephone;
}
