package com.pharmacopee.ladafura.dto.pharmacopee.notification;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Synthèse et indicateurs du centre de notifications de l'officine")
public class NotificationSummaryResponse {

    @Schema(description = "Nombre total de notifications reçues", example = "24")
    private long totalNotifications;

    @Schema(description = "Nombre de notifications non lues (pour le badge de la cloche)", example = "5")
    private long totalNonLues;

    @Schema(description = "Nouvelles commandes non lues", example = "2")
    private long nonLuesCommandes;

    @Schema(description = "Mises à jour de référencement / agrément non lues", example = "0")
    private long nonLuesReferencement;

    @Schema(description = "Nouveaux avis clients non lus", example = "1")
    private long nonLuesAvis;

    @Schema(description = "Alertes de stocks non lues", example = "1")
    private long nonLuesStock;

    @Schema(description = "Informations institutionnelles importantes non lues", example = "1")
    private long nonLuesInfoImportante;
}
