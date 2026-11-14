package com.pharmacopee.ladafura.dto.pharmacopee.caisse;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Clôture de la session ouverte")
public class CloturerSessionRequest {
    @NotNull(message = "Le montant compté est obligatoire")
    @DecimalMin(value = "0", message = "Le montant ne peut pas être négatif")
    private BigDecimal montantConstate;
    @Schema(description = "Obligatoire si un écart est constaté")
    private String commentaire;
}
