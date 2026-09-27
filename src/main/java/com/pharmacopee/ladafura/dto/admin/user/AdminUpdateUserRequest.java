package com.pharmacopee.ladafura.dto.admin.user;

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
public class AdminUpdateUserRequest {

    private String nom;
    private String prenom;
    private String telephone;
    private Role role;
    private StatutUtilisateur statut;

    // Champs spécifiques si applicable
    private String matricule;
    private String zoneCouverture;
    private String specialite;
    private Integer anneesExperience;
    private String adresse;
}
