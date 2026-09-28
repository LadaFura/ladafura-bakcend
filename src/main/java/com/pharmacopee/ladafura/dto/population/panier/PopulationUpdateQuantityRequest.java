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
@Schema(description = "Requête de modification de la quantité d'une ligne du panier")
public class PopulationUpdateQuantityRequest {

    @NotNull(message = "La nouvelle quantité est obligatoire")
    @Min(value = 1, message = "La quantité minimale autorisée est de 1")
    @Schema(description = "Nouvelle quantité souhaitée pour cet article", example = "3")
    private Integer quantite;
}
