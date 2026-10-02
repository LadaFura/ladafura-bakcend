package com.pharmacopee.ladafura.dto.pharmacopee.auth;

import com.pharmacopee.ladafura.enums.Role;
import com.pharmacopee.ladafura.enums.StatutPharmacopee;
import com.pharmacopee.ladafura.enums.StatutUtilisateur;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Informations sur l'utilisateur et l'entité Pharmacopée connectée")
public class PharmacopeeAuthResponse {

    @Schema(description = "Identifiant MySQL de l'utilisateur", example = "5")
    private Long utilisateurId;

    @Schema(description = "Nom de l'utilisateur", example = "Traoré")
    private String nom;

    @Schema(description = "Prénom de l'utilisateur", example = "Modibo")
    private String prenom;

    @Schema(description = "Adresse email officielle", example = "contact@pharmacopee-mali.ml")
    private String email;

    @Schema(description = "Numéro de téléphone de l'utilisateur", example = "+223 70 11 22 33")
    private String telephone;

    @Schema(description = "Rôle applicatif de l'utilisateur", example = "PHARMACOPEE")
    private Role role;

    @Schema(description = "Statut du compte utilisateur", example = "ACTIF")
    private StatutUtilisateur statutUtilisateur;

    @Schema(description = "Identifiant unique Firebase UID", example = "9bK8...Z2")
    private String firebaseUid;

    @Schema(description = "Identifiant de la pharmacopée associée (si référencée)", example = "1")
    private Long pharmacopeeId;

    @Schema(description = "Nom de l'établissement de pharmacopée", example = "Pharmacie Traditionnelle Mandé")
    private String nomPharmacopee;

    @Schema(description = "Téléphone de contact de la pharmacopée", example = "+223 20 22 33 44")
    private String telephonePharmacopee;

    @Schema(description = "Statut de référencement de la pharmacopée (EN_ATTENTE, VALIDE, SUSPENDU, REJETE)", example = "VALIDE")
    private StatutPharmacopee statutReferencement;

    @Schema(description = "Indique si la pharmacopée est validée et autorisée aux opérations commerciales", example = "true")
    private boolean validee;

    @Schema(description = "Liste de toutes les structures rattachées au praticien / utilisateur")
    @Builder.Default
    private java.util.List<PharmacopeeItemSummaryDto> structures = new java.util.ArrayList<>();

    @Schema(description = "Formule d'abonnement active", example = "GRATUIT")
    private String planAbonnement;

    @Schema(description = "Nombre maximal de structures autorisées par l'abonnement", example = "1")
    private Integer quotaMaxStructures;

    @Schema(description = "Nombre de structures actuellement créées", example = "1")
    private Integer structuresActuelles;

    @Schema(description = "Indique si l'utilisateur a le droit d'ajouter une structure supplémentaire", example = "false")
    private boolean peutCreerStructure;

    @Schema(description = "Indique si l'utilisateur est le praticien principal (habilité à basculer entre ses structures)", example = "true")
    private boolean estPraticienPrincipal;

    @Schema(description = "Titre officiel du praticien dans l'établissement", example = "Praticien Principal")
    private String titrePraticien;
}
