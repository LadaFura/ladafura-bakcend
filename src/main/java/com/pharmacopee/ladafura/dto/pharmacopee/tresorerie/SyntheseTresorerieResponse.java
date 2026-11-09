package com.pharmacopee.ladafura.dto.pharmacopee.tresorerie;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Synthèse par période. Ces chiffres ne s'additionnent pas entre eux.")
public class SyntheseTresorerieResponse {
    private LocalDate debut;
    private LocalDate fin;
    @Schema(description = "Ventes au comptoir validées")
    private BigDecimal chiffreAffairesComptoir;
    @Schema(description = "Commandes en ligne livrées ou retirées")
    private BigDecimal chiffreAffairesCommandes;
    private List<FluxCompteResponse> comptes;
    private List<DepenseCategorieResponse> depenses;
}
