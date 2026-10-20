package com.pharmacopee.ladafura.dto.population.pharmacopee;

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
@Schema(description = "Produit proposé par une officine de pharmacopée avec son prix et son stock")
public class PopulationPharmacopeeProduitItemResponse {

    @Schema(description = "Identifiant de la disponibilité", example = "10")
    private Long disponibiliteId;

    @Schema(description = "Identifiant du produit", example = "5")
    private Long produitId;

    @Schema(description = "Nom commercial du produit", example = "Tisane Kinkéliba Bio")
    private String nom;

    @Schema(description = "Description synthétique du produit", example = "Tisane détoxifiante et hépato-stimulante")
    private String description;

    @Schema(description = "Forme galénique", example = "Sachet de 100g")
    private String forme;

    @Schema(description = "Posologie et conseils d'utilisation", example = "1 sachet matin et soir en infusion tiède")
    private String posologie;

    @Schema(description = "Prix appliqué par cette pharmacopée en FCFA", example = "2500.0")
    private Double prix;

    @Schema(description = "URL de la photo du produit", example = "https://ladafura.ml/uploads/produits/tisane.jpg")
    private String photoUrl;

    @Schema(description = "Identifiant de la catégorie", example = "1")
    private Long categorieId;

    @Schema(description = "Nom de la catégorie de produit", example = "Tisanes et Infusions")
    private String categorieNom;

    @Schema(description = "Disponibilité immédiate en stock", example = "true")
    private Boolean disponible;

    @Schema(description = "Quantité en stock disponible", example = "15")
    private Integer quantiteStock;

    @Schema(description = "Maladies et symptômes ciblés par ce produit")
    private List<String> maladies;

    @Schema(description = "Plantes médicinales entrant dans la composition du produit")
    private List<String> plantes;
}
