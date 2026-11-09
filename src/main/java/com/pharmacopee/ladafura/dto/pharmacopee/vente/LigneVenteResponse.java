package com.pharmacopee.ladafura.dto.pharmacopee.vente;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Ligne d'une vente")
public class LigneVenteResponse {
    private Long id;
    private Long disponibiliteProduitId;
    private String produitNom;
    private Integer quantite;
    private BigDecimal prixUnitaire;
    private BigDecimal remise;
    private BigDecimal sousTotal;
}
