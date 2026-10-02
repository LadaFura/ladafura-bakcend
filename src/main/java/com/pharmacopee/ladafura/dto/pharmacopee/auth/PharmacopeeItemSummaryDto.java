package com.pharmacopee.ladafura.dto.pharmacopee.auth;

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
@Schema(description = "Résumé d'une structure de pharmacopée affiliée")
public class PharmacopeeItemSummaryDto {

    @Schema(description = "Identifiant de la pharmacopée", example = "1")
    private Long id;

    @Schema(description = "Nom de la pharmacopée", example = "Pharmacie Danaya")
    private String nom;

    @Schema(description = "Téléphone de contact", example = "+223 76 00 11 22")
    private String telephone;

    @Schema(description = "Statut de référencement", example = "VALIDEE")
    private StatutPharmacopee statut;

    @Schema(description = "Indique si la structure est validée", example = "true")
    private boolean validee;

    private String localite;
    private String commune;
    private String cercle;
    private String region;
}
