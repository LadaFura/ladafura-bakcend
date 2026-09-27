package com.pharmacopee.ladafura.dto.pharmacopee.referencement;

import com.pharmacopee.ladafura.enums.StatutPharmacopee;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "État actuel du référencement et agrément de la pharmacopée")
public class StatutReferencementResponse {

    @Schema(description = "Identifiant unique de la pharmacopée", example = "1")
    private Long pharmacopeeId;

    @Schema(description = "Nom officiel de l'établissement", example = "Pharmacie Traditionnelle Mandé")
    private String nomPharmacopee;

    @Schema(description = "Numéro de téléphone de contact", example = "+223 20 22 33 44")
    private String telephone;

    @Schema(description = "Statut administratif d'agrément (EN_ATTENTE, VALIDEE, SUSPENDUE, REJETEE)", example = "EN_ATTENTE")
    private StatutPharmacopee statut;

    @Schema(description = "Indique si la pharmacopée est validée et autorisée aux opérations commerciales", example = "false")
    private boolean validee;

    @Schema(description = "Message informatif sur les actions possibles ou la décision administrative", example = "Votre dossier est actuellement à l'étude par les administrateurs de LADAFURA.")
    private String message;
}
