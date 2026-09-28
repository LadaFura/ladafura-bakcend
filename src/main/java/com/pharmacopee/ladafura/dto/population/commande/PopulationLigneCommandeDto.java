package com.pharmacopee.ladafura.dto.population.commande;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Détail d'un article dans une commande ou un récapitulatif")
public class PopulationLigneCommandeDto {

    @Schema(description = "Identifiant de la ligne de commande", example = "101")
    private Long id;

    @Schema(description = "Identifiant du produit", example = "5")
    private Long produitId;

    @Schema(description = "Désignation commerciale du produit", example = "Sirop d'Artemisia Annua")
    private String nomProduit;

    @Schema(description = "Forme galénique ou conditionnement", example = "Flacon de 250ml")
    private String forme;

    @Schema(description = "URL de l'image de présentation du produit", example = "https://storage.ladafura.ml/produits/artemisia.jpg")
    private String photoUrl;

    @Schema(description = "Prix unitaire facturé (FCFA)", example = "2500.0")
    private Double prixUnitaire;

    @Schema(description = "Quantité commandée", example = "2")
    private Integer quantite;

    @Schema(description = "Sous-total pour cette ligne (FCFA)", example = "5000.0")
    private Double sousTotal;
}
