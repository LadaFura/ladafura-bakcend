package com.pharmacopee.ladafura.dto.pharmacopee.commande;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Article commandé (ligne de commande)")
public class LigneCommandeResponse {

    @Schema(description = "Identifiant de la ligne", example = "1")
    private Long id;

    @Schema(description = "Identifiant du produit", example = "5")
    private Long produitId;

    @Schema(description = "Nom du produit", example = "Tisane Hépatite Kinkéliba")
    private String nomProduit;

    @Schema(description = "Forme galénique", example = "Tisane en sachet")
    private String forme;

    @Schema(description = "Quantité commandée", example = "2")
    private Integer quantite;

    @Schema(description = "Prix unitaire facturé en FCFA", example = "2500.0")
    private Double prixUnitaire;

    @Schema(description = "Sous-total pour cette ligne en FCFA", example = "5000.0")
    private Double sousTotal;
}
