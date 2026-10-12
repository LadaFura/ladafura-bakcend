package com.pharmacopee.ladafura.dto.admin.notification;

import java.util.Map;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Synthèse et statistiques des notifications pour l'administrateur")
public class AdminNotificationStatsResponse {

    @Schema(description = "Nombre total de notifications reçues", example = "25")
    private long total;

    @Schema(description = "Nombre de notifications non lues", example = "5")
    private long nonLues;

    @Schema(description = "Nombre de notifications déjà lues", example = "20")
    private long lues;

    @Schema(description = "Répartition par type", example = "{\"AGREMENT_VALIDATION\": 3, \"ALERTE_SYSTEME\": 2}")
    private Map<String, Long> repartitionParType;
}

