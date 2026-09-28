package com.pharmacopee.ladafura.dto.population.notification;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Statistiques et compteurs du centre de notifications de la Population")
public class PopulationNotificationStatsResponse {

    @Schema(description = "Nombre total de notifications reçues", example = "12")
    private long total;

    @Schema(description = "Nombre de notifications non lues", example = "3")
    private long nonLues;

    @Schema(description = "Nombre de notifications déjà lues", example = "9")
    private long lues;

    @Schema(description = "Nombre de notifications relatives aux commandes", example = "7")
    private long commandes;

    @Schema(description = "Nombre de notifications relatives aux avis", example = "2")
    private long avis;

    @Schema(description = "Nombre de notifications d'alertes ou informations sanitaires", example = "3")
    private long alertes;
}
