package com.pharmacopee.ladafura.dto.pharmacopee.achat;

import java.math.BigDecimal;
import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Ligne d'un achat")
public class LigneAchatResponse {
    private Long id;
    private Long produitId;
    private String produitNom;
    private Integer quantite;
    private BigDecimal prixUnitaire;
    private BigDecimal remise;
    private BigDecimal sousTotal;
    private String numeroLot;
    private LocalDate datePeremption;
}
