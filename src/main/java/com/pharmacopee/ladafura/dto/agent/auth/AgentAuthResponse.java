package com.pharmacopee.ladafura.dto.agent.auth;

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
@Schema(description = "Informations d'identification et de session de l'Agent de Collecte connecté")
public class AgentAuthResponse {

    @Schema(description = "Identifiant unique de l'agent de collecte", example = "4")
    private Long id;

    @Schema(description = "Matricule professionnel de l'agent", example = "AGT-2026-004")
    private String matricule;

    @Schema(description = "Zone géographique de couverture assignée", example = "Région de Sikasso - Cercle de Koutiala")
    private String zoneCouverture;

    @Schema(description = "Nom de famille de l'agent", example = "Coulibaly")
    private String nom;

    @Schema(description = "Prénom de l'agent", example = "Oumar")
    private String prenom;

    @Schema(description = "Adresse email officielle", example = "oumar.coulibaly@ladafura.ml")
    private String email;

    @Schema(description = "Numéro de téléphone professionnel", example = "+223 75 00 11 22")
    private String telephone;

    @Schema(description = "Rôle applicatif de l'utilisateur", example = "AGENT_COLLECTE")
    private Role role;

    @Schema(description = "Statut du compte agent", example = "ACTIF")
    private StatutUtilisateur statut;

    @Schema(description = "Identifiant unique Firebase UID", example = "kL89zV1...mN")
    private String firebaseUid;

    @Schema(description = "Date de création du compte", example = "2026-09-28T09:00:00")
    private LocalDateTime dateCreation;
}
