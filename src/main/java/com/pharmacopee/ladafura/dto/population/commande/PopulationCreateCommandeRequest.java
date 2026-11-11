package com.pharmacopee.ladafura.dto.population.commande;

import com.pharmacopee.ladafura.enums.MethodePaiement;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Demande de validation, règlement et enregistrement de commande par l'utilisateur")
public class PopulationCreateCommandeRequest {

    @NotNull(message = "L'identifiant de la pharmacopée est obligatoire")
    @Schema(description = "Identifiant de la pharmacopée choisie", example = "1")
    private Long pharmacopeeId;

    @NotNull(message = "Le mode de retrait est obligatoire")
    @Schema(description = "Identifiant du mode de retrait retenu", example = "2")
    private Long modeRetraitId;

    @Schema(description = "Adresse complète de livraison (obligatoire si mode LIVRAISON)", example = "Badalabougou, Rue 24, Porte 12, Bamako")
    private String adresseLivraison;

    @Schema(description = "Indications complémentaires pour la livraison ou le retrait", example = "Sonner au portail bleu ou appeler avant d'arriver")
    private String notes;

    @NotNull(message = "Le moyen de paiement est obligatoire pour valider la commande")
    @Schema(description = "Moyen de paiement choisi (MOBILE_MONEY, CASH, CARTE_BANCAIRE)", example = "MOBILE_MONEY")
    private MethodePaiement methode;

    @Schema(description = "Opérateur Mobile Money (ORANGE_MONEY, MOOV_MONEY, WAVE)", example = "ORANGE_MONEY")
    private String operateur;

    @Schema(description = "Numéro Mobile Money du payeur", example = "+22370112233")
    @Size(max = 30, message = "Le numéro de téléphone est trop long")
    private String telephoneMobileMoney;

    @Schema(description = "Compte Mobile Money de la pharmacopée sur lequel le client a payé", example = "12")
    private Long compteMobileMoneyId;

    @Size(max = 100, message = "Le numéro de transaction est trop long")
    @Schema(description = "Numéro de transaction reçu par SMS, obligatoire pour le Mobile Money", example = "TX-998822")
    private String referenceTransaction;
}
