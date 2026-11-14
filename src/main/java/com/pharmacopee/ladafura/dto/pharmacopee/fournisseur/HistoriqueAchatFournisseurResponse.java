package com.pharmacopee.ladafura.dto.pharmacopee.fournisseur;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Ligne d'un achat validé auprès d'un fournisseur")
public class HistoriqueAchatFournisseurResponse {
    private Long achatId;
    private String numeroAchat;
    private LocalDateTime dateAchat;
    private Long produitId;
    private String produitNom;
    private Integer quantite;
    private BigDecimal prixUnitaire;
    private BigDecimal sousTotal;
}
