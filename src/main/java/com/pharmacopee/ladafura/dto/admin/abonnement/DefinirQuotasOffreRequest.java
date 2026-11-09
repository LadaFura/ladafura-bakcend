package com.pharmacopee.ladafura.dto.admin.abonnement;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Liste complète des quotas d'une offre")
public class DefinirQuotasOffreRequest {
    @NotEmpty(message = "Au moins un quota est obligatoire")
    @Valid
    private List<LigneQuotaRequest> quotas;
}
