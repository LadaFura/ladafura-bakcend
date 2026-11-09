package com.pharmacopee.ladafura.dto.pharmacopee.caisse;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Transfert d'argent entre deux caisses ou comptes")
public class TransfertCaisseRequest {
    @NotNull(message = "La caisse source est obligatoire")
    private Long caisseSourceId;
    @NotNull(message = "La caisse de destination est obligatoire")
    private Long caisseDestinationId;
    @NotNull(message = "Le montant est obligatoire")
    @DecimalMin(value = "0.01", message = "Le montant doit être positif")
    private BigDecimal montant;
    @NotBlank(message = "Le libellé est obligatoire")
    @Schema(example = "Dépôt en banque")
    private String libelle;
}
