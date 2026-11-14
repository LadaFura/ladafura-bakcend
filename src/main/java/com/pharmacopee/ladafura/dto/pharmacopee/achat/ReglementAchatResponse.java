package com.pharmacopee.ladafura.dto.pharmacopee.achat;

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
@Schema(description = "Règlement d'un achat")
public class ReglementAchatResponse {
    private Long id;
    private Long achatId;
    private BigDecimal montant;
    private LocalDateTime dateReglement;
    private MethodePaiement methode;
    private String reference;
    private String observation;
    private StatutEcriture statut;
    private LocalDateTime dateAnnulation;
    private String motifAnnulation;
}
