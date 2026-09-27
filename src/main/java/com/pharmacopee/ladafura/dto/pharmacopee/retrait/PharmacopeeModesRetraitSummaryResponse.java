package com.pharmacopee.ladafura.dto.pharmacopee.retrait;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Synthèse et statut des modes de mise à disposition de l'officine")
public class PharmacopeeModesRetraitSummaryResponse {

    @Schema(description = "Identifiant de la pharmacopée", example = "1")
    private Long pharmacopeeId;

    @Schema(description = "Nom de l'officine de pharmacopée", example = "Grande Pharmacopée Traditionnelle du Mandé")
    private String nomPharmacopee;

    @Schema(description = "Configuration détaillée du mode Livraison")
    private ModeRetraitResponse livraison;

    @Schema(description = "Configuration détaillée du mode Pickup (Retrait au comptoir)")
    private ModeRetraitResponse pickup;

    @Schema(description = "Indique si au moins l'un des deux modes est actif pour recevoir des commandes clients", example = "true")
    private boolean auMoinsUnModeActif;

    @Schema(description = "Indique si les deux modes sont simultanément activés", example = "true")
    private boolean tousLesDeuxActifs;
}
