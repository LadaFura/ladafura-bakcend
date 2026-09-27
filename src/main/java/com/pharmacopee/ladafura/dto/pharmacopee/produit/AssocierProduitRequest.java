package com.pharmacopee.ladafura.dto.pharmacopee.produit;

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
@Schema(description = "Demande d'association d'un produit du catalogue à la pharmacopée")
public class AssocierProduitRequest {

    @NotNull(message = "L'identifiant du produit est obligatoire")
    @Schema(description = "Identifiant du produit du catalogue à référencer dans son officine", example = "5")
    private Long produitId;

    @Builder.Default
    @Min(value = 0, message = "La quantité initiale en stock ne peut pas être négative")
    @Schema(description = "Quantité initiale disponible en stock (défaut: 0)", example = "20")
    private Integer quantiteStock = 0;

    @Builder.Default
    @Schema(description = "Indique si le produit est immédiatement actif à la vente (défaut: true)", example = "true")
    private Boolean disponible = true;
}
