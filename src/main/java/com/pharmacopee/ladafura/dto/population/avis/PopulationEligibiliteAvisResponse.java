package com.pharmacopee.ladafura.dto.population.avis;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Vérification préalable de l'éligibilité pour déposer un avis vérifié")
public class PopulationEligibiliteAvisResponse {

    @Schema(description = "Identifiant de la pharmacopée", example = "1")
    private Long pharmacopeeId;

    @Schema(description = "Nom de la pharmacopée", example = "danaya")
    private String nomPharmacopee;

    @Schema(description = "Indique si le client a bien commandé et reçu ce produit", example = "true")
    private Boolean eligible;

    @Schema(description = "Indique si le client a déjà rédigé un avis pour ce produit", example = "false")
    private Boolean dejaEvalue;

    @Schema(description = "Identifiant de l'avis existant (si déjà évalué)", example = "12")
    private Long avisId;

    @Schema(description = "Consigne explicative", example = "Vous êtes éligible à déposer un avis vérifié pour ce produit.")
    private String message;
}
