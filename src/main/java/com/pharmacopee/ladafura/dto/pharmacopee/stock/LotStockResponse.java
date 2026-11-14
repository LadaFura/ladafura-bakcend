package com.pharmacopee.ladafura.dto.pharmacopee.stock;

import java.time.LocalDate;
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
@Schema(description = "Lot encore en stock")
public class LotStockResponse {

    @Schema(example = "8")
    private Long id;

    @Schema(example = "5")
    private Long produitId;

    @Schema(example = "Sirop de moringa")
    private String produitNom;

    @Schema(example = "L2026-014")
    private String numeroLot;

    private LocalDate datePeremption;

    @Schema(example = "20")
    private Integer quantiteInitiale;

    @Schema(example = "12")
    private Integer quantiteRestante;

    private LocalDateTime dateReception;

    @Schema(description = "Lot périmé : jamais vendu")
    private boolean perime;

    @Schema(description = "Péremption dans les 30 prochains jours")
    private boolean prochePeremption;
}
