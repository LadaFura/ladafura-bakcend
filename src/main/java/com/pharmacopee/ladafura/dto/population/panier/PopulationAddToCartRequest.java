package com.pharmacopee.ladafura.dto.population.panier;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Requête d'ajout d'un produit au panier")
public class PopulationAddToCartRequest {

    @NotNull(message = "L'identifiant du produit est obligatoire")
    @Schema(description = "Identifiant unique du produit à ajouter", example = "5")
    private Long produitId;

    @NotNull(message = "La quantité est obligatoire")
    @Min(value = 1, message = "La quantité minimale à ajouter est de 1")
    @Builder.Default
    @Schema(description = "Quantité d'unités souhaitée", example = "2", defaultValue = "1")
    private Integer quantite = 1;
}
