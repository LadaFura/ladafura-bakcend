package com.pharmacopee.ladafura.dto.population.avis;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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
@Schema(description = "Demande de publication d'un avis client et d'une note sur un produit acheté")
public class PopulationCreateAvisRequest {

    @NotNull(message = "L'identifiant du produit est obligatoire")
    @Schema(description = "Identifiant du produit commandé et reçu", example = "5")
    private Long produitId;

    @NotNull(message = "La note est obligatoire")
    @Min(value = 1, message = "La note minimale est de 1")
    @Max(value = 5, message = "La note maximale est de 5")
    @Schema(description = "Note attribuée au produit (de 1 à 5 étoiles)", example = "5")
    private Integer note;

    @Size(max = 2000, message = "Le commentaire ne peut pas dépasser 2000 caractères")
    @Schema(description = "Commentaire ou retour d'expérience sur le produit", example = "Très efficace pour soulager les symptômes, je recommande vivement !")
    private String commentaire;
}
