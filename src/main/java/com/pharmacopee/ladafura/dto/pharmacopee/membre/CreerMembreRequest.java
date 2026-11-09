package com.pharmacopee.ladafura.dto.pharmacopee.membre;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Création du compte d'une personne sans compte LADAFURA, ajoutée directement comme membre actif")
public class CreerMembreRequest {

    @NotBlank(message = "Le nom est obligatoire")
    @Schema(example = "Sissoko")
    private String nom;

    @NotBlank(message = "Le prénom est obligatoire")
    @Schema(example = "Awa")
    private String prenom;

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "L'email n'est pas valide")
    @Schema(example = "awa.sissoko@gmail.com")
    private String email;

    @NotBlank(message = "Le téléphone est obligatoire")
    @Schema(example = "+223 70 00 00 00")
    private String telephone;

    @NotBlank(message = "Le mot de passe initial est obligatoire")
    @Size(min = 6, message = "Le mot de passe doit contenir au moins 6 caractères")
    @Schema(description = "Mot de passe initial, transmis à Firebase et jamais stocké")
    private String motDePasse;

    @NotEmpty(message = "Au moins un rôle est obligatoire")
    private List<Long> roleIds;

    @Size(max = 100, message = "Le poste ne doit pas dépasser 100 caractères")
    @Schema(example = "Vendeuse")
    private String poste;
}
