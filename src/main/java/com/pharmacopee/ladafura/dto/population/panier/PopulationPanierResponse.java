package com.pharmacopee.ladafura.dto.population.panier;

import java.time.LocalDateTime;
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
@Schema(description = "Récapitulatif du panier actif de l'utilisateur Population")
public class PopulationPanierResponse {

    @Schema(description = "Identifiant unique du panier", example = "1")
    private Long panierId;

    @Schema(description = "Nombre cumulé total d'articles dans le panier", example = "4")
    private int nombreArticles;

    @Schema(description = "Montant total cumulé du panier en FCFA", example = "10500.0")
    private Double montantTotal;

    @Schema(description = "Date et heure de dernière mise à jour du panier")
    private LocalDateTime dateModification;

    @Schema(description = "Liste des lignes d'articles composant le panier")
    private List<PopulationLignePanierResponse> lignes;
}
