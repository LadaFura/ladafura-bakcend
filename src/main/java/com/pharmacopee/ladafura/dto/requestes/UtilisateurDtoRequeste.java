package com.pharmacopee.ladafura.dto.requestes;

import java.util.Date;

import com.pharmacopee.ladafura.Enums.Role;
import com.pharmacopee.ladafura.Enums.StatutUtilisateur;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data 
@Builder 
public class UtilisateurDtoRequeste {
    
    @NotBlank(message= "Le nom est obligatoire")
    private String nom;
    
    @NotBlank(message= "Le prénom est obligatoire")
    private String prenom;

    @NotBlank(message= "L'email est obligatoire")
    private String email;

    @NotBlank(message= "Le mot de passe est obligatoire")
    private String motDePasse;

    @NotBlank(message= "Le téléphone est obligatoire")
    private String telephone;

    @NotBlank(message= "L'adresse est obligatoire")
    private String adresse;

    @NotBlank(message= "Le rôle est obligatoire")
    private Role role;

    @NotBlank(message= "Le statut est obligatoire")
    private StatutUtilisateur statut;
    
    @NotBlank(message= "La date de création est obligatoire")
    private Date dateCreation;

}
