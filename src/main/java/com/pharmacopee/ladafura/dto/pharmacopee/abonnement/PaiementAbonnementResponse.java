package com.pharmacopee.ladafura.dto.pharmacopee.abonnement;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.pharmacopee.ladafura.enums.ModePaiementAbonnement;
import com.pharmacopee.ladafura.enums.StatutPaiement;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Paiement d'un abonnement")
public class PaiementAbonnementResponse {
    private Long id;
    private Long abonnementId;
    private Long pharmacopeeId;
    private String pharmacopeeNom;
    private String reference;
    private BigDecimal montant;
    private String devise;
    private ModePaiementAbonnement modePaiement;
    private StatutPaiement statut;
    private LocalDateTime datePaiement;
    private String referenceTransaction;
    private String observation;
    private LocalDateTime dateValidation;
    private String motifRejet;
}
