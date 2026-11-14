package com.pharmacopee.ladafura.dto.pharmacopee.vente;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Vente en attente ; le client est facultatif")
public class EnregistrerVenteRequest {
    private Long clientId;
    @DecimalMin(value = "0", message = "La remise ne peut pas être négative")
    @Schema(description = "Remise globale ; exige la permission VENTES_REMISE")
    private BigDecimal montantRemise;
    private String observation;
    @Valid
    @Builder.Default
    private List<LigneVenteRequest> lignes = new ArrayList<>();
}
