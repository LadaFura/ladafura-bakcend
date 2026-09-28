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
@Schema(description = "Demande de modification d'un avis client existant")
public class PopulationUpdateAvisRequest {

    @NotNull(message = "La note est obligatoire")
    @Min(value = 1, message = "La note minimale est de 1")
    @Max(value = 5, message = "La note maximale est de 5")
    @Schema(description = "Nouvelle note attribuée (de 1 à 5 étoiles)", example = "4")
    private Integer note;

    @Size(max = 2000, message = "Le commentaire ne peut pas dépasser 2000 caractères")
    @Schema(description = "Nouveau commentaire", example = "Efficace, délai de livraison satisfaisant.")
    private String commentaire;
}
