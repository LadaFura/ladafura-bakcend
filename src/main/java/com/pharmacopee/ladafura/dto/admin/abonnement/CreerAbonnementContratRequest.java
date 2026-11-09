package com.pharmacopee.ladafura.dto.admin.abonnement;

import java.math.BigDecimal;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Conditions négociées d'un contrat personnalisé")
public class CreerAbonnementContratRequest {
    @NotNull(message = "Le prix est obligatoire")
    @DecimalMin(value = "0.01", message = "Le prix doit être positif")
    private BigDecimal prix;
    @NotNull(message = "La durée est obligatoire")
    @Min(value = 1, message = "La durée doit être positive")
    @Schema(description = "Peut dépasser 12 mois", example = "24")
    private Integer dureeMois;
    @NotNull(message = "Les jours de grâce sont obligatoires")
    @Min(value = 0, message = "Les jours de grâce ne peuvent pas être négatifs")
    private Integer joursGrace;
    @NotEmpty(message = "Au moins un quota est obligatoire")
    @Valid
    private List<LigneQuotaRequest> quotas;
}
