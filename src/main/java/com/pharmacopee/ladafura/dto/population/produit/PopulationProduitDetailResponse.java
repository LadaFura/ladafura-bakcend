package com.pharmacopee.ladafura.dto.population.produit;

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
@Schema(description = "Fiche détaillée d'un produit traditionnel avec plantes, maladies associées, pharmacopées et prix")
public class PopulationProduitDetailResponse {

    @Schema(description = "Identifiant du produit", example = "5")
    private Long id;

    @Schema(description = "Nom commercial du produit", example = "Tisane Kinkéliba Bio")
    private String nom;

    @Schema(description = "Description détaillée du produit", example = "Infusion 100% naturelle formulée à partir de feuilles de kinkéliba...")
    private String description;

    @Schema(description = "Forme galénique ou conditionnement", example = "Sachet de 100g")
    private String forme;

    @Schema(description = "Composition textuelle déclarée", example = "Feuilles séchées de Combretum micranthum")
    private String compositionTexte;

    @Schema(description = "Prix indicatif de base en FCFA", example = "2500.0")
    private Double prixIndicatif;

    @Schema(description = "URL de l'image du produit", example = "https://ladafura.ml/uploads/produits/tisane.jpg")
    private String photoUrl;

    @Schema(description = "Identifiant de la catégorie", example = "1")
    private Long categorieId;

    @Schema(description = "Nom de la catégorie", example = "Tisanes et Infusions")
    private String categorieNom;

    @Schema(description = "Note moyenne des avis clients (sur 5)", example = "4.5")
    private Double noteMoyenne;

    @Schema(description = "Nombre total d'avis clients validés", example = "12")
    private long nombreAvis;

    @Schema(description = "Composition détaillée des plantes avec quantités et unités")
    private List<PopulationCompositionItemDto> compositions;

    @Schema(description = "Maladies et indications associées déduites des plantes de composition")
    private List<PopulationProduitMaladieDto> maladies;

    @Schema(description = "Officines de pharmacopée proposant ce produit avec prix, disponibilité et modes de retrait")
    private List<PopulationOffrePharmacopeeDto> offresPharmacopees;
}
