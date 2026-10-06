package com.pharmacopee.ladafura.dto.population.cartographie;

import java.util.List;

import com.pharmacopee.ladafura.dto.population.pharmacopee.PopulationPharmacopeeLocalisationDto;
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
@Schema(description = "Fiche détaillée d'un point d'intérêt cartographique pour une officine de pharmacopée")
public class PopulationCarteDetailPharmacopeeResponse {

    @Schema(description = "Identifiant de la pharmacopée", example = "3")
    private Long pharmacopeeId;

    @Schema(description = "Nom de la pharmacopée", example = "Pharmacie Mandé")
    private String nom;

    @Schema(description = "Présentation générale", example = "Officine spécialisée en remèdes traditionnels maliens")
    private String description;

    @Schema(description = "Numéro de téléphone de contact", example = "+223 70 12 34 56")
    private String telephone;

    @Schema(description = "Adresse email de contact", example = "contact@mande.ml")
    private String email;

    @Schema(description = "Localisation géographique et coordonnées GPS")
    private PopulationPharmacopeeLocalisationDto localisation;

    @Schema(description = "Distance estimée en kilomètres par rapport à la position de l'utilisateur", example = "5.6")
    private Double distanceKm;

    @Schema(description = "Modes de retrait proposés")
    private List<PopulationPharmacopeeModeRetraitDto> modesRetrait;

    @Schema(description = "Nombre de produits validés proposés", example = "18")
    private long nombreProduits;

    @Schema(description = "Note moyenne des avis clients (sur 5)", example = "4.7")
    private Double noteMoyenne;

    @Schema(description = "Nombre total d'avis clients validés", example = "24")
    private long nombreAvis;

    @Schema(description = "URL de la photo ou bannière de l'officine", example = "/uploads/pharmacopees/officine.jpg")
    private String photoUrl;
}
