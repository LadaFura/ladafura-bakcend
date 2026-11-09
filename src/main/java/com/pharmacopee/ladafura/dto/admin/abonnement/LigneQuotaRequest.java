package com.pharmacopee.ladafura.dto.admin.abonnement;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Valeur d'une limite")
public class LigneQuotaRequest {
    @NotNull(message = "La limite est obligatoire")
    private Long limiteId;
    @Min(value = 0, message = "La valeur ne peut pas être négative")
    private Integer valeurMaximale;
    @NotNull(message = "Préciser si la limite est illimitée")
    private Boolean illimite;
}
