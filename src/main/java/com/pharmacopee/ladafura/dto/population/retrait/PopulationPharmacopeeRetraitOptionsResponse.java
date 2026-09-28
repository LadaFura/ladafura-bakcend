package com.pharmacopee.ladafura.dto.population.retrait;

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
@Schema(description = "Récapitulatif des modes de livraison et retrait proposés par une pharmacopée")
public class PopulationPharmacopeeRetraitOptionsResponse {

    @Schema(description = "Identifiant de la pharmacopée", example = "1")
    private Long pharmacopeeId;

    @Schema(description = "Nom de l'officine", example = "Pharmacie Mandé")
    private String nomPharmacopee;

    @Schema(description = "Téléphone de l'officine", example = "+223 70 11 22 33")
    private String telephonePharmacopee;

    @Schema(description = "Adresse géographique complète de l'officine", example = "Siby Centre, Route Nationale 5, Koulikoro")
    private String adressePharmacopee;

    @Schema(description = "Indique si la livraison à domicile est disponible", example = "true")
    private Boolean proposeLivraison;

    @Schema(description = "Frais standards de livraison en FCFA", example = "1500.0")
    private Double fraisLivraison;

    @Schema(description = "Indique si le retrait en officine (Pickup) est disponible", example = "true")
    private Boolean proposePickup;

    @Schema(description = "Liste détaillée des options configurées par l'officine")
    private List<PopulationModeRetraitOptionDto> options;
}
