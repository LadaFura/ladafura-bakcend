package com.pharmacopee.ladafura.dto.population.profil;

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
@Schema(description = "Profil complet et statut de l'utilisateur de la Population")
public class PopulationProfileResponse {

    @Schema(description = "Identifiant unique de l'utilisateur", example = "15")
    private Long id;

    @Schema(description = "Nom de famille", example = "Diarra")
    private String nom;

    @Schema(description = "Prénom", example = "Fatoumata")
    private String prenom;

    @Schema(description = "Adresse email de connexion", example = "fatoumata.diarra@gmail.com")
    private String email;

    @Schema(description = "Numéro de téléphone de contact", example = "+223 70 12 34 56")
    private String telephone;

    @Schema(description = "Rôle applicatif attribué", example = "POPULATION")
    private Role role;

    @Schema(description = "Statut actuel du compte (ACTIF, INACTIF, SUSPENDU)", example = "ACTIF")
    private StatutUtilisateur statut;

    @Schema(description = "Identifiant unique Firebase UID", example = "uid-firebase-12345")
    private String firebaseUid;

    @Schema(description = "Date d'enregistrement du compte", example = "2026-09-28T14:00:00")
    private LocalDateTime dateCreation;

    @Schema(description = "Nombre total de commandes passées par cet utilisateur", example = "3")
    private long nombreTotalCommandes;

    @Schema(description = "Nombre d'éléments enregistrés en favoris", example = "5")
    private long nombreTotalFavoris;
}
