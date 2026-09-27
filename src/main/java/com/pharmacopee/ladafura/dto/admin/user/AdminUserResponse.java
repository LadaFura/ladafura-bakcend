package com.pharmacopee.ladafura.dto.admin.user;

import java.time.LocalDateTime;

import com.pharmacopee.ladafura.enums.Role;
import com.pharmacopee.ladafura.enums.StatutUtilisateur;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminUserResponse {

    private Long id;
    private String nom;
    private String prenom;
    private String email;
    private String telephone;
    private Role role;
    private StatutUtilisateur statut;
    private LocalDateTime dateCreation;

    // Profil spécifique Agent
    private String matricule;
    private String zoneCouverture;

    // Profil spécifique Source
    private String specialite;
    private Integer anneesExperience;
    private String adresse;
}
