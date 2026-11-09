package com.pharmacopee.ladafura.dto.pharmacopee.vente;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Ligne d'une vente ; le prix vient de la fiche de stock")
public class LigneVenteRequest {
    @NotNull(message = "La fiche de stock est obligatoire")
    private Long disponibiliteProduitId;
    @NotNull(message = "La quantité est obligatoire")
    @Min(value = 1, message = "La quantité doit être positive")
    private Integer quantite;
    @DecimalMin(value = "0", message = "La remise ne peut pas être négative")
    @Schema(description = "Exige la permission VENTES_REMISE")
    private BigDecimal remise;
}
