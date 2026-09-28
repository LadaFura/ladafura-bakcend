package com.pharmacopee.ladafura.dto.pharmacopee.produit;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Mise à jour de la disponibilité et du stock d'un produit dans la pharmacopée")
public class UpdateProduitDisponibiliteRequest {

    @Min(value = 0, message = "La quantité en stock ne peut pas être négative")
    @Schema(description = "Quantité en stock physique", example = "35")
    private Integer quantiteStock;

    @Schema(description = "Activer ou désactiver la mise en vente de ce produit dans votre pharmacopée", example = "true")
    private Boolean disponible;
}
