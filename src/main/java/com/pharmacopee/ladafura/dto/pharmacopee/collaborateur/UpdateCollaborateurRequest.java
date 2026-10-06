package com.pharmacopee.ladafura.dto.pharmacopee.collaborateur;

import com.pharmacopee.ladafura.enums.StatutUtilisateur;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Requête de mise à jour d'un collaborateur")
public class UpdateCollaborateurRequest {

    @NotBlank(message = "Le nom est obligatoire")
    @Schema(description = "Nom du collaborateur", example = "Sissoko")
    private String nom;

    @NotBlank(message = "Le prénom est obligatoire")
    @Schema(description = "Prénom du collaborateur", example = "Awa")
    private String prenom;

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "L'email n'est pas valide")
    @Schema(description = "Email", example = "awa.sissoko@gmail.com")
    private String email;

    @NotBlank(message = "Le téléphone est obligatoire")
    @Schema(description = "Téléphone de contact", example = "+223 70 00 00 00")
    private String telephone;

    @Schema(description = "Fonction ou spécialité", example = "Assistante")
    private String specialite;

    @NotNull(message = "Le statut est obligatoire")
    @Schema(description = "Statut d'activation du compte", example = "ACTIF")
    private StatutUtilisateur statut;
}
