package com.pharmacopee.ladafura.dto.pharmacopee.vente;

import java.util.ArrayList;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Paiements reçus à la validation ; un reste à payer exige un client")
public class ValiderVenteRequest {
    @Valid
    @Builder.Default
    private List<PaiementVenteRequest> paiements = new ArrayList<>();
}
