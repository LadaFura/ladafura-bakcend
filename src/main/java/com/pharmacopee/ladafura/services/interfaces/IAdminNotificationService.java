package com.pharmacopee.ladafura.services.interfaces;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.pharmacopee.ladafura.Models.Collecte;
import com.pharmacopee.ladafura.Models.Pharmacopee;
import com.pharmacopee.ladafura.dto.admin.notification.AdminNotificationResponse;
import com.pharmacopee.ladafura.dto.admin.notification.AdminNotificationStatsResponse;
import com.pharmacopee.ladafura.enums.TypeNotification;

public interface IAdminNotificationService {

    Page<AdminNotificationResponse> getAdminNotifications(Boolean lue, TypeNotification type, Pageable pageable);

    AdminNotificationStatsResponse getStats();

    AdminNotificationResponse markAsRead(Long id);

    void markAllAsRead();

    void deleteNotification(Long id);

    void notifierCollecteSoumise(Collecte collecte, boolean estResoumission);

    void notifierDemandeReferencementPharmacopee(Pharmacopee pharmacopee);
}

