package com.pharmacopee.ladafura.dto.agent.profil;

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
@Schema(description = "Profil complet et statut de l'Agent de Collecte")
public class AgentProfileResponse {

    @Schema(description = "Identifiant unique de l'agent", example = "4")
    private Long id;

    @Schema(description = "Matricule officiel de l'agent de terrain", example = "AGT-2026-004")
    private String matricule;

    @Schema(description = "Zone géographique couverte par l'agent", example = "Région de Sikasso - Cercle de Koutiala")
    private String zoneCouverture;

    @Schema(description = "Nom de famille", example = "Coulibaly")
    private String nom;

    @Schema(description = "Prénom", example = "Oumar")
    private String prenom;

    @Schema(description = "Adresse email de connexion", example = "oumar.coulibaly@ladafura.ml")
    private String email;

    @Schema(description = "Numéro de téléphone professionnel", example = "+223 75 00 11 22")
    private String telephone;

    @Schema(description = "Rôle applicatif attribué", example = "AGENT_COLLECTE")
    private Role role;

    @Schema(description = "Statut actuel du compte (ACTIF, INACTIF, SUSPENDU)", example = "ACTIF")
    private StatutUtilisateur statut;

    @Schema(description = "Identifiant Firebase UID", example = "kL89zV1...mN")
    private String firebaseUid;

    @Schema(description = "Date d'enregistrement du compte", example = "2026-09-28T09:00:00")
    private LocalDateTime dateCreation;

    @Schema(description = "Nombre total de collectes enregistrées par cet agent", example = "12")
    private long nombreTotalCollectes;
}
