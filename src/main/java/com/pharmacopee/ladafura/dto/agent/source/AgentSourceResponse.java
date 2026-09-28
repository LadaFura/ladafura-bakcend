package com.pharmacopee.ladafura.dto.agent.source;

import java.time.LocalDateTime;

import com.pharmacopee.ladafura.enums.Role;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Fiche détaillée d'une source de données de terrain (thérapeute traditionnel / herboriste)")
public class AgentSourceResponse {

    @Schema(description = "Identifiant unique de la source", example = "5")
    private Long id;

    @Schema(description = "Nom de famille de la source", example = "Diarra")
    private String nom;

    @Schema(description = "Prénom de la source", example = "Amadou")
    private String prenom;

    @Schema(description = "Nom complet de la source", example = "Amadou Diarra")
    private String nomComplet;

    @Schema(description = "Numéro de téléphone", example = "+223 70 22 33 44")
    private String telephone;

    @Schema(description = "Adresse email enregistrée ou référence traçable", example = "source.amadou.diarra.1712345@terrain.ladafura.ml")
    private String email;

    @Schema(description = "Adresse ou village de résidence", example = "Village de Finkolo, Cercle de Koutiala")
    private String adresse;

    @Schema(description = "Spécialité traditionnelle", example = "Tradipraticien spécialisé dans le traitement des fièvres")
    private String specialite;

    @Schema(description = "Années d'expérience", example = "25")
    private Integer anneesExperience;

    @Schema(description = "Profil de la source (THERAPEUTE ou HERBORISTE)", example = "THERAPEUTE")
    private Role role;

    @Schema(description = "Nombre de collectes associées à cette source", example = "3")
    private int nombreCollectes;

    @Schema(description = "Date d'enregistrement de la source", example = "2026-09-28T10:15:00")
    private LocalDateTime dateCreation;

    // Attributs garantissant la non-qualification en compte utilisateur
    @Builder.Default
    @Schema(description = "Atteste si la source est un compte utilisateur de la plateforme. Toujours false.", example = "false")
    private boolean estCompteUtilisateur = false;

    @Builder.Default
    @Schema(description = "Note de gouvernance et de traçabilité", example = "Source de données de terrain. Non utilisatrice de la plateforme LADAFURA.")
    private String noteTracabilite = "Source de données de terrain. Non utilisatrice de la plateforme LADAFURA.";
}
