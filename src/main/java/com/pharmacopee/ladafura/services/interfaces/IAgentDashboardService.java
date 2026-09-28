package com.pharmacopee.ladafura.services.interfaces;

import com.pharmacopee.ladafura.dto.agent.dashboard.AgentDashboardResponse;

public interface IAgentDashboardService {

    /**
     * Génère et renvoie le tableau de bord consolidé pour l'Agent de Collecte actuellement connecté.
     * Les données et métriques sont 100% réelles, calculées à partir des collectes et des notifications en base.
     *
     * @return AgentDashboardResponse contenant le profil, les statistiques de collectes, les notifications et les 5 dernières collectes.
     */
    AgentDashboardResponse getDashboard();
}
