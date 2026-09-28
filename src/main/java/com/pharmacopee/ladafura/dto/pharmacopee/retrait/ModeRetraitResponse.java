package com.pharmacopee.ladafura.dto.pharmacopee.retrait;

import com.pharmacopee.ladafura.enums.TypeModeRetrait;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Détail de configuration d'un mode de mise à disposition (Livraison ou Pickup)")
public class ModeRetraitResponse {

    @Schema(description = "Identifiant unique du mode configuré", example = "1")
    private Long id;

    @Schema(description = "Type de mode de mise à disposition : LIVRAISON ou PICKUP", example = "LIVRAISON")
    private TypeModeRetrait type;

    @Schema(description = "Libellé convivial du mode", example = "Livraison à domicile")
    private String libelle;

    @Schema(description = "Indique si le mode est actuellement activé par l'officine", example = "true")
    private Boolean actif;

    @Schema(description = "Frais applicables en FCFA (0 pour gratuit / retrait comptoir)", example = "1500.0")
    private Double frais;
}
