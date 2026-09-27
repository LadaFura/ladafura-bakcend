package com.pharmacopee.ladafura.dto.pharmacopee.produit;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Élément du catalogue général des produits validés dans LADAFURA")
public class CatalogueProduitItemResponse {

    @Schema(description = "Identifiant unique du produit", example = "5")
    private Long id;

    @Schema(description = "Nom du produit", example = "Tisane Hépatite Kinkéliba")
    private String nom;

    @Schema(description = "Description du produit", example = "Préparation traditionnelle certifiée.")
    private String description;

    @Schema(description = "Forme galénique", example = "Tisane")
    private String forme;

    @Schema(description = "Prix de référence (FCFA)", example = "2500.0")
    private Double prix;

    @Schema(description = "URL de l'image", example = "https://storage.ladafura.ml/produits/kinkeliba.jpg")
    private String photoUrl;

    @Schema(description = "Nom de la catégorie", example = "Tisanes")
    private String categorieNom;

    @Schema(description = "Indique si ce produit est déjà associé à l'officine connectée", example = "false")
    private boolean dejaAssocie;
}
