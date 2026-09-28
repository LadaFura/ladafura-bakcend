package com.pharmacopee.ladafura.dto.population.commande;

import java.time.LocalDateTime;

import com.pharmacopee.ladafura.enums.StatutCommande;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Aperçu synthétique d'une commande pour l'historique utilisateur")
public class PopulationCommandeSummaryResponse {

    @Schema(description = "Identifiant unique de la commande", example = "42")
    private Long id;

    @Schema(description = "Numéro de référence de la commande", example = "CMD-20260928-ABC12")
    private String numero;

    @Schema(description = "Date et heure de passage de la commande")
    private LocalDateTime dateCommande;

    @Schema(description = "Statut actuel d'acheminement de la commande", example = "EN_ATTENTE")
    private StatutCommande statut;

    @Schema(description = "Identifiant de la pharmacopée", example = "1")
    private Long pharmacopeeId;

    @Schema(description = "Nom de la pharmacopée", example = "Pharmacie Mandé")
    private String nomPharmacopee;

    @Schema(description = "Type de mode de retrait (LIVRAISON ou PICKUP)", example = "LIVRAISON")
    private String modeRetrait;

    @Schema(description = "Sous-total des produits (FCFA)", example = "5000.0")
    private Double totalProduit;

    @Schema(description = "Frais de livraison (FCFA)", example = "1500.0")
    private Double montantLivraison;

    @Schema(description = "Montant total de la commande (FCFA)", example = "6500.0")
    private Double montantTotal;

    @Schema(description = "Nombre d'articles commandés", example = "2")
    private Integer nombreArticles;

    @Schema(description = "Indique si la commande peut encore être annulée par le client", example = "true")
    private Boolean annulable;
}
