package com.pharmacopee.ladafura.dto.agent.suivi;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.pharmacopee.ladafura.enums.StatutCollecte;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Détail explicatif du rejet d'une collecte et guide pratique de correction pour l'agent")
public class AgentCollecteRejetDetailResponse {

    @Schema(description = "Identifiant unique de la collecte rejetée", example = "12")
    private Long collecteId;

    @Schema(description = "Date de réalisation de la collecte sur le terrain", example = "2026-09-28T10:00:00")
    private LocalDateTime dateCollecte;

    @Schema(description = "Date de la dernière soumission", example = "2026-09-28T14:30:00")
    private LocalDateTime dateSoumission;

    @Schema(description = "Statut actuel de la collecte (REJETEE)", example = "REJETEE")
    private StatutCollecte statut;

    @Schema(description = "Motif officiel du rejet communiqué par l'administrateur ou le modérateur", example = "Photos floues ne permettant pas d'identifier avec certitude la plante. Merci de joindre une photo nette des feuilles.")
    private String motifRejet;

    @Schema(description = "Nom scientifique de la plante concernée", example = "Combretum micranthum")
    private String planteNomScientifique;

    @Schema(description = "Localité ou village de la collecte", example = "Finkolo Centre")
    private String localite;

    @Schema(description = "Région administrative", example = "Sikasso")
    private String region;

    @Schema(description = "Nom complet de la source rencontrée sur le terrain", example = "Amadou Diarra")
    private String sourceNomComplet;

    @Schema(description = "Confirme que la collecte est à nouveau déverrouillée et modifiable par l'agent", example = "true")
    private boolean modifiable;

    @Builder.Default
    @Schema(description = "Guide et étapes recommandées pour corriger la fiche et la re-soumettre avec succès")
    private List<String> guideCorrection = new ArrayList<>();
}
