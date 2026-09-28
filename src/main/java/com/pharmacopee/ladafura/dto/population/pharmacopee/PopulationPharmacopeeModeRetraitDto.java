package com.pharmacopee.ladafura.dto.population.pharmacopee;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Mode de retrait proposé par la pharmacopée (Livraison ou Pickup)")
public class PopulationPharmacopeeModeRetraitDto {

    @Schema(description = "Identifiant du mode de retrait", example = "1")
    private Long id;

    @Schema(description = "Type de mode de retrait (LIVRAISON ou PICKUP)", example = "LIVRAISON")
    private String type;

    @Schema(description = "Indique si ce mode de retrait est actif", example = "true")
    private Boolean actif;

    @Schema(description = "Frais applicables en FCFA (0 pour le Pickup)", example = "1500.0")
    private Double frais;
}
