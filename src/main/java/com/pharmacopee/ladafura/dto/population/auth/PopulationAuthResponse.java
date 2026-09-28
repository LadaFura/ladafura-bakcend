package com.pharmacopee.ladafura.dto.population.auth;

import java.time.LocalDateTime;

import com.pharmacopee.ladafura.enums.Role;
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
@Schema(description = "Informations d'identification et de session de l'utilisateur Population connecté")
public class PopulationAuthResponse {

    @Schema(description = "Identifiant unique de l'utilisateur", example = "15")
    private Long id;

    @Schema(description = "Nom de famille", example = "Diarra")
    private String nom;

    @Schema(description = "Prénom", example = "Fatoumata")
    private String prenom;

    @Schema(description = "Adresse email officielle de l'utilisateur", example = "fatoumata.diarra@gmail.com")
    private String email;

    @Schema(description = "Numéro de téléphone de contact", example = "+223 70 12 34 56")
    private String telephone;

    @Schema(description = "Rôle applicatif attribué", example = "POPULATION")
    private Role role;

    @Schema(description = "Statut actuel du compte", example = "ACTIF")
    private StatutUtilisateur statut;

    @Schema(description = "Identifiant unique Firebase UID", example = "uid-firebase-12345")
    private String firebaseUid;

    @Schema(description = "Date de création du compte", example = "2026-09-28T14:00:00")
    private LocalDateTime dateCreation;
}
