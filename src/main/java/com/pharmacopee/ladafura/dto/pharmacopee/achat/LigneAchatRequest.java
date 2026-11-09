package com.pharmacopee.ladafura.dto.pharmacopee.achat;

import java.math.BigDecimal;
import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Ligne d'un achat")
public class LigneAchatRequest {
    @NotNull(message = "Le produit est obligatoire")
    private Long produitId;
    @NotNull(message = "La quantité est obligatoire")
    @Min(value = 1, message = "La quantité doit être positive")
    private Integer quantite;
    @NotNull(message = "Le prix unitaire est obligatoire")
    @DecimalMin(value = "0", message = "Le prix ne peut pas être négatif")
    private BigDecimal prixUnitaire;
    @DecimalMin(value = "0", message = "La remise ne peut pas être négative")
    private BigDecimal remise;
    @Size(max = 50, message = "Le numéro de lot ne doit pas dépasser 50 caractères")
    private String numeroLot;
    private LocalDate datePeremption;
}
