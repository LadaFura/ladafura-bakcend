package com.pharmacopee.ladafura.dto.population.notification;

import java.time.LocalDateTime;

import com.pharmacopee.ladafura.enums.NiveauNotification;
import com.pharmacopee.ladafura.enums.TypeNotification;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Représentation d'une notification destinée à l'utilisateur Population")
public class PopulationNotificationResponse {

    @Schema(description = "Identifiant unique de la notification", example = "42")
    private Long id;

    @Schema(description = "Titre explicite de la notification", example = "Commande expédiée")
    private String titre;

    @Schema(description = "Message complet contenant les détails", example = "Votre commande CMD-20260928-8742 est en cours de livraison.")
    private String message;

    @Schema(description = "Type métier de la notification", example = "COMMANDE_STATUT")
    private TypeNotification type;

    @Schema(description = "Niveau d'importance (INFO, SUCCES, ATTENTION, URGENT)", example = "SUCCES")
    private NiveauNotification niveau;

    @Schema(description = "Indique si la notification a déjà été lue", example = "false")
    private Boolean lue;

    @Schema(description = "Date et heure de réception de la notification", example = "2026-09-28T14:30:00")
    private LocalDateTime dateNotification;

    @Schema(description = "Date et heure de consultation", example = "2026-09-28T14:45:00")
    private LocalDateTime dateLecture;

    @Schema(description = "Identifiant de l'entité concernée (ex: numéro de commande, ID d'avis)", example = "CMD-20260928-8742")
    private String referenceId;

    @Schema(description = "Lien de redirection", example = "/api/v1/population/commandes/15")
    private String lien;
}
