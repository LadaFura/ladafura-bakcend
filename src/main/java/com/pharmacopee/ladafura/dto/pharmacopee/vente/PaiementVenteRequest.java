package com.pharmacopee.ladafura.dto.pharmacopee.vente;

import java.math.BigDecimal;

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
@Schema(description = "Paiement d'une vente")
public class PaiementVenteRequest {
    @NotNull(message = "Le montant est obligatoire")
    @DecimalMin(value = "0.01", message = "Le montant doit être positif")
    private BigDecimal montant;
    @NotNull(message = "Le mode de paiement est obligatoire")
    private MethodePaiement methode;
    @NotNull(message = "La caisse ou le compte est obligatoire")
    private Long caisseId;
    @Size(max = 100, message = "La référence ne doit pas dépasser 100 caractères")
    private String referenceTransaction;
    private String observation;
}
