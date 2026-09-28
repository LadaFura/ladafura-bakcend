package com.pharmacopee.ladafura.dto.pharmacopee.stock;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Définition du prix de vente pratiqué par l'officine")
public class UpdatePrixRequest {

    @Positive(message = "Le prix de vente doit être strictement positif s'il est spécifié")
    @Schema(description = "Nouveau prix de vente pratiqué par la pharmacopée en FCFA (passer null pour réinitialiser au prix national)", example = "2800.0")
    private Double prix;
}
