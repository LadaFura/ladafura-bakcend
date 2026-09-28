package com.pharmacopee.ladafura.dto.population.recherche;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Élément résultat de recherche pour une maladie ou pathologie")
public class PopulationMaladieSearchItem {

    @Schema(description = "Identifiant de la maladie", example = "3")
    private Long id;

    @Schema(description = "Nom de la maladie", example = "Paludisme (Fièvre jaune / Djoly)")
    private String nom;

    @Schema(description = "Description ou symptômes connus", example = "État fébrile avec maux de tête et courbatures...")
    private String description;

    @Schema(description = "Nombre de plantes médicinales documentées pour cette pathologie", example = "4")
    private int nombrePlantesAssociees;
}
