package com.pharmacopee.ladafura.Models;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.pharmacopee.ladafura.enums.StatutPharmacopee;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "pharmacopees")
@Getter
@Setter
public class Pharmacopee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nom;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String telephone;

    @Column(name = "photo_url")
    private String photoUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutPharmacopee statut;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utilisateur_id")
    private Utilisateur utilisateur;

    @ManyToMany(mappedBy = "pharmacopees", fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<Praticien> praticiens;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "localisation_id")
    private Localisation localisation;

    @OneToMany(mappedBy = "pharmacopee", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ModeRetrait> modesRetrait;

    @OneToMany(mappedBy = "pharmacopee", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<DisponibiliteProduit> disponibilites;

    @OneToMany(mappedBy = "pharmacopee")
    @JsonIgnore
    private List<Commande> commandes;

    @OneToMany(mappedBy = "pharmacopee", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Avis> avis;

    public Pharmacopee() {
        this.statut = StatutPharmacopee.EN_ATTENTE;
        this.praticiens = new HashSet<>();
        this.modesRetrait = new ArrayList<>();
        this.disponibilites = new ArrayList<>();
        this.commandes = new ArrayList<>();
        this.avis = new ArrayList<>();
    }

    @Builder
    public Pharmacopee(Long id, String nom, String description, String telephone, String photoUrl,
                       StatutPharmacopee statut, Utilisateur utilisateur, Set<Praticien> praticiens,
                       Localisation localisation, List<ModeRetrait> modesRetrait,
                       List<DisponibiliteProduit> disponibilites, List<Commande> commandes, List<Avis> avis) {
        this.id = id;
        this.nom = nom;
        this.description = description;
        this.telephone = telephone;
        this.photoUrl = photoUrl;
        this.statut = statut != null ? statut : StatutPharmacopee.EN_ATTENTE;
        this.utilisateur = utilisateur;
        this.praticiens = praticiens != null ? praticiens : new HashSet<>();
        this.localisation = localisation;
        this.modesRetrait = modesRetrait != null ? modesRetrait : new ArrayList<>();
        this.disponibilites = disponibilites != null ? disponibilites : new ArrayList<>();
        this.commandes = commandes != null ? commandes : new ArrayList<>();
        this.avis = avis != null ? avis : new ArrayList<>();
    }
}
