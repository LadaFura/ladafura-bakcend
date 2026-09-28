package com.pharmacopee.ladafura.services.impl;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pharmacopee.ladafura.Models.AgentCollecte;
import com.pharmacopee.ladafura.Models.Collecte;
import com.pharmacopee.ladafura.Models.Notification;
import com.pharmacopee.ladafura.dto.agent.notification.AgentNotificationResponse;
import com.pharmacopee.ladafura.dto.agent.notification.AgentNotificationStatsResponse;
import com.pharmacopee.ladafura.enums.NiveauNotification;
import com.pharmacopee.ladafura.enums.StatutCollecte;
import com.pharmacopee.ladafura.enums.TypeNotification;
import com.pharmacopee.ladafura.exceptions.ResourceNotFoundException;
import com.pharmacopee.ladafura.mappers.AgentNotificationMapper;
import com.pharmacopee.ladafura.repository.NotificationRepository;
import com.pharmacopee.ladafura.services.interfaces.IAgentAuthService;
import com.pharmacopee.ladafura.services.interfaces.IAgentNotificationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AgentNotificationServiceImpl implements IAgentNotificationService {

    private final NotificationRepository notificationRepository;
    private final AgentNotificationMapper agentNotificationMapper;
    private final IAgentAuthService agentAuthService;

    @Override
    @Transactional(readOnly = true)
    public Page<AgentNotificationResponse> getMyNotifications(Boolean lue, TypeNotification type, Pageable pageable) {
        AgentCollecte currentAgent = agentAuthService.getCurrentAgent();
        Long agentId = currentAgent.getId();

        Page<Notification> page;
        if (lue != null && type != null) {
            page = notificationRepository.findByUtilisateurIdAndLueAndType(agentId, lue, type, pageable);
        } else if (lue != null) {
            page = notificationRepository.findByUtilisateurIdAndLue(agentId, lue, pageable);
        } else if (type != null) {
            page = notificationRepository.findByUtilisateurIdAndType(agentId, type, pageable);
        } else {
            page = notificationRepository.findByUtilisateurId(agentId, pageable);
        }

        return page.map(agentNotificationMapper::toDto);
    }

    @Override
    @Transactional
    public AgentNotificationResponse getNotificationById(Long id) {
        AgentCollecte currentAgent = agentAuthService.getCurrentAgent();
        Notification notification = notificationRepository.findByIdAndUtilisateurId(id, currentAgent.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Notification", "id", id));

        if (!Boolean.TRUE.equals(notification.getLue())) {
            notification.setLue(true);
            notification.setDateLecture(LocalDateTime.now());
            notificationRepository.save(notification);
            log.info("Notification ID {} marquée automatiquement comme lue par l'agent ID {}", id, currentAgent.getId());
        }

        return agentNotificationMapper.toDto(notification);
    }

    @Override
    @Transactional
    public AgentNotificationResponse markAsRead(Long id) {
        AgentCollecte currentAgent = agentAuthService.getCurrentAgent();
        Notification notification = notificationRepository.findByIdAndUtilisateurId(id, currentAgent.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Notification", "id", id));

        notification.setLue(true);
        notification.setDateLecture(LocalDateTime.now());
        Notification saved = notificationRepository.save(notification);
        log.info("Notification ID {} marquée comme lue", id);
        return agentNotificationMapper.toDto(saved);
    }

    @Override
    @Transactional
    public void markAllAsRead() {
        AgentCollecte currentAgent = agentAuthService.getCurrentAgent();
        notificationRepository.markAllAsReadByUtilisateurId(currentAgent.getId());
        log.info("Toutes les notifications de l'agent ID {} ont été marquées comme lues", currentAgent.getId());
    }

    @Override
    @Transactional
    public void deleteNotification(Long id) {
        AgentCollecte currentAgent = agentAuthService.getCurrentAgent();
        Notification notification = notificationRepository.findByIdAndUtilisateurId(id, currentAgent.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Notification", "id", id));

        notificationRepository.delete(notification);
        log.info("Notification ID {} supprimée par l'agent ID {}", id, currentAgent.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public AgentNotificationStatsResponse getStats() {
        AgentCollecte currentAgent = agentAuthService.getCurrentAgent();
        Long agentId = currentAgent.getId();

        long total = notificationRepository.countByUtilisateurId(agentId);
        long nonLues = notificationRepository.countByUtilisateurIdAndLueFalse(agentId);
        long lues = notificationRepository.countByUtilisateurIdAndLueTrue(agentId);
        long validations = notificationRepository.countByUtilisateurIdAndType(agentId, TypeNotification.COLLECTE_VALIDEE);
        long rejets = notificationRepository.countByUtilisateurIdAndType(agentId, TypeNotification.COLLECTE_REJETEE);
        long corrections = notificationRepository.countByUtilisateurIdAndType(agentId, TypeNotification.COLLECTE_CORRECTION);
        long changementsStatut = notificationRepository.countByUtilisateurIdAndType(agentId, TypeNotification.COLLECTE_STATUT);
        long infosImportantes = notificationRepository.countByUtilisateurIdAndType(agentId, TypeNotification.INFO_IMPORTANTE);

        return AgentNotificationStatsResponse.builder()
                .total(total)
                .nonLues(nonLues)
                .lues(lues)
                .validations(validations)
                .rejets(rejets)
                .corrections(corrections)
                .changementsStatut(changementsStatut)
                .infosImportantes(infosImportantes)
                .build();
    }

    @Override
    @Transactional
    public Notification notifierValidationCollecte(Collecte collecte) {
        if (collecte == null || collecte.getAgentCollecte() == null) return null;

        Notification notification = Notification.builder()
                .utilisateur(collecte.getAgentCollecte())
                .titre("Collecte #" + collecte.getId() + " validée")
                .message("Votre fiche de collecte #" + collecte.getId() + " a été validée avec succès par le comité de modération.")
                .type(TypeNotification.COLLECTE_VALIDEE)
                .niveau(NiveauNotification.SUCCES)
                .lue(false)
                .dateNotification(LocalDateTime.now())
                .referenceId(String.valueOf(collecte.getId()))
                .lien("/api/v1/agent/collectes/" + collecte.getId())
                .build();

        log.info("Envoi notification de validation pour la collecte ID {} à l'agent ID {}",
                collecte.getId(), collecte.getAgentCollecte().getId());
        return notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public Notification notifierRejetCollecte(Collecte collecte, String motifRejet) {
        if (collecte == null || collecte.getAgentCollecte() == null) return null;

        String motif = (motifRejet != null && !motifRejet.isBlank()) ? motifRejet : "Non spécifié";
        Notification notification = Notification.builder()
                .utilisateur(collecte.getAgentCollecte())
                .titre("Collecte #" + collecte.getId() + " rejetée - Correction requise")
                .message("Votre collecte #" + collecte.getId() + " a été rejetée lors de l'examen. Motif : " + motif)
                .type(TypeNotification.COLLECTE_REJETEE)
                .niveau(NiveauNotification.ATTENTION)
                .lue(false)
                .dateNotification(LocalDateTime.now())
                .referenceId(String.valueOf(collecte.getId()))
                .lien("/api/v1/agent/suivi/" + collecte.getId() + "/motif-rejet")
                .build();

        log.info("Envoi notification de rejet pour la collecte ID {} à l'agent ID {}",
                collecte.getId(), collecte.getAgentCollecte().getId());
        return notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public Notification notifierDemandeCorrection(Collecte collecte, String instructions) {
        if (collecte == null || collecte.getAgentCollecte() == null) return null;

        Notification notification = Notification.builder()
                .utilisateur(collecte.getAgentCollecte())
                .titre("Demande de correction sur la collecte #" + collecte.getId())
                .message("Des corrections sont demandées sur votre collecte #" + collecte.getId() + " : " + instructions)
                .type(TypeNotification.COLLECTE_CORRECTION)
                .niveau(NiveauNotification.ATTENTION)
                .lue(false)
                .dateNotification(LocalDateTime.now())
                .referenceId(String.valueOf(collecte.getId()))
                .lien("/api/v1/agent/suivi/" + collecte.getId() + "/motif-rejet")
                .build();

        log.info("Envoi notification de demande de correction pour la collecte ID {} à l'agent ID {}",
                collecte.getId(), collecte.getAgentCollecte().getId());
        return notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public Notification notifierChangementStatut(Collecte collecte, StatutCollecte ancienStatut, StatutCollecte nouveauStatut) {
        if (collecte == null || collecte.getAgentCollecte() == null) return null;

        Notification notification = Notification.builder()
                .utilisateur(collecte.getAgentCollecte())
                .titre("Statut mis à jour pour la collecte #" + collecte.getId())
                .message("La collecte #" + collecte.getId() + " est passée de " + ancienStatut + " à " + nouveauStatut + ".")
                .type(TypeNotification.COLLECTE_STATUT)
                .niveau(NiveauNotification.INFO)
                .lue(false)
                .dateNotification(LocalDateTime.now())
                .referenceId(String.valueOf(collecte.getId()))
                .lien("/api/v1/agent/collectes/" + collecte.getId())
                .build();

        log.info("Envoi notification de changement de statut pour la collecte ID {} à l'agent ID {}",
                collecte.getId(), collecte.getAgentCollecte().getId());
        return notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public Notification notifierMessageAdmin(AgentCollecte agent, String titre, String message, NiveauNotification niveau) {
        if (agent == null) return null;

        Notification notification = Notification.builder()
                .utilisateur(agent)
                .titre(titre != null && !titre.isBlank() ? titre : "Communication de l'administrateur")
                .message(message)
                .type(TypeNotification.INFO_IMPORTANTE)
                .niveau(niveau != null ? niveau : NiveauNotification.INFO)
                .lue(false)
                .dateNotification(LocalDateTime.now())
                .referenceId(null)
                .lien(null)
                .build();

        log.info("Envoi notification administrative à l'agent ID {}", agent.getId());
        return notificationRepository.save(notification);
    }
}
