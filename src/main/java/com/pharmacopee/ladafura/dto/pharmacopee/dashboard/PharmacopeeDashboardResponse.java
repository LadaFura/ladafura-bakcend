package com.pharmacopee.ladafura.dto.pharmacopee.dashboard;

import java.util.List;

import com.pharmacopee.ladafura.dto.pharmacopee.commande.PharmacopeeCommandeItemResponse;
import com.pharmacopee.ladafura.enums.StatutPharmacopee;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Tableau de bord consolidé de l'officine de pharmacopée")
public class PharmacopeeDashboardResponse {

    @Schema(description = "Volet Référencement & Établissement")
    private ReferencementSection referencement;

    @Schema(description = "Volet Produits, Stocks & Valeur Marchande")
    private ProduitsStockSection inventaire;

    @Schema(description = "Volet Commandes & Chiffre d'Affaires")
    private CommandesSection commandes;

    @Schema(description = "Volet Avis Clients & Satisfaction")
    private AvisSection avis;

    @Schema(description = "Volet Alertes & Notifications")
    private NotificationsSection notifications;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Statut administratif et coordonnées de l'officine")
    public static class ReferencementSection {
        private Long pharmacopeeId;
        private String nomPharmacopee;
        private String telephone;
        private StatutPharmacopee statut;
        private boolean estValidee;
        private String region;
        private String cercle;
        private String commune;
        private String localite;
        private Double latitude;
        private Double longitude;
        private boolean livraisonActive;
        private Double fraisLivraison;
        private boolean pickupActif;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Statistiques sur le catalogue, le stock et la valorisation")
    public static class ProduitsStockSection {
        private long totalReferences;
        private long referencesActives;
        private long referencesDesactivees;
        private double tauxDisponibilite;
        private long referencesEnStock;
        private long referencesEnRupture;
        private double valeurTotaleStock;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Statistiques sur les commandes et les ventes")
    public static class CommandesSection {
        private long totalCommandes;
        private long commandesEnAttente;
        private long commandesConfirmees;
        private long commandesPreparees;
        private long commandesEnCoursAcheminement;
        private long commandesTerminees;
        private long commandesAnnulees;
        private double chiffreAffairesTotal;
        private List<PharmacopeeCommandeItemResponse> dernieresCommandes;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Statistiques sur la réputation et la satisfaction des clients")
    public static class AvisSection {
        private long totalAvis;
        private Double noteMoyenneGlobale;
        private int nombreProduitsEvalues;
        private long total5Etoiles;
        private long total4Etoiles;
        private long total3Etoiles;
        private long total2Etoiles;
        private long total1Etoile;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "État du centre de notifications de l'officine")
    public static class NotificationsSection {
        private long totalNonLues;
        private long nonLuesCommandes;
        private long nonLuesAlertesStock;
    }
}
