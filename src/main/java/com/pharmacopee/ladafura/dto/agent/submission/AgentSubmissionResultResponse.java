package com.pharmacopee.ladafura.dto.agent.submission;

import java.time.LocalDateTime;

import com.pharmacopee.ladafura.enums.StatutCollecte;
import com.pharmacopee.ladafura.enums.StatutValidation;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Confirmation de soumission et passage au statut d'examen")
public class AgentSubmissionResultResponse {

    @Schema(description = "Identifiant de la collecte soumise", example = "12")
    private Long collecteId;

    @Schema(description = "Nouveau statut de la collecte", example = "SOUMISE")
    private StatutCollecte statut;

    @Schema(description = "Date et heure de soumission effective", example = "2026-09-28T10:45:00")
    private LocalDateTime dateSoumission;

    @Schema(description = "Statut de modération des connaissances associées", example = "EN_ATTENTE")
    private StatutValidation statutConnaissances;

    @Schema(description = "Indique que la collecte est désormais verrouillée contre toute modification", example = "true")
    private boolean verrouillee;

    @Schema(description = "Message explicatif du workflow", example = "Collecte soumise avec succès pour validation. Les données sont désormais verrouillées en attente d'examen administratif.")
    private String message;
}
