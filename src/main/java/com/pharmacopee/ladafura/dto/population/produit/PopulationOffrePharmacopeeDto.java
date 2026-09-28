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
@Schema(description = "Offre commerciale d'une officine de pharmacopée pour un produit (prix, disponibilité, modes de retrait)")
public class PopulationOffrePharmacopeeDto {

    @Schema(description = "Identifiant de la disponibilité produit", example = "12")
    private Long disponibiliteId;

    @Schema(description = "Identifiant de la pharmacopée", example = "3")
    private Long pharmacopeeId;

    @Schema(description = "Nom de la pharmacopée / officine", example = "Pharmacie Mandé")
    private String nomPharmacopee;

    @Schema(description = "Numéro de téléphone de contact", example = "+223 70 11 22 33")
    private String telephone;

    @Schema(description = "Région administrative", example = "Koulikoro")
    private String region;

    @Schema(description = "Cercle administratif", example = "Kati")
    private String cercle;

    @Schema(description = "Commune", example = "Siby")
    private String commune;

    @Schema(description = "Localité ou adresse détaillée", example = "Centre-ville, près du marché")
    private String localite;

    @Schema(description = "Latitude géographique", example = "12.384")
    private Double latitude;

    @Schema(description = "Longitude géographique", example = "-8.332")
    private Double longitude;

    @Schema(description = "Disponibilité immédiate du produit", example = "true")
    private Boolean disponible;

    @Schema(description = "Quantité en stock disponible", example = "15")
    private Integer quantiteStock;

    @Schema(description = "Prix appliqué en FCFA par cette pharmacopée", example = "3500.0")
    private Double prix;

    @Schema(description = "Modes de retrait proposés par cette pharmacopée (Livraison / Pickup)")
    private List<PopulationModeRetraitDto> modesRetrait;
}
