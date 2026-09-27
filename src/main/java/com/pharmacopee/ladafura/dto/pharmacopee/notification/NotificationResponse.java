package com.pharmacopee.ladafura.dto.pharmacopee.notification;

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
@Schema(description = "Fiche détaillée d'une notification reçue par la pharmacopée")
public class NotificationResponse {

    @Schema(description = "Identifiant de la notification", example = "1")
    private Long id;

    @Schema(description = "Titre résumé", example = "Nouvelle commande reçue")
    private String titre;

    @Schema(description = "Corps du message de notification", example = "La commande CMD-2026-00042 d'un montant de 7000 FCFA est en attente de préparation.")
    private String message;

    @Schema(description = "Type fonctionnel de la notification", example = "COMMANDE_NOUVELLE")
    private TypeNotification type;

    @Schema(description = "Niveau de sévérité ou priorité", example = "INFO")
    private NiveauNotification niveau;

    @Schema(description = "Indique si la notification a déjà été consultée", example = "false")
    private Boolean lue;

    @Schema(description = "Date et heure de réception")
    private LocalDateTime dateNotification;

    @Schema(description = "Date et heure de première lecture")
    private LocalDateTime dateLecture;

    @Schema(description = "Identifiant de la ressource liée (ex: référence commande, avis)", example = "CMD-2026-00042")
    private String referenceId;

    @Schema(description = "Lien ou route applicative pour redirection directe", example = "/commandes/10")
    private String lien;
}
