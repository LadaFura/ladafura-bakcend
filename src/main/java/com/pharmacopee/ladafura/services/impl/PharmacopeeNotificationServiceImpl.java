package com.pharmacopee.ladafura.services.impl;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pharmacopee.ladafura.Models.Notification;
import com.pharmacopee.ladafura.Models.Pharmacopee;
import com.pharmacopee.ladafura.dto.pharmacopee.notification.CreateNotificationRequest;
import com.pharmacopee.ladafura.dto.pharmacopee.notification.NotificationResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.notification.NotificationSummaryResponse;
import com.pharmacopee.ladafura.enums.NiveauNotification;
import com.pharmacopee.ladafura.enums.TypeNotification;
import com.pharmacopee.ladafura.exceptions.ResourceNotFoundException;
import com.pharmacopee.ladafura.repository.NotificationRepository;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeAuthService;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeNotificationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PharmacopeeNotificationServiceImpl implements IPharmacopeeNotificationService {

    private final IPharmacopeeAuthService pharmacopeeAuthService;
    private final NotificationRepository notificationRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationResponse> getNotifications(Boolean nonLuesSeulement, TypeNotification type, Pageable pageable) {
        Pharmacopee pharmacopee = pharmacopeeAuthService.getCurrentPharmacopee();
        Long pId = pharmacopee.getId();
        log.info("Consultation paginée des notifications pour la pharmacopée ID {} (nonLuesSeulement={}, type={})",
                pId, nonLuesSeulement, type);

        Page<Notification> page;
        if (Boolean.TRUE.equals(nonLuesSeulement) && type != null) {
            page = notificationRepository.findByPharmacopeeIdAndLueAndType(pId, false, type, pageable);
        } else if (Boolean.TRUE.equals(nonLuesSeulement)) {
            page = notificationRepository.findByPharmacopeeIdAndLue(pId, false, pageable);
        } else if (type != null) {
            page = notificationRepository.findByPharmacopeeIdAndType(pId, type, pageable);
        } else {
            page = notificationRepository.findByPharmacopeeId(pId, pageable);
        }

        return page.map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationSummaryResponse getSummary() {
        Pharmacopee pharmacopee = pharmacopeeAuthService.getCurrentPharmacopee();
        Long pId = pharmacopee.getId();
        log.info("Calcul de la synthèse des notifications pour la pharmacopée ID {}", pId);

        long total = notificationRepository.countByPharmacopeeId(pId);
        long nonLues = notificationRepository.countByPharmacopeeIdAndLueFalse(pId);

        long nonLuesCmd = notificationRepository.countByPharmacopeeIdAndLueFalseAndType(pId, TypeNotification.COMMANDE_NOUVELLE)
                + notificationRepository.countByPharmacopeeIdAndLueFalseAndType(pId, TypeNotification.COMMANDE_STATUT);
        long nonLuesRef = notificationRepository.countByPharmacopeeIdAndLueFalseAndType(pId, TypeNotification.REFERENCEMENT);
        long nonLuesAvis = notificationRepository.countByPharmacopeeIdAndLueFalseAndType(pId, TypeNotification.AVIS_NOUVEAU);
        long nonLuesStock = notificationRepository.countByPharmacopeeIdAndLueFalseAndType(pId, TypeNotification.STOCK_ALERTE);
        long nonLuesInfo = notificationRepository.countByPharmacopeeIdAndLueFalseAndType(pId, TypeNotification.INFO_IMPORTANTE);

        return NotificationSummaryResponse.builder()
                .totalNotifications(total)
                .totalNonLues(nonLues)
                .nonLuesCommandes(nonLuesCmd)
                .nonLuesReferencement(nonLuesRef)
                .nonLuesAvis(nonLuesAvis)
                .nonLuesStock(nonLuesStock)
                .nonLuesInfoImportante(nonLuesInfo)
                .build();
    }

    @Override
    public NotificationResponse marquerCommeLue(Long id) {
        Pharmacopee pharmacopee = pharmacopeeAuthService.getCurrentPharmacopee();
        Long pId = pharmacopee.getId();
        log.info("Marquage de la notification ID {} comme lue pour la pharmacopée ID {}", id, pId);

        Notification n = notificationRepository.findByIdAndPharmacopeeId(id, pId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification introuvable avec l'ID " + id
                        + " pour votre officine."));

        if (!Boolean.TRUE.equals(n.getLue())) {
            n.setLue(true);
            n.setDateLecture(LocalDateTime.now());
            n = notificationRepository.save(n);
        }

        return mapToResponse(n);
    }

    @Override
    public void marquerToutesCommeLues() {
        Pharmacopee pharmacopee = pharmacopeeAuthService.getCurrentPharmacopee();
        Long pId = pharmacopee.getId();
        log.info("Marquage de toutes les notifications comme lues pour la pharmacopée ID {}", pId);
        notificationRepository.markAllAsReadByPharmacopeeId(pId);
    }

    @Override
    public void supprimerNotification(Long id) {
        Pharmacopee pharmacopee = pharmacopeeAuthService.getCurrentPharmacopee();
        Long pId = pharmacopee.getId();
        log.info("Suppression de la notification ID {} pour la pharmacopée ID {}", id, pId);

        Notification n = notificationRepository.findByIdAndPharmacopeeId(id, pId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification introuvable avec l'ID " + id
                        + " pour votre officine."));

        notificationRepository.delete(n);
    }

    @Override
    public NotificationResponse creerNotification(CreateNotificationRequest request) {
        Pharmacopee pharmacopee = pharmacopeeAuthService.getCurrentPharmacopee();
        log.info("Création manuelle d'une notification pour la pharmacopée ID {} : {}",
                pharmacopee.getId(), request.getTitre());

        Notification n = Notification.builder()
                .pharmacopee(pharmacopee)
                .utilisateur(pharmacopee.getUtilisateur())
                .titre(request.getTitre())
                .message(request.getMessage())
                .type(request.getType())
                .niveau(request.getNiveau() != null ? request.getNiveau() : NiveauNotification.INFO)
                .referenceId(request.getReferenceId())
                .lien(request.getLien())
                .lue(false)
                .dateNotification(LocalDateTime.now())
                .build();

        Notification saved = notificationRepository.save(n);
        return mapToResponse(saved);
    }

    @Override
    public void notifier(Pharmacopee pharmacopee, TypeNotification type, NiveauNotification niveau,
                         String titre, String message, String referenceId, String lien) {
        if (pharmacopee == null) {
            log.warn("Impossible d'envoyer la notification : pharmacopée null");
            return;
        }

        Notification n = Notification.builder()
                .pharmacopee(pharmacopee)
                .utilisateur(pharmacopee.getUtilisateur())
                .titre(titre)
                .message(message)
                .type(type)
                .niveau(niveau != null ? niveau : NiveauNotification.INFO)
                .referenceId(referenceId)
                .lien(lien)
                .lue(false)
                .dateNotification(LocalDateTime.now())
                .build();

        notificationRepository.save(n);
        log.info("Notification émise pour la pharmacopée ID {} : [{}] {}", pharmacopee.getId(), type, titre);
    }

    private NotificationResponse mapToResponse(Notification n) {
        return NotificationResponse.builder()
                .id(n.getId())
                .titre(n.getTitre())
                .message(n.getMessage())
                .type(n.getType())
                .niveau(n.getNiveau())
                .lue(n.getLue())
                .dateNotification(n.getDateNotification())
                .dateLecture(n.getDateLecture())
                .referenceId(n.getReferenceId())
                .lien(n.getLien())
                .build();
    }
}
