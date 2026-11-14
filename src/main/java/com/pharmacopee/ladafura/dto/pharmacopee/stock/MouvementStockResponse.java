package com.pharmacopee.ladafura.dto.pharmacopee.stock;

import java.time.LocalDateTime;

import com.pharmacopee.ladafura.enums.TypeMouvementStock;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Variation de stock, immuable")
public class MouvementStockResponse {

    @Schema(example = "120")
    private Long id;

    @Schema(description = "Date du mouvement")
    private LocalDateTime dateMouvement;

    @Schema(example = "SORTIE_VENTE")
    private TypeMouvementStock type;

    @Schema(example = "5")
    private Long produitId;

    @Schema(example = "Tisane de kinkéliba")
    private String produitNom;

    @Schema(description = "Quantité signée : positive pour une entrée, négative pour une sortie", example = "-2")
    private Integer quantite;

    @Schema(example = "20")
    private Integer stockAvant;

    @Schema(example = "18")
    private Integer stockApres;

    @Schema(example = "L2026-014")
    private String numeroLot;

    @Schema(example = "Commande CMD-1024")
    private String motif;

    @Schema(description = "Auteur ; vide pour une commande passée en ligne", example = "Aminata Diarra")
    private String auteur;
}
