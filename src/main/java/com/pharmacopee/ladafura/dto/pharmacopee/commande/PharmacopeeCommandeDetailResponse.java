package com.pharmacopee.ladafura.dto.pharmacopee.commande;

import java.time.LocalDateTime;
import java.util.List;

import com.pharmacopee.ladafura.dto.pharmacopee.retrait.ModeRetraitResponse;
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
@Schema(description = "Fiche détaillée et complète d'une commande pour l'officine")
public class PharmacopeeCommandeDetailResponse {

    @Schema(description = "Identifiant de la commande", example = "10")
    private Long id;

    @Schema(description = "Numéro unique de la commande", example = "CMD-2026-00042")
    private String numero;

    @Schema(description = "Date de passage de commande")
    private LocalDateTime dateCommande;

    @Schema(description = "Dernière mise à jour du statut")
    private LocalDateTime dateMiseAJour;

    @Schema(description = "Statut actuel de la commande", example = "PREPAREE")
    private StatutCommande statut;

    @Schema(description = "Montant total des produits (FCFA)", example = "5000.0")
    private Double totalProduit;

    @Schema(description = "Frais de livraison (FCFA)", example = "2000.0")
    private Double montantLivraison;

    @Schema(description = "Montant total de la commande (FCFA)", example = "7000.0")
    private Double montantTotal;

    @Schema(description = "Adresse physique de livraison (si mode LIVRAISON)", example = "Kalaban Coura, Rue 105, Porte 24, Bamako")
    private String adresseLivraison;

    @Schema(description = "Coordonnées de l'acheteur")
    private ClientInfoResponse client;

    @Schema(description = "Mode de mise à disposition (LIVRAISON ou PICKUP)")
    private ModeRetraitResponse modeRetrait;

    @Schema(description = "Articles commandés")
    private List<LigneCommandeResponse> lignes;

    @Schema(description = "Informations de règlement")
    private PaiementInfoResponse paiement;

    @Schema(description = "Liste des prochains statuts valides vers lesquels cette commande peut évoluer")
    private List<StatutCommande> prochainsStatutsAutorises;
}
