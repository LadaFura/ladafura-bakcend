package com.pharmacopee.ladafura.dto.population.historique;

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
@Schema(description = "Relevé et synthèse d'activité complète du compte client")
public class PopulationReleveActiviteResponse {

    @Schema(description = "Identifiant de l'utilisateur", example = "10")
    private Long utilisateurId;

    @Schema(description = "Nom complet du client", example = "Awa Coulibaly")
    private String nomComplet;

    @Schema(description = "Adresse email", example = "client@ladafura.ml")
    private String email;

    @Schema(description = "Date et heure de génération du relevé", example = "2026-09-28T16:00:00")
    private LocalDateTime dateGeneration;

    @Schema(description = "Nombre total de commandes passées", example = "6")
    private long totalCommandes;

    @Schema(description = "Nombre total de commandes réceptionnées (LIVREE / RETIREE)", example = "5")
    private long totalCommandesLivrees;

    @Schema(description = "Montant total dépensé cumulé en FCFA", example = "35000.0")
    private Double totalDepense;

    @Schema(description = "Nombre d'avis clients déposés", example = "3")
    private long totalAvis;

    @Schema(description = "Nombre d'éléments en favoris", example = "8")
    private long totalFavoris;

    @Schema(description = "Extrait chronologique des dernières activités du compte")
    private List<PopulationJournalActiviteItem> dernieresActivites;
}
