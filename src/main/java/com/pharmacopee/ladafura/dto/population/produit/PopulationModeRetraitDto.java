package com.pharmacopee.ladafura.dto.population.produit;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Mode de retrait proposé par une officine (Livraison ou Pickup)")
public class PopulationModeRetraitDto {

    @Schema(description = "Identifiant du mode de retrait", example = "1")
    private Long id;

    @Schema(description = "Type de retrait (LIVRAISON ou PICKUP)", example = "LIVRAISON")
    private String type;

    @Schema(description = "Indique si le mode est actuellement actif", example = "true")
    private Boolean actif;

    @Schema(description = "Frais applicables en FCFA (0 pour le Pickup)", example = "1500.0")
    private Double frais;
}
