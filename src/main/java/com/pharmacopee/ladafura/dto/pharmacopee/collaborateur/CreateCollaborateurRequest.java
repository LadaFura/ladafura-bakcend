package com.pharmacopee.ladafura.dto.pharmacopee.collaborateur;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Requête d'ajout d'un collaborateur à la pharmacopée")
public class CreateCollaborateurRequest {

    @NotBlank(message = "Le nom est obligatoire")
    @Schema(description = "Nom du collaborateur", example = "Sissoko")
    private String nom;

    @NotBlank(message = "Le prénom est obligatoire")
    @Schema(description = "Prénom du collaborateur", example = "Awa")
    private String prenom;

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "L'email n'est pas valide")
    @Schema(description = "Email (utilisé pour la connexion)", example = "awa.sissoko@gmail.com")
    private String email;

    @NotBlank(message = "Le téléphone est obligatoire")
    @Schema(description = "Téléphone de contact", example = "+223 70 00 00 00")
    private String telephone;

    @NotBlank(message = "Le mot de passe initial est obligatoire")
    @Schema(description = "Mot de passe initial du collaborateur", example = "passer123")
    private String motDePasse;

    @Schema(description = "Fonction ou spécialité", example = "Gérante")
    private String specialite;
}
