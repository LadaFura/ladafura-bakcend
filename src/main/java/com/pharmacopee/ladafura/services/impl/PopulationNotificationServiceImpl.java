package com.pharmacopee.ladafura.services.impl;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pharmacopee.ladafura.Models.Notification;
import com.pharmacopee.ladafura.Models.Utilisateur;
import com.pharmacopee.ladafura.dto.population.notification.PopulationNotificationCountResponse;
import com.pharmacopee.ladafura.dto.population.notification.PopulationNotificationResponse;
import com.pharmacopee.ladafura.dto.population.notification.PopulationNotificationStatsResponse;
import com.pharmacopee.ladafura.enums.NiveauNotification;
import com.pharmacopee.ladafura.enums.TypeNotification;
import com.pharmacopee.ladafura.exceptions.ResourceNotFoundException;
import com.pharmacopee.ladafura.mappers.PopulationNotificationMapper;
import com.pharmacopee.ladafura.repository.NotificationRepository;
import com.pharmacopee.ladafura.services.interfaces.IPopulationAuthService;
import com.pharmacopee.ladafura.services.interfaces.IPopulationNotificationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PopulationNotificationServiceImpl implements IPopulationNotificationService {

    private final NotificationRepository notificationRepository;
    private final IPopulationAuthService populationAuthService;
    private final PopulationNotificationMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public Page<PopulationNotificationResponse> getMesNotifications(Boolean lue, TypeNotification type, Pageable pageable) {
        Utilisateur user = populationAuthService.getCurrentPopulationUser();
        log.info("Consultation des notifications population pour l'utilisateur ID: {} (lue={}, type={})", user.getId(), lue, type);

        Page<Notification> page;
        if (lue != null && type != null) {
            page = notificationRepository.findByUtilisateurIdAndLueAndType(user.getId(), lue, type, pageable);
        } else if (lue != null) {
            page = notificationRepository.findByUtilisateurIdAndLue(user.getId(), lue, pageable);
        } else if (type != null) {
            page = notificationRepository.findByUtilisateurIdAndType(user.getId(), type, pageable);
        } else {
            page = notificationRepository.findByUtilisateurId(user.getId(), pageable);
        }

        return page.map(mapper::toDto);
    }

    @Override
    public PopulationNotificationResponse getNotificationById(Long id) {
        Utilisateur user = populationAuthService.getCurrentPopulationUser();
        log.info("Consultation du détail de la notification ID: {} pour l'utilisateur ID: {}", id, user.getId());

        Notification notification = notificationRepository.findByIdAndUtilisateurId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Notification", "id", id));

        // Marquage automatique comme lue lors de la première consultation
        if (Boolean.FALSE.equals(notification.getLue())) {
            notification.setLue(true);
            notification.setDateLecture(LocalDateTime.now());
            notification = notificationRepository.save(notification);
            log.info("Notification ID: {} automatiquement marquée comme lue pour l'utilisateur ID: {}", id, user.getId());
        }

        return mapper.toDto(notification);
    }

    @Override
    public PopulationNotificationResponse markAsRead(Long id) {
        Utilisateur user = populationAuthService.getCurrentPopulationUser();
        log.info("Marquage explicite comme lue de la notification ID: {} pour l'utilisateur ID: {}", id, user.getId());

        Notification notification = notificationRepository.findByIdAndUtilisateurId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Notification", "id", id));

        notification.setLue(true);
        notification.setDateLecture(LocalDateTime.now());
        Notification saved = notificationRepository.save(notification);

        return mapper.toDto(saved);
    }

    @Override
    public void markAllAsRead() {
        Utilisateur user = populationAuthService.getCurrentPopulationUser();
        log.info("Marquage de toutes les notifications non lues comme lues pour l'utilisateur ID: {}", user.getId());
        notificationRepository.markAllAsReadByUtilisateurId(user.getId());
    }

    @Override
    public void deleteNotification(Long id) {
        Utilisateur user = populationAuthService.getCurrentPopulationUser();
        log.info("Suppression de la notification ID: {} pour l'utilisateur ID: {}", id, user.getId());

        Notification notification = notificationRepository.findByIdAndUtilisateurId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Notification", "id", id));

        notificationRepository.delete(notification);
        log.info("Notification ID: {} supprimée avec succès pour l'utilisateur ID: {}", id, user.getId());
    }

    @Override
    public void clearAllReadNotifications() {
        Utilisateur user = populationAuthService.getCurrentPopulationUser();
        log.info("Suppression en masse des notifications déjà lues pour l'utilisateur ID: {}", user.getId());
        notificationRepository.deleteAllReadByUtilisateurId(user.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public PopulationNotificationStatsResponse getStats() {
        Utilisateur user = populationAuthService.getCurrentPopulationUser();
        log.info("Calcul des statistiques de notifications pour l'utilisateur ID: {}", user.getId());

        long total = notificationRepository.countByUtilisateurId(user.getId());
        long nonLues = notificationRepository.countByUtilisateurIdAndLueFalse(user.getId());
        long lues = notificationRepository.countByUtilisateurIdAndLueTrue(user.getId());

        long cmdNouvelle = notificationRepository.countByUtilisateurIdAndType(user.getId(), TypeNotification.COMMANDE_NOUVELLE);
        long cmdStatut = notificationRepository.countByUtilisateurIdAndType(user.getId(), TypeNotification.COMMANDE_STATUT);
        long avis = notificationRepository.countByUtilisateurIdAndType(user.getId(), TypeNotification.AVIS_NOUVEAU);
        long infos = notificationRepository.countByUtilisateurIdAndType(user.getId(), TypeNotification.INFO_IMPORTANTE);
        long stock = notificationRepository.countByUtilisateurIdAndType(user.getId(), TypeNotification.STOCK_ALERTE);

        return mapper.toStatsResponse(total, nonLues, lues, cmdNouvelle + cmdStatut, avis, infos + stock);
    }

    @Override
    @Transactional(readOnly = true)
    public PopulationNotificationCountResponse getUnreadCount() {
        Utilisateur user = populationAuthService.getCurrentPopulationUser();
        long unread = notificationRepository.countByUtilisateurIdAndLueFalse(user.getId());
        return mapper.toCountResponse(unread);
    }

    @Override
    public void envoyerNotification(Utilisateur destinataire, String titre, String message,
                                     TypeNotification type, NiveauNotification niveau,
                                     String referenceId, String lien) {
        log.info("Envoi notification de type {} à l'utilisateur ID: {}", type, destinataire.getId());
        Notification notification = Notification.builder()
                .utilisateur(destinataire)
                .titre(titre)
                .message(message)
                .type(type)
                .niveau(niveau != null ? niveau : NiveauNotification.INFO)
                .referenceId(referenceId)
                .lien(lien)
                .lue(false)
                .dateNotification(LocalDateTime.now())
                .build();

        notificationRepository.save(notification);
    }
}
