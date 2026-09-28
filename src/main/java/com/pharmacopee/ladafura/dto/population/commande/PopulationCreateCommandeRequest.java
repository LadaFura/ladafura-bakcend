package com.pharmacopee.ladafura.dto.population.commande;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Demande de validation et passage de commande par l'utilisateur")
public class PopulationCreateCommandeRequest {

    @NotNull(message = "L'identifiant de la pharmacopée est obligatoire")
    @Schema(description = "Identifiant de la pharmacopée choisie", example = "1")
    private Long pharmacopeeId;

    @NotNull(message = "Le mode de retrait est obligatoire")
    @Schema(description = "Identifiant du mode de retrait retenu", example = "2")
    private Long modeRetraitId;

    @Schema(description = "Adresse complète de livraison (obligatoire si mode LIVRAISON)", example = "Badalabougou, Rue 24, Porte 12, Bamako")
    private String adresseLivraison;

    @Schema(description = "Indications complémentaires pour la livraison ou le retrait", example = "Sonner au portail bleu ou appeler avant d'arriver")
    private String notes;
}
