package com.pharmacopee.ladafura.dto.pharmacopee.commande;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Synthèse et tableau de bord des commandes de l'officine")
public class PharmacopeeCommandeSummaryResponse {

    @Schema(description = "Nombre total de commandes reçues", example = "35")
    private long totalCommandes;

    @Schema(description = "Commandes en attente de confirmation", example = "3")
    private long totalEnAttente;

    @Schema(description = "Commandes confirmées à préparer", example = "5")
    private long totalConfirmees;

    @Schema(description = "Commandes préparées en attente d'acheminement ou de retrait", example = "4")
    private long totalPreparees;

    @Schema(description = "Commandes en cours d'acheminement (En livraison ou Disponible en pickup)", example = "6")
    private long totalEnCoursAcheminement;

    @Schema(description = "Commandes finalisées avec succès (Livrées ou Retirées)", example = "15")
    private long totalTerminees;

    @Schema(description = "Commandes annulées", example = "2")
    private long totalAnnulees;

    @Schema(description = "Chiffre d'affaires cumulé sur les commandes terminées (FCFA)", example = "350000.0")
    private double chiffreAffairesTermine;
}
