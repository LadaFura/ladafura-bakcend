package com.pharmacopee.ladafura.dto.pharmacopee.retrait;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Formulaire de configuration simultanée des deux modes de mise à disposition (Livraison et Pickup)")
public class ConfigureModesRetraitGlobalRequest {

    @NotNull(message = "L'activation du mode Livraison est obligatoire (true ou false)")
    @Schema(description = "Activer (true) ou désactiver (false) le mode Livraison", example = "true")
    private Boolean livraisonActif;

    @PositiveOrZero(message = "Les frais de livraison doivent être positifs ou nuls")
    @Schema(description = "Frais de livraison en FCFA", example = "1500.0")
    private Double livraisonFrais;

    @NotNull(message = "L'activation du mode Pickup est obligatoire (true ou false)")
    @Schema(description = "Activer (true) ou désactiver (false) le mode Pickup (Retrait au comptoir)", example = "true")
    private Boolean pickupActif;

    @PositiveOrZero(message = "Les frais de pickup doivent être positifs ou nuls")
    @Schema(description = "Frais de retrait au comptoir en FCFA (par défaut 0.0 FCFA)", example = "0.0")
    private Double pickupFrais;
}
