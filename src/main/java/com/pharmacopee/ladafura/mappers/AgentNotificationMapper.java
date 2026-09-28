package com.pharmacopee.ladafura.mappers;

import org.springframework.stereotype.Component;

import com.pharmacopee.ladafura.Models.Notification;
import com.pharmacopee.ladafura.dto.agent.notification.AgentNotificationResponse;

@Component
public class AgentNotificationMapper {

    public AgentNotificationResponse toDto(Notification notification) {
        if (notification == null) return null;

        return AgentNotificationResponse.builder()
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
}
