package com.pharmacopee.ladafura.mappers;

import org.springframework.stereotype.Component;

import com.pharmacopee.ladafura.Models.Notification;
import com.pharmacopee.ladafura.dto.population.notification.PopulationNotificationCountResponse;
import com.pharmacopee.ladafura.dto.population.notification.PopulationNotificationResponse;
import com.pharmacopee.ladafura.dto.population.notification.PopulationNotificationStatsResponse;

@Component
public class PopulationNotificationMapper {

    public PopulationNotificationResponse toDto(Notification notification) {
        if (notification == null) {
            return null;
        }

        return PopulationNotificationResponse.builder()
                .id(notification.getId())
                .titre(notification.getTitre())
                .message(notification.getMessage())
                .type(notification.getType())
                .niveau(notification.getNiveau())
                .lue(notification.getLue())
                .dateNotification(notification.getDateNotification())
                .dateLecture(notification.getDateLecture())
                .referenceId(notification.getReferenceId())
                .lien(notification.getLien())
                .build();
    }

    public PopulationNotificationStatsResponse toStatsResponse(
            long total, long nonLues, long lues, long commandes, long avis, long alertes) {
        return PopulationNotificationStatsResponse.builder()
                .total(total)
                .nonLues(nonLues)
                .lues(lues)
                .commandes(commandes)
                .avis(avis)
                .alertes(alertes)
                .build();
    }

    public PopulationNotificationCountResponse toCountResponse(long count) {
        return PopulationNotificationCountResponse.builder()
                .count(count)
                .build();
    }
}
