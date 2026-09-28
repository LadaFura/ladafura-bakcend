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

    @Schema(description = "Identifiant du produit", example = "5")
    private Long produitId;

    @Schema(description = "Nom du remède / produit évalué", example = "Tisane Hépatite Kinkéliba")
    private String nomProduit;

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
}
