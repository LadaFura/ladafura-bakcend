package com.pharmacopee.ladafura.dto.population.historique;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Historique consolidé d'un produit acheté par le client")
public class PopulationProduitAcheteItem {

    @Schema(description = "Identifiant du produit", example = "5")
    private Long produitId;

    @Schema(description = "Nom commercial du produit", example = "Sirop d'Artemisia")
    private String nomProduit;

    @Schema(description = "Forme galénique", example = "Sirop")
    private String forme;

    @Schema(description = "Photo du produit", example = "https://cdn.ladafura.ml/produits/artemisia.jpg")
    private String photoUrl;

    @Schema(description = "Dernier prix unitaire payé en FCFA", example = "2500.0")
    private Double dernierPrixUnitaire;

    @Schema(description = "Quantité totale cumulée commandée", example = "3")
    private int quantiteTotaleAchetee;

    @Schema(description = "Montant total cumulé dépensé sur ce produit en FCFA", example = "7500.0")
    private Double montantTotalDepense;

    @Schema(description = "Date et heure de la commande la plus récente contenant ce produit", example = "2026-09-28T14:30:00")
    private LocalDateTime dateDernierAchat;

    @Schema(description = "Numéro de la dernière commande", example = "CMD-20260928-8742")
    private String dernierNumeroCommande;

    @Schema(description = "Identifiant de la pharmacopée du dernier achat", example = "3")
    private Long dernierePharmacopeeId;

    @Schema(description = "Nom de la pharmacopée du dernier achat", example = "Pharmacie Mandé")
    private String dernierePharmacopeeNom;

    @Schema(description = "Indique si le client a déjà laissé un avis sur ce produit", example = "true")
    private boolean dejaEvalue;

    @Schema(description = "ID de l'avis laissé (si applicable)", example = "42")
    private Long avisId;
}
