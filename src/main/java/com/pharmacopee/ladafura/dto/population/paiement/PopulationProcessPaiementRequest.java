package com.pharmacopee.ladafura.dto.population.paiement;

import com.pharmacopee.ladafura.enums.MethodePaiement;

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
@Schema(description = "Demande d'exécution ou d'initiation d'un paiement de commande")
public class PopulationProcessPaiementRequest {

    @NotNull(message = "L'identifiant de la commande est obligatoire")
    @Schema(description = "Identifiant unique de la commande à régler", example = "42")
    private Long commandeId;

    @NotNull(message = "Le mode de paiement est obligatoire")
    @Schema(description = "Moyen retenu : MOBILE_MONEY, CASH ou CARTE_BANCAIRE", example = "MOBILE_MONEY")
    private MethodePaiement methode;

    @Schema(description = "Opérateur choisi pour le Mobile Money (ex: ORANGE_MONEY, MOOV_MONEY, WAVE)", example = "ORANGE_MONEY")
    private String operateur;

    @Schema(description = "Numéro de téléphone émetteur pour le Mobile Money", example = "+223 70 11 22 33")
    private String telephoneMobileMoney;

    @Schema(description = "Référence externe de transaction (fournie par l'opérateur ou facultative)", example = "OM-ML-2026-998811")
    private String referenceTransaction;

    @Builder.Default
    @Schema(description = "Simulation MVP : forcer le succès (true) ou l'échec (false) du règlement", defaultValue = "true", example = "true")
    private Boolean simulerSucces = true;
}
