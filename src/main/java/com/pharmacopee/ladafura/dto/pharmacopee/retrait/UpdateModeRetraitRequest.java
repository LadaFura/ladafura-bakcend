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
@Schema(description = "Formulaire de mise à jour d'un mode de mise à disposition spécifique (Livraison ou Pickup)")
public class UpdateModeRetraitRequest {

    @NotNull(message = "Le statut d'activation (actif) est obligatoire")
    @Schema(description = "Activer (true) ou désactiver (false) ce mode", example = "true")
    private Boolean actif;

    @PositiveOrZero(message = "Les frais doivent être positifs ou nuls")
    @Schema(description = "Frais applicables en FCFA (0 pour gratuit / retrait comptoir)", example = "1500.0")
    private Double frais;
}
