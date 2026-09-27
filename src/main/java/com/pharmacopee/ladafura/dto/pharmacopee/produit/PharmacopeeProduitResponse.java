package com.pharmacopee.ladafura.dto.pharmacopee.produit;

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
@Schema(description = "Détails d'un produit associé à la pharmacopée et son état de disponibilité")
public class PharmacopeeProduitResponse {

    @Schema(description = "Identifiant unique de la disponibilité (liaison pharmacopée-produit)", example = "10")
    private Long disponibiliteId;

    @Schema(description = "Identifiant du produit", example = "5")
    private Long produitId;

    @Schema(description = "Nom du produit", example = "Tisane Hépatite Kinkéliba")
    private String nom;

    @Schema(description = "Description du produit", example = "Préparation traditionnelle à base de feuilles de kinkéliba séchées.")
    private String description;

    @Schema(description = "Forme galénique (tisane, poudre, macérat, etc.)", example = "Tisane en sachet")
    private String forme;

    @Schema(description = "Prix unitaire de référence (FCFA)", example = "2500.0")
    private Double prix;

    @Schema(description = "URL de la photo du produit", example = "https://storage.ladafura.ml/produits/kinkeliba.jpg")
    private String photoUrl;

    @Schema(description = "Nom de la catégorie du produit", example = "Tisanes & Décoctions")
    private String categorieNom;

    @Schema(description = "Identifiant de la catégorie du produit", example = "2")
    private Long categorieId;

    @Schema(description = "Quantité physique disponible en stock dans l'officine", example = "45")
    private Integer quantiteStock;

    @Schema(description = "Indicateur d'activation de la disponibilité à la vente", example = "true")
    private Boolean disponible;

    @Schema(description = "Date et heure de la dernière mise à jour du stock/disponibilité")
    private LocalDateTime dateMiseAJour;
}
