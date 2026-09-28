package com.pharmacopee.ladafura.services.interfaces;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.pharmacopee.ladafura.Models.AgentCollecte;
import com.pharmacopee.ladafura.Models.Collecte;
import com.pharmacopee.ladafura.Models.Notification;
import com.pharmacopee.ladafura.dto.agent.notification.AgentNotificationResponse;
import com.pharmacopee.ladafura.dto.agent.notification.AgentNotificationStatsResponse;
import com.pharmacopee.ladafura.enums.NiveauNotification;
import com.pharmacopee.ladafura.enums.StatutCollecte;
import com.pharmacopee.ladafura.enums.TypeNotification;

public interface IAgentNotificationService {

    /**
     * Récupère la liste paginée des notifications de l'agent connecté avec filtres optionnels.
     *
     * @param lue Filtre statut de lecture (true, false, ou null pour tout)
     * @param type Filtre par type de notification
     * @param pageable Pagination et tri
     * @return Page de AgentNotificationResponse
     */
    Page<AgentNotificationResponse> getMyNotifications(Boolean lue, TypeNotification type, Pageable pageable);

    /**
     * Consulte le détail d'une notification et la marque automatiquement comme lue si elle ne l'était pas.
     *
     * @param id Identifiant de la notification
     * @return DTO AgentNotificationResponse
     */
    AgentNotificationResponse getNotificationById(Long id);

    /**
     * Marque explicitement une notification comme lue.
     *
     * @param id Identifiant de la notification
     * @return DTO AgentNotificationResponse mis à jour
     */
    AgentNotificationResponse markAsRead(Long id);

    /**
     * Marque l'ensemble des notifications non lues de l'agent connecté comme lues.
     */
    void markAllAsRead();

    /**
     * Supprime définitivement une notification de l'agent connecté.
     *
     * @param id Identifiant de la notification
     */
    void deleteNotification(Long id);

    /**
     * Calcule les compteurs statistiques de notifications de l'agent connecté.
     *
     * @return DTO AgentNotificationStatsResponse
     */
    AgentNotificationStatsResponse getStats();

    /**
     * Génère et enregistre une notification de validation d'une collecte.
     *
     * @param collecte Collecte validée
     * @return Notification persistée
     */
    Notification notifierValidationCollecte(Collecte collecte);

    /**
     * Génère et enregistre une notification de rejet d'une collecte avec le motif officiel.
     *
     * @param collecte Collecte rejetée
     * @param motifRejet Motif du rejet
     * @return Notification persistée
     */
    Notification notifierRejetCollecte(Collecte collecte, String motifRejet);

    /**
     * Génère et enregistre une notification de demande de correction de collecte.
     *
     * @param collecte Collecte concernée
     * @param instructions Instructions correctives
     * @return Notification persistée
     */
    Notification notifierDemandeCorrection(Collecte collecte, String instructions);

    /**
     * Génère et enregistre une notification de changement de statut d'une collecte.
     *
     * @param collecte Collecte concernée
     * @param ancienStatut Ancien statut
     * @param nouveauStatut Nouveau statut
     * @return Notification persistée
     */
    Notification notifierChangementStatut(Collecte collecte, StatutCollecte ancienStatut, StatutCollecte nouveauStatut);

    /**
     * Génère et enregistre une notification d'information importante émanant de l'administrateur.
     *
     * @param agent Agent destinataire
     * @param titre Titre du message
     * @param message Contenu du message
     * @param niveau Niveau d'importance
     * @return Notification persistée
     */
    Notification notifierMessageAdmin(AgentCollecte agent, String titre, String message, NiveauNotification niveau);
}
