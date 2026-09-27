package com.pharmacopee.ladafura.Models;
import java.util.Date;

import com.pharmacopee.ladafura.Enums.Role;
import com.pharmacopee.ladafura.Enums.StatutUtilisateur;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Getter 
@Setter 

@Table(name = "utilisateurs")
@NoArgsConstructor

public class Utilisateur {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false)
    private String prenom;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String motDePasse;

    private String telephone;

    private String adresse;

@Enumerated(EnumType.STRING)
    private Role role;


    private StatutUtilisateur statut;

    private Date dateCreation;
}
