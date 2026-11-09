package com.pharmacopee.ladafura.dto.pharmacopee.abonnement;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.pharmacopee.ladafura.enums.StatutFacture;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Facture d'abonnement")
public class FactureAbonnementResponse {
    private Long id;
    private Long abonnementId;
    private String numeroFacture;
    private LocalDateTime dateEmission;
    private BigDecimal tauxTaxe;
    private BigDecimal montantHT;
    private BigDecimal montantTaxe;
    private BigDecimal montantTTC;
    private String devise;
    private StatutFacture statut;
}
