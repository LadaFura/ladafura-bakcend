package com.pharmacopee.ladafura.dto.population.paiement;

import java.util.List;

import com.pharmacopee.ladafura.enums.MethodePaiement;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Information et consignes sur une méthode de règlement acceptée sur la plateforme")
public class PopulationMethodePaiementInfoDto {

    @Schema(description = "Code technique de la méthode (MOBILE_MONEY, CASH, CARTE_BANCAIRE)", example = "MOBILE_MONEY")
    private MethodePaiement code;

    @Schema(description = "Libellé convivial", example = "Mobile Money (Orange Money / Moov Money / Wave)")
    private String libelle;

    @Schema(description = "Description du moyen de règlement", example = "Paiement direct et sécurisé depuis votre téléphone mobile.")
    private String description;

    @Schema(description = "Indique si ce moyen de paiement est actif sur la plateforme", example = "true")
    private Boolean disponible;

    @Schema(description = "Opérateurs ou réseaux supportés au Mali", example = "[\"Orange Money\", \"Moov Money\", \"Wave\"]")
    private List<String> operateurs;

    @Schema(description = "Consigne d'utilisation pour le client", example = "Saisissez votre numéro de téléphone et validez la notification USSD / SMS reçue sur votre téléphone.")
    private String instructions;
}
