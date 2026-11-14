package com.pharmacopee.ladafura.dto.pharmacopee.membre;

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
@Schema(description = "Désignation du nouveau gérant, parmi les membres actifs")
public class TransfertProprieteRequest {

    @NotNull(message = "Le membre est obligatoire")
    private Long membreId;
}
