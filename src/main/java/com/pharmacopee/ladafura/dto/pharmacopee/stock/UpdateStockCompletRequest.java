package com.pharmacopee.ladafura.dto.pharmacopee.stock;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Formulaire de mise à jour globale de la gestion stock, tarification et disponibilité")
public class UpdateStockCompletRequest {

    @Min(value = 0, message = "La quantité en stock ne peut pas être négative")
    @Schema(description = "Quantité en stock physique", example = "40")
    private Integer quantiteStock;

    @Positive(message = "Le prix de vente doit être strictement positif s'il est spécifié")
    @Schema(description = "Prix de vente spécifique en FCFA (optionnel)", example = "2750.0")
    private Double prix;

    @Schema(description = "Activation ou suspension de la mise en vente", example = "true")
    private Boolean disponible;
}
