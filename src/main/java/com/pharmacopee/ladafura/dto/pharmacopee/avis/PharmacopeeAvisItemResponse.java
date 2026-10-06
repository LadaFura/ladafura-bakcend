package com.pharmacopee.ladafura.dto.pharmacopee.avis;

import java.time.LocalDateTime;

import com.pharmacopee.ladafura.enums.StatutAvis;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Avis client modéré et publié sur un produit commercialisé par l'officine")
public class PharmacopeeAvisItemResponse {

    @Schema(description = "Identifiant de l'avis", example = "1")
    private Long id;

    @Schema(description = "Identifiant de la pharmacopée", example = "1")
    private Long pharmacopeeId;

    @Schema(description = "Nom de la pharmacopée évaluée", example = "danaya")
    private String nomPharmacopee;

    @Schema(description = "Note attribuée (entre 1 et 5)", example = "5")
    private Integer note;

    @Schema(description = "Commentaire ou retour d'expérience du patient/acheteur", example = "Très efficace dès la première semaine, produit traditionnel de qualité.")
    private String commentaire;

    @Schema(description = "Date de publication de l'avis")
    private LocalDateTime dateAvis;

    @Schema(description = "Auteur de l'évaluation", example = "Amadou T.")
    private String auteurNom;

    @Schema(description = "Statut de modération (toujours PUBLIE dans l'espace pharmacopée)", example = "PUBLIE")
    private StatutAvis statut;

    @Schema(description = "Réponse de l'officine à l'avis", example = "Merci pour votre retour.")
    private String reponseOfficine;

    @Schema(description = "Date de la réponse de l'officine")
    private LocalDateTime dateReponse;
}
