package com.pharmacopee.ladafura.dto.population.avis;

import java.time.LocalDateTime;

import com.pharmacopee.ladafura.enums.StatutAvis;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Détail d'un avis client et statut de modération")
public class PopulationAvisResponse {

    @Schema(description = "Identifiant de l'avis", example = "12")
    private Long id;

    @Schema(description = "Identifiant du produit évalué", example = "5")
    private Long produitId;

    @Schema(description = "Nom du produit", example = "Sirop d'Artemisia")
    private String nomProduit;

    @Schema(description = "Forme galénique", example = "Sirop 250ml")
    private String formeProduit;

    @Schema(description = "Photo du produit", example = "https://storage.ladafura.ml/produits/artemisia.jpg")
    private String photoProduitUrl;

    @Schema(description = "Note attribuée (1 à 5)", example = "5")
    private Integer note;

    @Schema(description = "Commentaire rédigé", example = "Très efficace et naturel !")
    private String commentaire;

    @Schema(description = "Date et heure de dépôt de l'avis")
    private LocalDateTime dateAvis;

    @Schema(description = "Statut actuel de modération", example = "EN_ATTENTE")
    private StatutAvis statut;

    @Schema(description = "Libellé convivial du statut", example = "En attente de modération")
    private String statutLibelle;
}
