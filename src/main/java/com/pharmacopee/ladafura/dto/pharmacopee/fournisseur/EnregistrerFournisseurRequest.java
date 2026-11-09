package com.pharmacopee.ladafura.dto.pharmacopee.fournisseur;

import com.pharmacopee.ladafura.enums.TypeFournisseur;

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
@Schema(description = "Création ou modification d'un fournisseur")
public class EnregistrerFournisseurRequest {
    @NotBlank(message = "Le nom est obligatoire")
    @Schema(example = "Coopérative de Sikasso")
    private String nom;
    private TypeFournisseur typeFournisseur;
    @Size(max = 30, message = "Le téléphone ne doit pas dépasser 30 caractères")
    @Schema(example = "+223 76 00 00 00")
    private String telephone;
    @Email(message = "L'email n'est pas valide")
    private String email;
    private String adresse;
    private String description;
}
