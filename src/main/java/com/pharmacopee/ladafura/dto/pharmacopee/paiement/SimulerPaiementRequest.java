package com.pharmacopee.ladafura.dto.pharmacopee.paiement;

import com.pharmacopee.ladafura.enums.MethodePaiement;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Formulaire de simulation de règlement (Mobile Money, Espèces ou Carte Bancaire)")
public class SimulerPaiementRequest {

    @NotNull(message = "L'identifiant de la commande est obligatoire")
    @Schema(description = "Identifiant de la commande à régler", example = "10")
    private Long commandeId;

    @NotNull(message = "Le mode de règlement est obligatoire (MOBILE_MONEY, CASH, CARTE_BANCAIRE)")
    @Schema(description = "Méthode de paiement", example = "MOBILE_MONEY")
    private MethodePaiement methode;

    @Builder.Default
    @Schema(description = "Résultat simulé : true pour simuler un paiement réussi, false pour simuler un échec", example = "true")
    private Boolean simulerSucces = true;

    @Schema(description = "Numéro de compte / téléphone de l'acheteur (ex: compte Orange Money / Wave)", example = "+223 70 12 34 56")
    private String numeroCompteOuTelephone;

    @Schema(description = "Référence externe optionnelle (générée automatiquement avec préfixe SIM- si vide)", example = "OM-BKO-2026-987654")
    private String referencePaiement;
}
