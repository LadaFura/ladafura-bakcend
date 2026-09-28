package com.pharmacopee.ladafura.dto.pharmacopee.commande;

import java.time.LocalDateTime;

import com.pharmacopee.ladafura.enums.MethodePaiement;
import com.pharmacopee.ladafura.enums.StatutPaiement;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Informations de règlement de la commande")
public class PaiementInfoResponse {

    @Schema(description = "Identifiant du paiement", example = "1")
    private Long id;

    @Schema(description = "Montant total réglé ou à régler en FCFA", example = "7000.0")
    private Double montant;

    @Schema(description = "Méthode de paiement", example = "ORANGE_MONEY")
    private MethodePaiement methode;

    @Schema(description = "Statut du règlement", example = "VALIDE")
    private StatutPaiement statut;

    @Schema(description = "Référence de transaction", example = "OM-BKO-2026-998877")
    private String reference;

    @Schema(description = "Date et heure du règlement")
    private LocalDateTime datePaiement;
}
