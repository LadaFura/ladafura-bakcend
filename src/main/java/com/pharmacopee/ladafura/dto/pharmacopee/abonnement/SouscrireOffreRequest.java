package com.pharmacopee.ladafura.dto.pharmacopee.abonnement;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Souscription à une offre du catalogue")
public class SouscrireOffreRequest {
    @NotNull(message = "L'offre est obligatoire")
    private Long offreId;
}
