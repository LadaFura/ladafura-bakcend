package com.pharmacopee.ladafura.dto.pharmacopee.achat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
@Schema(description = "Achat en brouillon ; le fournisseur est facultatif")
public class EnregistrerAchatRequest {
    @Schema(description = "Facultatif")
    private Long fournisseurId;
    private LocalDateTime dateAchat;
    @DecimalMin(value = "0", message = "La remise ne peut pas être négative")
    private BigDecimal montantRemise;
    @Schema(description = "Provenance libre, autres remarques", example = "Marché de Médine")
    private String observation;
    @Valid
    @Builder.Default
    private List<LigneAchatRequest> lignes = new ArrayList<>();
}
