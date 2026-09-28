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
@Schema(description = "Élément résultat de recherche pour une officine de pharmacopée")
public class PopulationPharmacopeeSearchItem {

    @Schema(description = "Identifiant de l'officine", example = "2")
    private Long id;

    @Schema(description = "Nom de la pharmacopée", example = "Pharmacie Traditionnelle Mandé Santé")
    private String nom;

    @Schema(description = "Présentation synthétique", example = "Officine spécialisée dans les plantes médicinales du Mandé.")
    private String description;

    @Schema(description = "Numéro de téléphone", example = "+223 76 11 22 33")
    private String telephone;

    @Schema(description = "Région administrative", example = "Koulikoro")
    private String region;

    @Schema(description = "Cercle administratif", example = "Kati")
    private String cercle;

    @Schema(description = "Commune", example = "Siby")
    private String commune;

    @Schema(description = "Localité ou quartier", example = "Village de Siby")
    private String localite;

    @Schema(description = "Coordonnée Latitude", example = "12.3833")
    private Double latitude;

    @Schema(description = "Coordonnée Longitude", example = "-8.3333")
    private Double longitude;
}
