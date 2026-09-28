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
@Schema(description = "Résultat de l'opération de bascule (toggle) d'un favori")
public class PopulationToggleFavoriResponse {

    @Schema(description = "Type du favori concerné", example = "PRODUIT")
    private TypeFavori type;

    @Schema(description = "Identifiant de la cible", example = "5")
    private Long cibleId;

    @Schema(description = "Indique si l'élément est désormais en favori (true = ajouté, false = retiré)", example = "true")
    private boolean favori;

    @Schema(description = "ID du favori créé (null si retiré)", example = "42")
    private Long favoriId;

    @Schema(description = "Message informatif", example = "Produit ajouté aux favoris avec succès.")
    private String message;
}
