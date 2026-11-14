package com.pharmacopee.ladafura.dto.pharmacopee.abonnement;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.pharmacopee.ladafura.enums.ModePaiementAbonnement;

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
@Schema(description = "Déclaration d'un paiement d'abonnement, à confirmer par l'administrateur")
public class DeclarerPaiementRequest {
    @NotNull(message = "Le montant est obligatoire")
    @DecimalMin(value = "0.01", message = "Le montant doit être positif")
    @Schema(example = "10000")
    private BigDecimal montant;
    @NotNull(message = "Le mode de paiement est obligatoire")
    private ModePaiementAbonnement modePaiement;
    @Size(max = 100, message = "La référence ne doit pas dépasser 100 caractères")
    @Schema(description = "Référence de la transaction", example = "OM240612.1530.A12345")
    private String referenceTransaction;
    private LocalDateTime datePaiement;

    @Size(max = 150, message = "Le nom du contact ne doit pas dépasser 150 caractères")
    @Schema(description = "Personne à joindre, obligatoire pour un paiement en espèces", example = "Aminata Diarra")
    private String contactNom;

    @Size(max = 30, message = "Le téléphone ne doit pas dépasser 30 caractères")
    @Schema(description = "Téléphone à joindre, obligatoire pour un paiement en espèces", example = "+223 70 11 22 33")
    private String contactTelephone;

    private String observation;
}
