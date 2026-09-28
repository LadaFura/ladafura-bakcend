package com.pharmacopee.ladafura.dto.agent.suivi;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Statistiques de suivi de l'ensemble des collectes d'un agent de collecte")
public class AgentSuiviStatsResponse {

    @Schema(description = "Nombre total de collectes créées par l'agent", example = "25")
    private long total;

    @Schema(description = "Nombre de collectes au statut BROUILLON", example = "6")
    private long brouillons;

    @Schema(description = "Nombre de collectes au statut SOUMISE (en attente d'attribution)", example = "4")
    private long soumises;

    @Schema(description = "Nombre de collectes au statut EN_EXAMEN (en cours d'examen)", example = "3")
    private long enExamen;

    @Schema(description = "Nombre de collectes au statut VALIDEE", example = "10")
    private long validees;

    @Schema(description = "Nombre de collectes au statut REJETEE (nécessitant des corrections)", example = "2")
    private long rejetees;

    @Schema(description = "Nombre de collectes nécessitant une action corrective de l'agent", example = "2")
    private long necessitantCorrection;

    @Schema(description = "Taux de validation des collectes instruites (en %)", example = "83.3")
    private double tauxValidation;
}
