package com.pharmacopee.ladafura.dto.agent.dashboard;

import java.util.List;

import com.pharmacopee.ladafura.dto.agent.collecte.AgentCollecteSummaryResponse;
import com.pharmacopee.ladafura.dto.agent.notification.AgentNotificationResponse;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Tableau de bord consolidé pour l'Agent de Collecte")
public class AgentDashboardResponse {

    @Schema(description = "Volet Informations de l'Agent connecté")
    private AgentInfoSection agent;

    @Schema(description = "Volet Statistiques globales des collectes de l'agent")
    private CollectesStatsSection statistiques;

    @Schema(description = "Volet Notifications & Alertes")
    private NotificationsSection notifications;

    @Schema(description = "Liste des 5 dernières collectes réalisées par l'agent")
    private List<AgentCollecteSummaryResponse> dernieresCollectes;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Informations synthétiques du profil de l'agent")
    public static class AgentInfoSection {
        @Schema(description = "Identifiant de l'agent", example = "10")
        private Long agentId;

        @Schema(description = "Nom de famille", example = "Traoré")
        private String nom;

        @Schema(description = "Prénom", example = "Bakary")
        private String prenom;

        @Schema(description = "Adresse email", example = "agent.traore@ladafura.ml")
        private String email;

        @Schema(description = "Numéro de téléphone", example = "+223 76 00 11 22")
        private String telephone;

        @Schema(description = "Matricule officiel", example = "AGT-2026-0042")
        private String matricule;

        @Schema(description = "Zone géographique assignée", example = "Région de Sikasso")
        private String zoneCouverture;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Synthèse chiffrée et réelle des collectes de terrain de l'agent")
    public static class CollectesStatsSection {
        @Schema(description = "Nombre total de collectes", example = "25")
        private long total;

        @Schema(description = "Nombre de brouillons en cours d'édition", example = "6")
        private long brouillons;

        @Schema(description = "Nombre total de collectes en attente de validation (SOUMISE ou EN_EXAMEN)", example = "7")
        private long enAttente;

        @Schema(description = "Détail : collectes soumises", example = "4")
        private long soumises;

        @Schema(description = "Détail : collectes en cours d'examen", example = "3")
        private long enExamen;

        @Schema(description = "Nombre de collectes validées et publiées", example = "10")
        private long validees;

        @Schema(description = "Nombre de collectes rejetées nécessitant correction", example = "2")
        private long rejetees;

        @Schema(description = "Taux de validation (%) des collectes instruites", example = "83.3")
        private double tauxValidation;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Synthèse des alertes et notifications de l'agent")
    public static class NotificationsSection {
        @Schema(description = "Nombre total de notifications reçues", example = "18")
        private long total;

        @Schema(description = "Nombre de notifications non lues", example = "3")
        private long nonLues;

        @Schema(description = "Dernières notifications reçues pour affichage rapide")
        private List<AgentNotificationResponse> dernieresNotifications;
    }
}
