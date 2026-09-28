package com.pharmacopee.ladafura.dto.agent.notification;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Compteurs statistiques du centre de notifications de l'Agent de Collecte")
public class AgentNotificationStatsResponse {

    @Schema(description = "Nombre total de notifications reçues", example = "18")
    private long total;

    @Schema(description = "Nombre de notifications non lues", example = "4")
    private long nonLues;

    @Schema(description = "Nombre de notifications déjà lues", example = "14")
    private long lues;

    @Schema(description = "Nombre de notifications de type validation de collecte", example = "8")
    private long validations;

    @Schema(description = "Nombre de notifications de type rejet de collecte", example = "2")
    private long rejets;

    @Schema(description = "Nombre de notifications de type demande de correction", example = "3")
    private long corrections;

    @Schema(description = "Nombre de notifications de type changement de statut", example = "4")
    private long changementsStatut;

    @Schema(description = "Nombre de communications directes ou avis de l'administrateur", example = "1")
    private long infosImportantes;
}
