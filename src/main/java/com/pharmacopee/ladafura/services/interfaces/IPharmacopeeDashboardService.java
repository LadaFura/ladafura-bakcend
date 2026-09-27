package com.pharmacopee.ladafura.services.interfaces;

import com.pharmacopee.ladafura.dto.pharmacopee.dashboard.PharmacopeeDashboardResponse;

public interface IPharmacopeeDashboardService {

    /**
     * Récupère l'ensemble des indicateurs réels du tableau de bord consolidé de la pharmacopée :
     * - Statut de référencement & coordonnées
     * - Inventaire, disponibilité des produits et valorisation marchande des stocks
     * - Suivi des commandes, flux Livraison/Pickup et chiffre d'affaires
     * - Synthèse de réputation et avis clients validés
     * - Alertes et notifications non lues
     */
    PharmacopeeDashboardResponse getDashboard();
}
