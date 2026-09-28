package com.pharmacopee.ladafura.dto.population.recherche;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Élément résultat de recherche pour un produit traditionnel disponible")
public class PopulationProduitSearchItem {

    @Schema(description = "Identifiant du produit", example = "8")
    private Long id;

    @Schema(description = "Nom commercial du produit", example = "Tisane Kinkéliba Bio")
    private String nom;

    @Schema(description = "Description du remède", example = "Sachets de feuilles séchées prêtes à l'infusion.")
    private String description;

    @Schema(description = "Forme galénique", example = "Poudre / Tisane")
    private String forme;

    @Schema(description = "Prix indicatif en FCFA", example = "1500.0")
    private Double prix;

    @Schema(description = "URL de l'image du produit", example = "https://storage.ladafura.ml/produits/tisane_kink.jpg")
    private String photoUrl;

    @Schema(description = "Nom de la catégorie", example = "Tisanes & Infusions")
    private String categorie;
}
