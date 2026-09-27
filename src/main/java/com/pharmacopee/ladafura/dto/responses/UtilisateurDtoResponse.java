package com.pharmacopee.ladafura.dto.responses;

import java.util.Date;

import com.pharmacopee.ladafura.Enums.Role;
import com.pharmacopee.ladafura.Enums.StatutUtilisateur;

import lombok.Builder;
import lombok.Data;

@Data 
@Builder
public class UtilisateurDtoResponse {

    private Long id;

    private String nom;

    private String prenom;

    private String email;

    private String telephone;

    private String adresse;

    private Role role;

    private StatutUtilisateur statut;

    private Date dateCreation;
}
