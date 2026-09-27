package com.pharmacopee.ladafura.dto.pharmacopee.paiement;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Confirmation d'encaissement en espèces (Cash) au comptoir ou lors de la livraison")
public class EncaisserCashRequest {

    @Schema(description = "Note ou justificatif de remise des espèces (optionnel)", example = "Espèces encaissées au guichet lors du retrait de la commande")
    private String note;
}
