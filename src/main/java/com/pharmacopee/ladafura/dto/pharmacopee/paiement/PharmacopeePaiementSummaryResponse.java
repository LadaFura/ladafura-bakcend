package com.pharmacopee.ladafura.dto.pharmacopee.paiement;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Synthèse financière et récapitulatif des encaissements par canal")
public class PharmacopeePaiementSummaryResponse {

    @Schema(description = "Nombre total de transactions enregistrées", example = "28")
    private long totalTransactions;

    @Schema(description = "Nombre de règlements réussis", example = "25")
    private long totalReussis;

    @Schema(description = "Nombre de règlements en attente", example = "2")
    private long totalEnAttente;

    @Schema(description = "Nombre de règlements échoués", example = "1")
    private long totalEchoues;

    @Schema(description = "Total général encaissé (en FCFA)", example = "350000.0")
    private double totalEncaisse;

    @Schema(description = "Total encaissé par Mobile Money (Orange Money / Wave) en FCFA", example = "220000.0")
    private double encaisseMobileMoney;

    @Schema(description = "Total encaissé en espèces (Cash à la livraison ou au comptoir) en FCFA", example = "95000.0")
    private double encaisseCash;

    @Schema(description = "Total encaissé par carte bancaire en FCFA", example = "35000.0")
    private double encaisseCarteBancaire;
}
