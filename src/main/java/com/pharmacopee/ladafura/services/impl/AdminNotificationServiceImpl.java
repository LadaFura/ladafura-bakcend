package com.pharmacopee.ladafura.services.impl;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pharmacopee.ladafura.Models.Notification;
import com.pharmacopee.ladafura.Models.Utilisateur;
import com.pharmacopee.ladafura.dto.admin.notification.AdminNotificationResponse;
import com.pharmacopee.ladafura.dto.admin.notification.AdminNotificationStatsResponse;
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
}

