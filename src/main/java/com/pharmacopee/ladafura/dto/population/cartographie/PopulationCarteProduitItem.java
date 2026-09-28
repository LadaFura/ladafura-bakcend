package com.pharmacopee.ladafura.dto.population.cartographie;

import java.util.List;

import com.pharmacopee.ladafura.dto.population.pharmacopee.PopulationPharmacopeeModeRetraitDto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Résultat de localisation cartographique pour un produit disponible dans une officine")
public class PopulationCarteProduitItem {

    @Schema(description = "Identifiant de la pharmacopée", example = "3")
    private Long pharmacopeeId;

    @Schema(description = "Nom de la pharmacopée", example = "Pharmacie Mandé")
    private String nomPharmacopee;

    @Schema(description = "Téléphone de contact", example = "+223 70 12 34 56")
    private String telephone;

    @Schema(description = "Région administrative", example = "Koulikoro")
    private String region;

    @Schema(description = "Cercle administratif", example = "Kati")
    private String cercle;

    @Schema(description = "Commune", example = "Siby")
    private String commune;

    @Schema(description = "Adresse / Localité", example = "Près du Grand Marché")
    private String localite;

    @Schema(description = "Latitude GPS de l'officine", example = "12.3847")
    private Double latitude;

    @Schema(description = "Longitude GPS de l'officine", example = "-8.3341")
    private Double longitude;

    @Schema(description = "Distance en kilomètres par rapport à la position de l'utilisateur", example = "2.8")
    private Double distanceKm;

    @Schema(description = "Identifiant du produit", example = "5")
    private Long produitId;

    @Schema(description = "Nom commercial du produit", example = "Tisane Kinkéliba")
    private String nomProduit;

    @Schema(description = "Forme galénique", example = "Sachet de 100g")
    private String forme;

    @Schema(description = "Prix appliqué par cette officine en FCFA", example = "2500.0")
    private Double prix;

    @Schema(description = "Disponibilité immédiate du produit", example = "true")
    private Boolean disponible;

    @Schema(description = "Quantité en stock", example = "12")
    private Integer quantiteStock;

    @Schema(description = "Modes de retrait disponibles (Livraison et/ou Pickup)")
    private List<PopulationPharmacopeeModeRetraitDto> modesRetrait;
}
