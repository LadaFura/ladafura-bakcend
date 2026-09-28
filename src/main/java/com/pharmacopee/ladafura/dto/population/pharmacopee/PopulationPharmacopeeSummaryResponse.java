package com.pharmacopee.ladafura.dto.population.pharmacopee;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Aperçu synthétique d'une officine de pharmacopée agréée pour la Population")
public class PopulationPharmacopeeSummaryResponse {

    @Schema(description = "Identifiant de la pharmacopée", example = "3")
    private Long id;

    @Schema(description = "Nom de la pharmacopée", example = "Pharmacie Traditionnelle Mandé")
    private String nom;

    @Schema(description = "Courte présentation ou spécialités", example = "Spécialisée en remèdes traditionnels validés du Mandé")
    private String description;

    @Schema(description = "Numéro de téléphone de contact", example = "+223 70 12 34 56")
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

    @Schema(description = "Propose la livraison à domicile", example = "true")
    private boolean proposeLivraison;

    @Schema(description = "Propose le retrait gratuit en officine (Pickup)", example = "true")
    private boolean proposePickup;

    @Schema(description = "Nombre de produits validés disponibles dans cette officine", example = "18")
    private long nombreProduits;

    @Schema(description = "Note moyenne des avis clients (sur 5)", example = "4.7")
    private Double noteMoyenne;

    @Schema(description = "Nombre total d'avis validés", example = "24")
    private long nombreAvis;
}
