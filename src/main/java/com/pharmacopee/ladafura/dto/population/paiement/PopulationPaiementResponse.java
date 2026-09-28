package com.pharmacopee.ladafura.dto.population.paiement;

import java.time.LocalDateTime;

import com.pharmacopee.ladafura.enums.MethodePaiement;
import com.pharmacopee.ladafura.enums.StatutCommande;
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
@Schema(description = "Confirmation et reçu de paiement d'une commande")
public class PopulationPaiementResponse {

    @Schema(description = "Identifiant unique de la transaction de paiement", example = "55")
    private Long id;

    @Schema(description = "Numéro de référence de transaction", example = "PAY-OM-20260928-88392")
    private String reference;

    @Schema(description = "Identifiant de la commande réglée", example = "42")
    private Long commandeId;

    @Schema(description = "Numéro unique de la commande", example = "CMD-20260928-ABC12")
    private String numeroCommande;

    @Schema(description = "Montant facturé et réglé en FCFA", example = "6500.0")
    private Double montant;

    @Schema(description = "Moyen de règlement utilisé", example = "MOBILE_MONEY")
    private MethodePaiement methode;

    @Schema(description = "Libellé convivial du mode de paiement", example = "Mobile Money (Orange Money)")
    private String libelleMethode;

    @Schema(description = "Statut actuel de la transaction", example = "REUSSI")
    private StatutPaiement statut;

    @Schema(description = "Date et heure d'enregistrement du paiement")
    private LocalDateTime datePaiement;

    @Schema(description = "Indique si la transaction s'est conclue avec succès", example = "true")
    private Boolean succes;

    @Schema(description = "Message de confirmation ou de guidage client", example = "Votre paiement a été validé avec succès. Votre commande est désormais confirmée.")
    private String message;

    @Schema(description = "Statut actualisé de la commande", example = "CONFIRMEE")
    private StatutCommande statutCommande;
}
