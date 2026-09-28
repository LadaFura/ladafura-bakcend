package com.pharmacopee.ladafura.dto.population.historique;

import java.time.LocalDateTime;

import com.pharmacopee.ladafura.enums.TypeEvenementHistorique;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Entrée unitaire du journal chronologique d'activité du client")
public class PopulationJournalActiviteItem {

    @Schema(description = "Identifiant technique de l'événement", example = "CMD-15")
    private String id;

    @Schema(description = "Type métier de l'événement (COMMANDE, PAIEMENT, AVIS, FAVORI, NOTIFICATION)", example = "COMMANDE")
    private TypeEvenementHistorique type;

    @Schema(description = "Titre explicite de l'activité", example = "Commande validée")
    private String titre;

    @Schema(description = "Description détaillée ou résumé de l'activité", example = "Commande CMD-20260928-8742 auprès de la Pharmacie Mandé")
    private String description;

    @Schema(description = "Date et heure de l'événement", example = "2026-09-28T14:30:00")
    private LocalDateTime dateEvenement;

    @Schema(description = "Référence ou identifiant cible (ex: numéro de commande, code paiement)", example = "CMD-20260928-8742")
    private String reference;

    @Schema(description = "Statut associé à l'événement", example = "VALIDEE")
    private String statut;

    @Schema(description = "Montant financier en FCFA (si applicable)", example = "7500.0")
    private Double montant;

    @Schema(description = "Lien de consultation détaillée dans l'application", example = "/api/v1/population/commandes/15")
    private String lienDetail;
}
