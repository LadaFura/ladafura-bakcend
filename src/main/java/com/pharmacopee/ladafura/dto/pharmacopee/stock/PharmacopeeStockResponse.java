package com.pharmacopee.ladafura.dto.pharmacopee.stock;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Fiche détaillée du stock et de la tarification d'un produit dans l'officine")
public class PharmacopeeStockResponse {

    @Schema(description = "Identifiant de la liaison stock/disponibilité", example = "10")
    private Long disponibiliteId;

    @Schema(description = "Identifiant du produit", example = "5")
    private Long produitId;

    @Schema(description = "Nom du produit", example = "Tisane Hépatite Kinkéliba")
    private String nomProduit;

    @Schema(description = "Forme galénique", example = "Tisane en sachet")
    private String forme;

    @Schema(description = "Prix national conseillé dans le catalogue (FCFA)", example = "2500.0")
    private Double prixNational;

    @Schema(description = "Prix de vente effectif pratiqué par cette pharmacopée (FCFA)", example = "2500.0")
    private Double prixVente;

    @Schema(description = "Indique si l'officine a défini un prix de vente personnalisé différent du prix national", example = "false")
    private boolean prixPersonnalise;

    @Schema(description = "Quantité en stock physique disponible", example = "35")
    private Integer quantiteStock;

    @Schema(description = "Indicateur d'activation du produit à la vente", example = "true")
    private Boolean disponible;

    @Schema(description = "Alerte de rupture de stock (true si quantiteStock <= 0)", example = "false")
    private boolean enRupture;

    @Schema(description = "Horodatage de la dernière mise à jour de stock ou de disponibilité")
    private LocalDateTime dateMiseAJour;
}
