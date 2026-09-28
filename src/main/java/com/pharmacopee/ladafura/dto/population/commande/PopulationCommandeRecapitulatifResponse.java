package com.pharmacopee.ladafura.dto.population.commande;

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
@Schema(description = "Récapitulatif et devis chiffré avant confirmation de la commande")
public class PopulationCommandeRecapitulatifResponse {

    @Schema(description = "Identifiant de la pharmacopée", example = "1")
    private Long pharmacopeeId;

    @Schema(description = "Nom de la pharmacopée sélectionnée", example = "Pharmacie Mandé")
    private String nomPharmacopee;

    @Schema(description = "Téléphone de contact de la pharmacopée", example = "+223 70 11 22 33")
    private String telephonePharmacopee;

    @Schema(description = "Type de mode de retrait sélectionné (LIVRAISON ou PICKUP)", example = "LIVRAISON")
    private String modeRetrait;

    @Schema(description = "Frais de livraison calculés (0 FCFA pour Pickup)", example = "1500.0")
    private Double fraisLivraison;

    @Schema(description = "Montant total des produits commandés (FCFA)", example = "5000.0")
    private Double totalProduits;

    @Schema(description = "Montant global TTC à payer (produits + livraison) (FCFA)", example = "6500.0")
    private Double montantTotal;

    @Schema(description = "Nombre total d'articles dans la commande", example = "2")
    private Integer nombreArticles;

    @Schema(description = "Liste des articles du panier inclus dans ce récapitulatif")
    private List<PopulationLigneCommandeDto> lignes;
}
