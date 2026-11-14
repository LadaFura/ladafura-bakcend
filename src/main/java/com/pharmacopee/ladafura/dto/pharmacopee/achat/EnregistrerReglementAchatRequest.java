package com.pharmacopee.ladafura.dto.pharmacopee.achat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.pharmacopee.ladafura.enums.MethodePaiement;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Règlement d'un achat validé")
public class EnregistrerReglementAchatRequest {
    @NotNull(message = "Le montant est obligatoire")
    @DecimalMin(value = "0.01", message = "Le montant doit être positif")
    private BigDecimal montant;
    @NotNull(message = "Le mode de paiement est obligatoire")
    private MethodePaiement methode;
    @NotNull(message = "La caisse ou le compte est obligatoire")
    private Long caisseId;
    @Size(max = 50, message = "La référence ne doit pas dépasser 50 caractères")
    private String reference;
    private String observation;
    private LocalDateTime dateReglement;
}
