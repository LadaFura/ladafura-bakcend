package com.pharmacopee.ladafura.dto.population.historique;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Synthèse financière et analyse des dépenses de l'utilisateur Population")
public class PopulationDepensesSyntheseResponse {

    @Schema(description = "Montant cumulé total dépensé en FCFA (paiements validés)", example = "35000.0")
    private Double montantTotalDepense;

    @Schema(description = "Nombre total de commandes payées", example = "5")
    private long nombreTotalTransactions;

    @Schema(description = "Panier moyen par commande en FCFA", example = "7000.0")
    private Double panierMoyen;

    @Schema(description = "Historique d'évolution des dépenses par mois")
    private List<PopulationDepenseMensuelleDto> depensesParMois;

    @Schema(description = "Répartition des dépenses par mode de règlement")
    private List<PopulationDepenseMethodeDto> depensesParMethode;
}
