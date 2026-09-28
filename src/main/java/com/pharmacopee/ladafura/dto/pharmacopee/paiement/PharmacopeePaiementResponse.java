package com.pharmacopee.ladafura.dto.pharmacopee.paiement;

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
@Schema(description = "Fiche détaillée d'un règlement financier rattaché à une commande")
public class PharmacopeePaiementResponse {

    @Schema(description = "Identifiant unique du paiement", example = "1")
    private Long id;

    @Schema(description = "Montant du règlement en FCFA", example = "7000.0")
    private Double montant;

    @Schema(description = "Horodatage de l'opération")
    private LocalDateTime datePaiement;

    @Schema(description = "Moyen de paiement employé", example = "MOBILE_MONEY")
    private MethodePaiement methode;

    @Schema(description = "Libellé lisible du moyen de paiement", example = "Mobile Money (Orange Money / Wave)")
    private String libelleMethode;

    @Schema(description = "Statut de la transaction", example = "REUSSI")
    private StatutPaiement statut;

    @Schema(description = "Référence de transaction marchande ou de reçu", example = "SIM-OM-1727471234-897")
    private String reference;

    @Schema(description = "Identifiant de la commande associée", example = "10")
    private Long commandeId;

    @Schema(description = "Numéro officiel de la commande associée", example = "CMD-2026-00042")
    private String commandeNumero;

    @Schema(description = "Nom complet de l'acheteur", example = "Amadou Traoré")
    private String clientNomComplet;

    @Schema(description = "Téléphone de l'acheteur", example = "+223 76 12 34 56")
    private String clientTelephone;
}
