package com.pharmacopee.ladafura.dto.population.pharmacopee;

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
@Schema(description = "Fiche détaillée d'une pharmacopée agréée pour la Population")
public class PopulationPharmacopeeDetailResponse {

    @Schema(description = "Identifiant de la pharmacopée", example = "3")
    private Long id;

    @Schema(description = "Nom officiel de l'officine", example = "Pharmacie Traditionnelle Mandé")
    private String nom;

    @Schema(description = "Description ou historique de l'officine", example = "Officine agréée en médecine traditionnelle certifiée...")
    private String description;

    @Schema(description = "Numéro de téléphone de contact", example = "+223 70 12 34 56")
    private String telephone;

    @Schema(description = "Adresse email de contact", example = "contact@pharmaciemande.ml")
    private String email;

    @Schema(description = "Localisation géographique détaillée")
    private PopulationPharmacopeeLocalisationDto localisation;

    @Schema(description = "Modes de retrait disponibles (Livraison et/ou Pickup avec frais)")
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
