package com.pharmacopee.ladafura.dto.population.commande;

import java.time.LocalDateTime;

import com.pharmacopee.ladafura.enums.StatutCommande;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Suivi en direct du statut d'acheminement d'une commande")
public class PopulationCommandeStatutResponse {

    @Schema(description = "Identifiant unique de la commande", example = "42")
    private Long id;

    @Schema(description = "Numéro de référence", example = "CMD-20260928-ABC12")
    private String numero;

    @Schema(description = "Statut en cours", example = "EN_ATTENTE")
    private StatutCommande statut;

    @Schema(description = "Date de la commande")
    private LocalDateTime dateCommande;

    @Schema(description = "Date de dernière mise à jour du statut")
    private LocalDateTime dateMiseAJour;

    @Schema(description = "Mode de retrait", example = "LIVRAISON")
    private String modeRetrait;

    @Schema(description = "Possibilité d'annulation", example = "true")
    private Boolean annulable;

    @Schema(description = "Message d'explication convivial sur l'avancement", example = "Votre commande a été reçue par la pharmacopée et est en attente de confirmation.")
    private String message;
}
