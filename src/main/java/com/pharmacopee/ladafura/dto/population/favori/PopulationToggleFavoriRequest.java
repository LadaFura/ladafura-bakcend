package com.pharmacopee.ladafura.dto.population.favori;

import com.pharmacopee.ladafura.enums.TypeFavori;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
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
@Schema(description = "Requête d'ajout/retrait dynamique (toggle) d'un favori")
public class PopulationToggleFavoriRequest {

    @NotNull(message = "Le type de favori (PLANTE, PRODUIT, PHARMACOPEE) est obligatoire")
    @Schema(description = "Type d'élément à basculer en favori", example = "PRODUIT")
    private TypeFavori type;

    @NotNull(message = "L'ID de l'élément cible est obligatoire")
    @Schema(description = "Identifiant de la plante, du produit ou de la pharmacopée", example = "5")
    private Long cibleId;
}
