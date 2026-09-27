package com.pharmacopee.ladafura.Models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.pharmacopee.ladafura.enums.StatutProduit;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "produits")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
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

    @Column(nullable = false)
    private Double prix;

    private String photoUrl;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutProduit statut = StatutProduit.EN_ATTENTE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categorie_id")
    private CategorieProduit categorie;

    @Builder.Default
    @OneToMany(mappedBy = "produit", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CompositionProduit> compositions = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "produit", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<DisponibiliteProduit> disponibilites = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "produit", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Avis> avis = new ArrayList<>();
}
