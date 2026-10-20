package com.pharmacopee.ladafura.Models;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.pharmacopee.ladafura.enums.StatutProduit;

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
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "produits")
@Getter
@Setter
public class Produit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nom;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String forme;

    @Column(columnDefinition = "TEXT")
    private String composition;

    @Column(columnDefinition = "TEXT")
    private String posologie;

    @Column(nullable = false)
    private Double prix;

    private String photoUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutProduit statut;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categorie_id")
    private CategorieProduit categorie;

    @OneToMany(mappedBy = "produit", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CompositionProduit> compositions;

    @ManyToMany
    @JoinTable(
            name = "produit_maladies",
            joinColumns = @JoinColumn(name = "produit_id"),
            inverseJoinColumns = @JoinColumn(name = "maladie_id")
    )
    private Set<Maladie> maladies;

    @OneToMany(mappedBy = "produit", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<DisponibiliteProduit> disponibilites;

    public Produit() {
        this.statut = StatutProduit.EN_ATTENTE;
        this.compositions = new ArrayList<>();
        this.maladies = new HashSet<>();
        this.disponibilites = new ArrayList<>();
    }

    @Builder
    public Produit(Long id, String nom, String description, String forme, String composition,
                   String posologie, Double prix, String photoUrl, StatutProduit statut, CategorieProduit categorie,
                   List<CompositionProduit> compositions, Set<Maladie> maladies,
                   List<DisponibiliteProduit> disponibilites) {
        this.id = id;
        this.nom = nom;
        this.description = description;
        this.forme = forme;
        this.composition = composition;
        this.posologie = posologie;
        this.prix = prix;
        this.photoUrl = photoUrl;
        this.statut = statut != null ? statut : StatutProduit.EN_ATTENTE;
        this.categorie = categorie;
        this.compositions = compositions != null ? compositions : new ArrayList<>();
        this.maladies = maladies != null ? maladies : new HashSet<>();
        this.disponibilites = disponibilites != null ? disponibilites : new ArrayList<>();
    }
}
