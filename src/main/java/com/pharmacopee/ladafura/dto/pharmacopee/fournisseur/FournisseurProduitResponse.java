package com.pharmacopee.ladafura.dto.pharmacopee.fournisseur;

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
@Schema(description = "Produit proposé par un fournisseur")
public class FournisseurProduitResponse {
    private Long id;
    private Long produitId;
    private String produitNom;
    private String referenceFournisseur;
    private BigDecimal prixIndicatif;
    private Integer delaiApprovisionnement;
    private Boolean disponible;
    private String observation;
}
