package com.pharmacopee.ladafura.dto.population.plante;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Savoir ou usage traditionnel validé associé à une plante (Ne constitue pas une preuve scientifique)")
public class PopulationConnaissanceTraditionnelleDto {

    @Schema(description = "Identifiant de la connaissance traditionnelle", example = "10")
    private Long id;

    @Schema(description = "Usage traditionnel rapporté", example = "Soulagement des états fébriles et digestifs")
    private String usageRapporte;

    @Schema(description = "Partie de la plante utilisée", example = "Feuilles")
    private String partieUtilisee;

    @Schema(description = "Mode de préparation traditionnel", example = "Décoction de 15 minutes des feuilles séchées")
    private String preparation;

    @Schema(description = "Précautions d'emploi traditionnelles rapportées", example = "Éviter les prises prolongées chez la femme enceinte sans avis")
    private String precaution;

    @Schema(description = "Description complémentaire du savoir traditionnel", example = "Traditionnellement consommé le matin en infusion")
    private String description;

    @Schema(description = "Nom du tradithérapeute ou informateur source", example = "Mamadou Traoré")
    private String sourceNom;

    @Schema(description = "Spécialité de la source traditionnelle", example = "Naturopathe / Tradithérapeute herboriste")
    private String sourceSpecialite;

    @Schema(description = "Localité ou adresse de la source", example = "Kati, Région de Koulikoro")
    private String sourceLocalite;
}
