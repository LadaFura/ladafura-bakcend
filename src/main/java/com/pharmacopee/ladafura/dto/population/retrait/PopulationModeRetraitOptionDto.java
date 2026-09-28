package com.pharmacopee.ladafura.dto.population.retrait;

import com.pharmacopee.ladafura.enums.TypeModeRetrait;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Option de mise à disposition d'une commande (Livraison ou Pickup)")
public class PopulationModeRetraitOptionDto {

    @Schema(description = "Identifiant unique de configuration du mode", example = "10")
    private Long id;

    @Schema(description = "Type de mode de mise à disposition : LIVRAISON ou PICKUP", example = "LIVRAISON")
    private TypeModeRetrait type;

    @Schema(description = "Libellé convivial du mode", example = "Livraison à domicile")
    private String libelle;

    @Schema(description = "Indique si ce mode est actuellement actif et sélectionnable", example = "true")
    private Boolean actif;

    @Schema(description = "Frais applicables en FCFA (strictement 0 pour le Pickup)", example = "1500.0")
    private Double frais;

    @Schema(description = "Indique si l'option est totalement gratuite", example = "false")
    private Boolean gratuit;

    @Schema(description = "Description ou consigne associée", example = "Livraison directe à votre domicile ou bureau dans la zone desservie.")
    private String description;

    @Schema(description = "Adresse de retrait (renseignée si type PICKUP)", example = "Siby Centre, Route Nationale 5")
    private String adresseRetrait;
}
