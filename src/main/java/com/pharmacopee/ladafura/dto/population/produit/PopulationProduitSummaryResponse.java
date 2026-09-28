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
@Schema(description = "Aperçu synthétique d'un produit dans le catalogue Population")
public class PopulationProduitSummaryResponse {

    @Schema(description = "Identifiant du produit", example = "5")
    private Long id;

    @Schema(description = "Nom commercial du produit", example = "Tisane Kinkéliba Bio")
    private String nom;

    @Schema(description = "Description synthétique du produit", example = "Infusion détoxifiante et stimulante...")
    private String description;

    @Schema(description = "Forme galénique ou présentation", example = "Sachet de tisane")
    private String forme;

    @Schema(description = "Prix indicatif de base en FCFA", example = "2500.0")
    private Double prixIndicatif;

    @Schema(description = "URL de l'image du produit", example = "https://ladafura.ml/uploads/produits/tisane.jpg")
    private String photoUrl;

    @Schema(description = "Identifiant de la catégorie de produit", example = "1")
    private Long categorieId;

    @Schema(description = "Nom de la catégorie de produit", example = "Tisanes et Infusions")
    private String categorieNom;

    @Schema(description = "Noms scientifiques des plantes principales composant le produit", example = "[\"Combretum micranthum\"]")
    private List<String> plantesPrincipales;

    @Schema(description = "Nombre de pharmacopées proposant ce produit", example = "3")
    private int nombrePharmacopees;

    @Schema(description = "Indique si le produit est actuellement en stock dans au moins une officine", example = "true")
    private boolean disponibleEnPharmacie;

    @Schema(description = "Note moyenne des avis clients (sur 5)", example = "4.5")
    private Double noteMoyenne;

    @Schema(description = "Nombre total d'avis clients validés", example = "12")
    private long nombreAvis;
}
