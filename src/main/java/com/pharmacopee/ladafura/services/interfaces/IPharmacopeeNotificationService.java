package com.pharmacopee.ladafura.services.interfaces;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.pharmacopee.ladafura.Models.Pharmacopee;
import com.pharmacopee.ladafura.dto.pharmacopee.notification.CreateNotificationRequest;
import com.pharmacopee.ladafura.dto.pharmacopee.notification.NotificationResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.notification.NotificationSummaryResponse;
import com.pharmacopee.ladafura.enums.NiveauNotification;
import com.pharmacopee.ladafura.enums.TypeNotification;

public interface IPharmacopeeNotificationService {

    /**
     * Liste paginée des notifications de l'officine avec filtres optionnels (non lues, type).
     */
    Page<NotificationResponse> getNotifications(Boolean nonLuesSeulement, TypeNotification type, Pageable pageable);

    /**
     * Synthèse et compteurs des notifications (total non lues pour badge cloche, détail par type).
     */
    NotificationSummaryResponse getSummary();

    /**
     * Marque une notification spécifique comme lue.
     */
    NotificationResponse marquerCommeLue(Long id);

    /**
     * Marque toutes les notifications non lues de l'officine comme lues en une seule action.
     */
    void marquerToutesCommeLues();

    /**
     * Supprime définitivement une notification de l'historique de l'officine.
     */
    void supprimerNotification(Long id);

    /**
     * Émet une notification pour l'officine connectée (test / création explicite).
     */
    NotificationResponse creerNotification(CreateNotificationRequest request);

    /**
     * Méthode utilitaire interne pour envoyer une notification à une pharmacopée ciblée.
     */
    void notifier(Pharmacopee pharmacopee, TypeNotification type, NiveauNotification niveau,
                  String titre, String message, String referenceId, String lien);
}
