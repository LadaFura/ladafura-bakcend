package com.pharmacopee.ladafura.dto.population.favori;

import com.pharmacopee.ladafura.enums.TypeFavori;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Vérification du statut favori d'un élément")
public class PopulationFavoriCheckResponse {

    @Schema(description = "Type de l'élément vérifié", example = "PLANTE")
    private TypeFavori type;

    @Schema(description = "Identifiant de la cible", example = "12")
    private Long cibleId;

    @Schema(description = "Indique si l'élément figure dans les favoris du client", example = "true")
    private boolean favori;

    @Schema(description = "Identifiant de l'enregistrement favori (null si non présent)", example = "105")
    private Long favoriId;
}
