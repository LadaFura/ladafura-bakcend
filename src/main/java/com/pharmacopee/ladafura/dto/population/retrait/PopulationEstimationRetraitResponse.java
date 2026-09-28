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
@Schema(description = "Résultat de la vérification et de l'estimation du mode de retrait")
public class PopulationEstimationRetraitResponse {

    @Schema(description = "Identifiant de la pharmacopée", example = "1")
    private Long pharmacopeeId;

    @Schema(description = "Nom de la pharmacopée", example = "Pharmacie Mandé")
    private String nomPharmacopee;

    @Schema(description = "Identifiant unique du mode de retrait configuré (null si indisponible)", example = "10")
    private Long modeRetraitId;

    @Schema(description = "Type de mode de mise à disposition", example = "LIVRAISON")
    private TypeModeRetrait type;

    @Schema(description = "Libellé convivial", example = "Livraison à domicile")
    private String libelle;

    @Schema(description = "Indique si ce mode est actif et éligible pour cette pharmacopée", example = "true")
    private Boolean eligible;

    @Schema(description = "Montant des frais calculés en FCFA (0 pour le Pickup)", example = "1500.0")
    private Double frais;

    @Schema(description = "Indique si ce mode est gratuit", example = "false")
    private Boolean gratuit;

    @Schema(description = "Adresse physique pour le retrait au comptoir (si Pickup)", example = "Siby Centre, Route Nationale 5")
    private String adresseRetrait;

    @Schema(description = "Message explicatif ou consignes d'acheminement", example = "Livraison à domicile disponible. Vos produits seront expédiés à l'adresse indiquée lors de la commande.")
    private String message;
}
