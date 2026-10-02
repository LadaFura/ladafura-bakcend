package com.pharmacopee.ladafura.dto.admin.user;

import com.pharmacopee.ladafura.enums.Role;
import com.pharmacopee.ladafura.enums.StatutUtilisateur;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminCreateUserRequest {

    @NotBlank(message = "Le nom est obligatoire")
    private String nom;

    @NotBlank(message = "Le prénom est obligatoire")
    private String prenom;

    @NotBlank(message = "L'adresse email est obligatoire")
    @Email(message = "Format d'email invalide")
    private String email;

    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 6, message = "Le mot de passe doit comporter au moins 6 caractères")
    private String motDePasse;

    private String telephone;

    @NotNull(message = "Le rôle est obligatoire")
    private Role role;

    @Builder.Default
    private StatutUtilisateur statut = StatutUtilisateur.ACTIF;

    // Champs spécifiques si l'utilisateur est un Agent de Collecte
    private String matricule;
    private String zoneCouverture;

    // Champs spécifiques si l'utilisateur est une Source (Thérapeute / Herboriste)
    private String specialite;
    private Integer anneesExperience;
    private String adresse;

    // Champs spécifiques si l'utilisateur est une Pharmacopée / Praticien
    private Boolean estPraticienPrincipal;
    private String typePharmacopee;
    private String numeroAgrement;
}
