package com.pharmacopee.ladafura.dto.population.historique;

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
@Schema(description = "Répartition des dépenses par méthode de paiement")
public class PopulationDepenseMethodeDto {

    @Schema(description = "Méthode de paiement", example = "ORANGE_MONEY")
    private MethodePaiement methode;

    @Schema(description = "Nom lisible de la méthode", example = "Orange Money Mali")
    private String nomMethode;

    @Schema(description = "Montant cumulé réglé via cette méthode en FCFA", example = "18000.0")
    private Double montant;

    @Schema(description = "Nombre de paiements effectués", example = "3")
    private long nombrePaiements;

    @Schema(description = "Part en pourcentage des dépenses globales", example = "72.0")
    private Double pourcentage;
}
