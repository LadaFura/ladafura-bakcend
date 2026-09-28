package com.pharmacopee.ladafura.dto.agent.vernaculaire;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Détail d'un nom vernaculaire rattaché à une plante")
public class AgentNomVernaculaireResponse {

    @Schema(description = "Identifiant unique du nom vernaculaire", example = "10")
    private Long id;

    @Schema(description = "Identifiant de la plante associée", example = "1")
    private Long planteId;

    @Schema(description = "Nom scientifique de la plante associée", example = "Combretum micranthum")
    private String nomScientifiquePlante;

    @Schema(description = "Nom vernaculaire dans la langue locale", example = "Kinkéliba")
    private String nom;

    @Schema(description = "Langue locale ou dialecte", example = "Bambara")
    private String langue;

    @Schema(description = "Pays d'usage", example = "Mali")
    private String pays;
}
