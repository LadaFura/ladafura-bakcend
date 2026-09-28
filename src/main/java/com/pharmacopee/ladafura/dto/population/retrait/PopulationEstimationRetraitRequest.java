package com.pharmacopee.ladafura.dto.population.retrait;

import com.pharmacopee.ladafura.enums.TypeModeRetrait;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Requête de vérification d'éligibilité et de calcul de frais pour un mode de retrait")
public class PopulationEstimationRetraitRequest {

    @NotNull(message = "L'identifiant de la pharmacopée est obligatoire")
    @Schema(description = "Identifiant de la pharmacopée concernée", example = "1")
    private Long pharmacopeeId;

    @NotNull(message = "Le type de mode de retrait est obligatoire")
    @Schema(description = "Type de mode souhaité : LIVRAISON ou PICKUP", example = "LIVRAISON")
    private TypeModeRetrait type;
}
