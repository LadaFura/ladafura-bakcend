package com.pharmacopee.ladafura.dto.population.commande;

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
@Schema(description = "Demande d'estimation et récapitulatif d'une commande avant confirmation")
public class PopulationCommandeRecapitulatifRequest {

    @NotNull(message = "L'identifiant de la pharmacopée est obligatoire")
    @Schema(description = "Identifiant de l'officine auprès de laquelle commander", example = "1")
    private Long pharmacopeeId;

    @NotNull(message = "Le mode de retrait est obligatoire")
    @Schema(description = "Identifiant du mode de retrait sélectionné (LIVRAISON ou PICKUP)", example = "2")
    private Long modeRetraitId;
}
