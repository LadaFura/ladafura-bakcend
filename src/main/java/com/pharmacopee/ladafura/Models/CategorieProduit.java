package com.pharmacopee.ladafura.Models;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "categories_produits")
@Getter
@Setter
public class CategorieProduit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nom;

    private String description;

    @Column(nullable = false)
    private Boolean statut;

    @OneToMany(mappedBy = "categorie", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Produit> produits;

    public CategorieProduit() {
        this.statut = true;
        this.produits = new ArrayList<>();
    }

    @Builder
    public CategorieProduit(Long id, String nom, String description, Boolean statut, List<Produit> produits) {
        this.id = id;
        this.nom = nom;
        this.description = description;
        this.statut = statut != null ? statut : true;
        this.produits = produits != null ? produits : new ArrayList<>();
    }
}
