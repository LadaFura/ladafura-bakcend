package com.pharmacopee.ladafura.dto.pharmacopee.caisse;

import java.math.BigDecimal;

import com.pharmacopee.ladafura.enums.NatureOperationCaisse;

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
@Schema(description = "Apport, retrait ou recette diverse")
public class OperationManuelleRequest {
    @NotNull(message = "La nature est obligatoire")
    @Schema(description = "APPORT, RETRAIT ou RECETTE_DIVERSE")
    private NatureOperationCaisse nature;
    @NotNull(message = "Le montant est obligatoire")
    @DecimalMin(value = "0.01", message = "Le montant doit être positif")
    private BigDecimal montant;
    @NotBlank(message = "Le libellé est obligatoire")
    private String libelle;
}
