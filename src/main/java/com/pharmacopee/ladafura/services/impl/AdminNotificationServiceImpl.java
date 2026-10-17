package com.pharmacopee.ladafura.services.impl;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pharmacopee.ladafura.Models.AgentCollecte;
import com.pharmacopee.ladafura.Models.Collecte;
import com.pharmacopee.ladafura.Models.Notification;
import com.pharmacopee.ladafura.Models.Pharmacopee;
import com.pharmacopee.ladafura.Models.Utilisateur;
import com.pharmacopee.ladafura.dto.admin.notification.AdminNotificationResponse;
import com.pharmacopee.ladafura.dto.admin.notification.AdminNotificationStatsResponse;
import com.pharmacopee.ladafura.enums.NiveauNotification;
import com.pharmacopee.ladafura.enums.Role;
import com.pharmacopee.ladafura.enums.TypeNotification;
import com.pharmacopee.ladafura.exceptions.ResourceNotFoundException;
import com.pharmacopee.ladafura.exceptions.UnauthorizedException;
import com.pharmacopee.ladafura.mappers.AdminNotificationMapper;
import com.pharmacopee.ladafura.repository.NotificationRepository;
import com.pharmacopee.ladafura.repository.UtilisateurRepository;
import com.pharmacopee.ladafura.services.interfaces.IAdminNotificationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AdminNotificationServiceImpl implements IAdminNotificationService {

    private final NotificationRepository notificationRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final AdminNotificationMapper mapper;

    private Utilisateur getCurrentAdminUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new UnauthorizedException("Authentification requise.");
        }
        String identifier = authentication.getName();
        return utilisateurRepository.findByEmail(identifier)
                .or(() -> utilisateurRepository.findByFirebaseUid(identifier))
                .orElseGet(() -> utilisateurRepository.findByRole(Role.ADMINISTRATEUR)
                        .stream().findFirst()
                        .orElseThrow(() -> new ResourceNotFoundException("Administrateur introuvable")));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AdminNotificationResponse> getAdminNotifications(Boolean lue, TypeNotification type, Pageable pageable) {
        Utilisateur admin = getCurrentAdminUser();
        Page<Notification> page;

        if (lue != null && type != null) {
            page = notificationRepository.findByUtilisateurIdAndLueAndType(admin.getId(), lue, type, pageable);
        } else if (lue != null) {
            page = notificationRepository.findByUtilisateurIdAndLue(admin.getId(), lue, pageable);
        } else if (type != null) {
            page = notificationRepository.findByUtilisateurIdAndType(admin.getId(), type, pageable);
        } else {
            page = notificationRepository.findByUtilisateurId(admin.getId(), pageable);
        }

        return page.map(mapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminNotificationStatsResponse getStats() {
        Utilisateur admin = getCurrentAdminUser();
        long total = notificationRepository.countByUtilisateurId(admin.getId());
        long nonLues = notificationRepository.countByUtilisateurIdAndLueFalse(admin.getId());
        long lues = notificationRepository.countByUtilisateurIdAndLueTrue(admin.getId());

        Map<String, Long> repartition = new HashMap<>();
        for (TypeNotification t : TypeNotification.values()) {
            long count = notificationRepository.countByUtilisateurIdAndType(admin.getId(), t);
            if (count > 0) {
                repartition.put(t.name(), count);
            }
        }

        return AdminNotificationStatsResponse.builder()
                .total(total)
                .nonLues(nonLues)
                .lues(lues)
                .repartitionParType(repartition)
                .build();
    }

    @Override
    public AdminNotificationResponse markAsRead(Long id) {
        Utilisateur admin = getCurrentAdminUser();
        Notification notification = notificationRepository.findByIdAndUtilisateurId(id, admin.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Notification", "id", id));

        notification.setLue(true);
        notification.setDateLecture(LocalDateTime.now());
        Notification saved = notificationRepository.save(notification);

        return mapper.toDto(saved);
    }

    @Override
    public void markAllAsRead() {
        Utilisateur admin = getCurrentAdminUser();
        notificationRepository.markAllAsReadByUtilisateurId(admin.getId());
    }

    @Override
    public void deleteNotification(Long id) {
        Utilisateur admin = getCurrentAdminUser();
        Notification notification = notificationRepository.findByIdAndUtilisateurId(id, admin.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Notification", "id", id));
        notificationRepository.delete(notification);
    }

    @Override
    public void notifierCollecteSoumise(Collecte collecte, boolean estResoumission) {
        if (collecte == null || collecte.getId() == null) return;
        List<Utilisateur> admins = utilisateurRepository.findByRole(Role.ADMINISTRATEUR);
        if (admins.isEmpty()) return;

        AgentCollecte agent = collecte.getAgentCollecte();
        String agentNom = (agent != null)
                ? ((agent.getPrenom() != null ? agent.getPrenom() + " " : "") + (agent.getNom() != null ? agent.getNom() : "")).trim()
                : "un agent de collecte";
        String refId = String.valueOf(collecte.getId());
        String titre = estResoumission
                ? "Collecte #" + collecte.getId() + " corrigée et soumise à nouveau"
                : "Nouvelle collecte #" + collecte.getId() + " soumise pour validation";
        String message = estResoumission
                ? "L'agent " + agentNom + " a corrigé et soumis à nouveau la fiche de collecte #" + collecte.getId() + " pour examen administratif."
                : "L'agent " + agentNom + " a soumis une nouvelle fiche de collecte #" + collecte.getId() + " en attente de validation.";

        try {
            for (Utilisateur admin : admins) {
                if (notificationRepository.existsByUtilisateurIdAndTypeAndReferenceIdAndDateNotificationAfter(
                        admin.getId(), TypeNotification.COLLECTE_SOUMISE, refId, LocalDateTime.now().minusSeconds(15))) {
                    continue;
                }
                Notification n = Notification.builder()
                        .utilisateur(admin)
                        .titre(titre)
                        .message(message)
                        .type(TypeNotification.COLLECTE_SOUMISE)
                        .niveau(NiveauNotification.INFO)
                        .lue(false)
                        .dateNotification(LocalDateTime.now())
                        .typeEntiteLiee("COLLECTE")
                        .referenceId(refId)
                        .lien("/admin/collectes/" + collecte.getId())
                        .build();
                notificationRepository.save(n);
            }
            log.info("Notification administrative émise pour la collecte soumise ID {} (resoumission={})", collecte.getId(), estResoumission);
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de la notification administrative pour la collecte ID {}: {}", collecte.getId(), e.getMessage());
        }
    }

    @Override
    public void notifierDemandeReferencementPharmacopee(Pharmacopee pharmacopee) {
        if (pharmacopee == null || pharmacopee.getId() == null) return;
        List<Utilisateur> admins = utilisateurRepository.findByRole(Role.ADMINISTRATEUR);
        if (admins.isEmpty()) return;

        String refId = String.valueOf(pharmacopee.getId());
        String titre = "Nouvelle demande d'agrément : " + pharmacopee.getNom();
        String message = "L'établissement « " + pharmacopee.getNom() + " » a soumis une demande de référencement en attente de validation.";

        for (Utilisateur admin : admins) {
            if (notificationRepository.existsByUtilisateurIdAndTypeAndReferenceIdAndDateNotificationAfter(
                    admin.getId(), TypeNotification.REFERENCEMENT, refId, LocalDateTime.now().minusSeconds(15))) {
                continue;
            }
            Notification n = Notification.builder()
                    .utilisateur(admin)
                    .titre(titre)
                    .message(message)
                    .type(TypeNotification.REFERENCEMENT)
                    .niveau(NiveauNotification.INFO)
                    .lue(false)
                    .dateNotification(LocalDateTime.now())
                    .typeEntiteLiee("PHARMACOPEE")
                    .referenceId(refId)
                    .lien("/admin/pharmacopees/" + pharmacopee.getId())
                    .build();
            notificationRepository.save(n);
        }
        log.info("Notification administrative émise pour la demande de référencement pharmacopée ID {}", pharmacopee.getId());
    }
}

