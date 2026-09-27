package com.pharmacopee.ladafura.dto.pharmacopee.profil;

import java.util.ArrayList;
import java.util.List;

import com.pharmacopee.ladafura.dto.pharmacopee.localisation.PharmacopeeLocalisationResponse;
import com.pharmacopee.ladafura.enums.StatutPharmacopee;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Profil complet de la pharmacopée et de son responsable")
public class PharmacopeeProfileResponse {

    @Schema(description = "Identifiant de la pharmacopée", example = "1")
    private Long id;

    @Schema(description = "Nom officiel de l'établissement", example = "Pharmacie Traditionnelle Mandé")
    private String nom;

    @Schema(description = "Description des activités et remèdes", example = "Officine spécialisée dans les remèdes du terroir Mandé.")
    private String description;

    @Schema(description = "Numéro de téléphone officiel de l'officine", example = "+223 20 22 33 44")
    private String telephone;

    @Schema(description = "Statut de référencement administratif", example = "VALIDEE")
    private StatutPharmacopee statut;

    @Schema(description = "Indique si l'établissement est agréé et validé", example = "true")
    private boolean validee;

    @Schema(description = "Identifiant du compte utilisateur responsable", example = "2")
    private Long utilisateurId;

    @Schema(description = "Nom de famille du responsable", example = "Traoré")
    private String nomResponsable;

    @Schema(description = "Prénom du responsable", example = "Modibo")
    private String prenomResponsable;

    @Schema(description = "Email officiel du compte (identifiant Firebase)", example = "contact@pharmacopee-mande.ml")
    private String emailResponsable;

    @Schema(description = "Numéro de téléphone personnel du responsable", example = "+223 70 11 22 33")
    private String telephoneResponsable;

    @Schema(description = "Localisation géographique et coordonnées GPS de l'officine")
    private PharmacopeeLocalisationResponse localisation;

    @Builder.Default
    @Schema(description = "Modes de retrait actuellement configurés (LIVRAISON, PICKUP)", example = "[\"LIVRAISON\", \"PICKUP\"]")
    private List<String> modesRetraitActifs = new ArrayList<>();

    @Schema(description = "Nombre total de produits actuellement référencés dans le catalogue de cette pharmacopée", example = "14")
    private int nombreProduitsDisponibles;
}
