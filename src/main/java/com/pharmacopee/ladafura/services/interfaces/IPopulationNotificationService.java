package com.pharmacopee.ladafura.services.interfaces;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.pharmacopee.ladafura.Models.Utilisateur;
import com.pharmacopee.ladafura.dto.population.notification.PopulationNotificationCountResponse;
import com.pharmacopee.ladafura.dto.population.notification.PopulationNotificationResponse;
import com.pharmacopee.ladafura.dto.population.notification.PopulationNotificationStatsResponse;
import com.pharmacopee.ladafura.enums.NiveauNotification;
import com.pharmacopee.ladafura.enums.TypeNotification;

public interface IPopulationNotificationService {

    /**
     * Récupère la liste paginée des notifications de l'utilisateur Population connecté,
     * avec filtres optionnels par état de lecture et type.
     */
    Page<PopulationNotificationResponse> getMesNotifications(Boolean lue, TypeNotification type, Pageable pageable);

    /**
     * Récupère le détail d'une notification et la marque automatiquement comme lue.
     */
    PopulationNotificationResponse getNotificationById(Long id);

    /**
     * Marque explicitement une notification comme lue.
     */
    PopulationNotificationResponse markAsRead(Long id);

    /**
     * Marque toutes les notifications non lues de l'utilisateur comme lues.
     */
    void markAllAsRead();

    /**
     * Supprime définitivement une notification de l'historique du client.
     */
    void deleteNotification(Long id);

    /**
     * Supprime en masse toutes les notifications déjà lues de l'utilisateur connecté.
     */
    void clearAllReadNotifications();

    /**
     * Calcule les statistiques et compteurs du centre de notifications.
     */
    PopulationNotificationStatsResponse getStats();

    /**
     * Récupère le nombre de notifications non lues (pour l'affichage d'un badge UI).
     */
    PopulationNotificationCountResponse getUnreadCount();

    /**
     * Méthode interne/métier d'envoi d'une notification à un utilisateur de la population.
     */
    void envoyerNotification(Utilisateur destinataire, String titre, String message,
                             TypeNotification type, NiveauNotification niveau,
                             String referenceId, String lien);
}
