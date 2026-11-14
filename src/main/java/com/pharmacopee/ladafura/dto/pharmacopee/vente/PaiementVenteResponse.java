package com.pharmacopee.ladafura.dto.pharmacopee.vente;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.pharmacopee.ladafura.enums.MethodePaiement;
import com.pharmacopee.ladafura.enums.StatutEcriture;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Paiement d'une vente")
public class PaiementVenteResponse {
    private Long id;
    private Long venteId;
    private String reference;
    private BigDecimal montant;
    private MethodePaiement methode;
    private String referenceTransaction;
    private LocalDateTime datePaiement;
    private String observation;
    private StatutEcriture statut;
    private LocalDateTime dateAnnulation;
    private String motifAnnulation;
}
