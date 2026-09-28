package com.pharmacopee.ladafura.dto.population.cartographie;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Point d'intérêt cartographique représentant une officine de pharmacopée agréée")
public class PopulationCartePharmacopeeItem {

    @Schema(description = "Identifiant de la pharmacopée", example = "3")
    private Long pharmacopeeId;

    @Schema(description = "Nom de la pharmacopée", example = "Pharmacie Mandé")
    private String nom;

    @Schema(description = "Description ou spécialité", example = "Remèdes traditionnels du Mandé")
    private String description;

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

    @Schema(description = "Latitude GPS", example = "12.3847")
    private Double latitude;

    @Schema(description = "Longitude GPS", example = "-8.3341")
    private Double longitude;

    @Schema(description = "Distance en kilomètres par rapport à la position demandée", example = "4.2")
    private Double distanceKm;

    @Schema(description = "Propose le service de livraison", example = "true")
    private boolean proposeLivraison;

    @Schema(description = "Propose le retrait en officine (Pickup)", example = "true")
    private boolean proposePickup;

    @Schema(description = "Nombre de produits validés disponibles dans l'officine", example = "15")
    private long nombreProduits;

    @Schema(description = "Note moyenne des avis clients (sur 5)", example = "4.7")
    private Double noteMoyenne;

    @Schema(description = "Nombre total d'avis validés", example = "24")
    private long nombreAvis;
}
