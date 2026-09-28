package com.pharmacopee.ladafura.dto.pharmacopee.referencement;

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
@Schema(description = "Formulaire de soumission d'une demande de référencement d'une pharmacopée")
public class DemandeReferencementRequest {

    @NotBlank(message = "Le nom officiel de l'établissement ou de la pharmacopée est obligatoire")
    @Size(min = 2, max = 150, message = "Le nom doit comporter entre 2 et 150 caractères")
    @Schema(description = "Nom de la pharmacopée traditionnelle", example = "Pharmacie Traditionnelle Mandé")
    private String nom;

    @Size(max = 2000, message = "La description ne doit pas dépasser 2000 caractères")
    @Schema(description = "Description des activités et des spécialités", example = "Spécialisée dans les tisanes et décoctions à base de plantes locales du Mandé.")
    private String description;

    @NotBlank(message = "Le numéro de téléphone officiel est obligatoire")
    @Size(min = 8, max = 25, message = "Le numéro de téléphone doit comporter entre 8 et 25 caractères")
    @Schema(description = "Numéro de contact professionnel", example = "+223 20 22 33 44")
    private String telephone;
}
