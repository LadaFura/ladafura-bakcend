package com.pharmacopee.ladafura.dto.population.commande;

import java.time.LocalDateTime;
import java.util.List;

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
@Schema(description = "Détail complet d'une commande passée par l'utilisateur Population")
public class PopulationCommandeDetailResponse {

    @Schema(description = "Identifiant unique de la commande", example = "42")
    private Long id;

    @Schema(description = "Numéro unique de la commande", example = "CMD-20260928-ABC12")
    private String numero;

    @Schema(description = "Date et heure de création de la commande")
    private LocalDateTime dateCommande;

    @Schema(description = "Dernière mise à jour du statut")
    private LocalDateTime dateMiseAJour;

    @Schema(description = "Statut actuel de la commande", example = "EN_ATTENTE")
    private StatutCommande statut;

    @Schema(description = "Identifiant de la pharmacopée", example = "1")
    private Long pharmacopeeId;

    @Schema(description = "Nom de la pharmacopée", example = "Pharmacie Mandé")
    private String nomPharmacopee;

    @Schema(description = "Numéro de téléphone de la pharmacopée", example = "+223 70 11 22 33")
    private String telephonePharmacopee;

    @Schema(description = "Adresse physique de l'officine", example = "Siby Centre, Route Nationale 5")
    private String adressePharmacopee;

    @Schema(description = "Mode de retrait retenu (LIVRAISON ou PICKUP)", example = "LIVRAISON")
    private String modeRetrait;

    @Schema(description = "Frais de livraison facturés (FCFA)", example = "1500.0")
    private Double montantLivraison;

    @Schema(description = "Adresse de livraison fournie par le client", example = "Badalabougou, Rue 24, Porte 12, Bamako")
    private String adresseLivraison;

    @Schema(description = "Montant total des produits achetés (FCFA)", example = "5000.0")
    private Double totalProduit;

    @Schema(description = "Montant global TTC de la commande (FCFA)", example = "6500.0")
    private Double montantTotal;

    @Schema(description = "Indique si la commande est encore annulable par l'utilisateur", example = "true")
    private Boolean annulable;

    @Schema(description = "Articles composant la commande")
    private List<PopulationLigneCommandeDto> lignes;

    @Schema(description = "Statut du paiement rattaché (ex: EN_ATTENTE, PAYE, NON_APPLICABLE)", example = "EN_ATTENTE")
    private String statutPaiement;
}
