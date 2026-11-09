package com.pharmacopee.ladafura.dto.pharmacopee.caisse;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Ouverture d'une session ; le fond reprend par défaut le montant compté à la dernière clôture")
public class OuvrirSessionRequest {
    @DecimalMin(value = "0", message = "Le fond ne peut pas être négatif")
    private BigDecimal fondOuverture;
    @Schema(description = "Obligatoire si le fond diffère du montant de la dernière clôture")
    private String commentaire;
}
