package com.pharmacopee.ladafura.dto.pharmacopee.stock;

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
@Schema(description = "Mise à jour directe de la quantité en stock physique")
public class UpdateStockRequest {

    @NotNull(message = "La quantité en stock est obligatoire")
    @Min(value = 0, message = "La quantité en stock ne peut pas être négative")
    @Schema(description = "Nouvelle quantité physique disponible en stock", example = "50")
    private Integer quantiteStock;

    @jakarta.validation.constraints.Size(max = 255, message = "Le motif ne doit pas dépasser 255 caractères")
    @Schema(description = "Motif de l'ajustement de stock (facultatif)", example = "Inventaire de fin de mois")
    private String motif;
}
