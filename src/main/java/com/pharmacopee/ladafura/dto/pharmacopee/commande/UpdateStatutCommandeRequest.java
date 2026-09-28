package com.pharmacopee.ladafura.dto.pharmacopee.commande;

import com.pharmacopee.ladafura.enums.StatutCommande;

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
@Schema(description = "Formulaire de changement de statut d'une commande")
public class UpdateStatutCommandeRequest {

    @NotNull(message = "Le nouveau statut de commande est obligatoire")
    @Schema(description = "Nouveau statut à appliquer", example = "PREPAREE")
    private StatutCommande nouveauStatut;

    @Schema(description = "Motif ou commentaire éventuel (recommandé en cas d'annulation)", example = "Rupture de stock temporaire sur l'une des références.")
    private String motif;
}
