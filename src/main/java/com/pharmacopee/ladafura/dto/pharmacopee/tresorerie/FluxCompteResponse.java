package com.pharmacopee.ladafura.dto.pharmacopee.tresorerie;

import java.math.BigDecimal;

import com.pharmacopee.ladafura.enums.TypeCaisse;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Mouvements d'un compte sur la période")
public class FluxCompteResponse {
    private Long caisseId;
    private String caisseNom;
    private TypeCaisse type;
    private BigDecimal entrees;
    private BigDecimal sorties;
}
