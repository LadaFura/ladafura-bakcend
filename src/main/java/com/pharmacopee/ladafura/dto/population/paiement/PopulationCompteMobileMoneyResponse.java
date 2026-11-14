package com.pharmacopee.ladafura.dto.population.paiement;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Compte Mobile Money d'une pharmacopée, sur lequel le client effectue son paiement")
public class PopulationCompteMobileMoneyResponse {

    @Schema(description = "Identifiant du compte, à renvoyer avec la déclaration", example = "12")
    private Long id;

    @Schema(description = "Nom du compte", example = "Orange Money")
    private String nom;

    @Schema(description = "Numéro à payer", example = "+223 70 11 22 33")
    private String numero;
}
