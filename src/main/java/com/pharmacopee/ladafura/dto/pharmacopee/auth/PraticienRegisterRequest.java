package com.pharmacopee.ladafura.dto.pharmacopee.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Inscription publique d'un praticien principal avec création de sa pharmacopée (statut EN_ATTENTE).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PraticienRegisterRequest {

    @NotBlank(message = "Le nom est obligatoire")
    private String nom;

    @NotBlank(message = "Le prénom est obligatoire")
    private String prenom;

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Format d'email invalide")
    private String email;

    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 6, message = "Le mot de passe doit contenir au moins 6 caractères")
    private String motDePasse;

    private String telephone;
    private String numeroAgrement;
    private String specialite;

    @NotBlank(message = "Le nom de la pharmacopée est obligatoire")
    private String nomPharmacopee;

    private String descriptionPharmacopee;

    @NotBlank(message = "Le téléphone de la pharmacopée est obligatoire")
    @Size(min = 8, max = 25, message = "Le téléphone de la pharmacopée doit contenir entre 8 et 25 caractères")
    private String telephonePharmacopee;

    private String region;
    private String cercle;
    private String commune;
    private String localite;
    private Double latitude;
    private Double longitude;
}

