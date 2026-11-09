package com.pharmacopee.ladafura.dto.pharmacopee.abonnement;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Quota de l'abonnement et sa consommation actuelle")
public class LimiteConsommationResponse {
    private String codeLimite;
    private Integer valeurMaximale;
    private Boolean illimite;
    @Schema(description = "Consommation actuelle ; absente hors de l'abonnement en vigueur")
    private Long consommation;
}
