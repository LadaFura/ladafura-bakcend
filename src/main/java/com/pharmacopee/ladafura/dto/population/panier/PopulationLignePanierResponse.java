package com.pharmacopee.ladafura.dto.population.panier;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Ligne individuelle du panier")
public class PopulationLignePanierResponse {

    @Schema(description = "Identifiant de la ligne du panier", example = "10")
    private Long ligneId;

    @Schema(description = "Identifiant du produit", example = "5")
    private Long produitId;

    @Schema(description = "Nom commercial du produit", example = "Tisane Kinkéliba Bio")
    private String nomProduit;

    @Schema(description = "Forme galénique", example = "Sachet de 100g")
    private String forme;

    @Schema(description = "Photo du produit", example = "https://ladafura.ml/uploads/produits/tisane.jpg")
    private String photoUrl;

    @Schema(description = "Prix unitaire effectif en FCFA", example = "2500.0")
    private Double prixUnitaire;

    @Schema(description = "Quantité sélectionnée", example = "2")
    private Integer quantite;

    @Schema(description = "Sous-total pour cette ligne en FCFA", example = "5000.0")
    private Double sousTotal;

    @Schema(description = "Indique si le produit est toujours disponible dans le catalogue validé", example = "true")
    private boolean disponible;
}
