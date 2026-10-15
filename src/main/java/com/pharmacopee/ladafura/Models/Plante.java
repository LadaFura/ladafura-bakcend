package com.pharmacopee.ladafura.Models;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.pharmacopee.ladafura.enums.StatutPlante;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "plantes")
@Getter
@Setter
public class Plante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nomScientifique;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String photoUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutPlante statut;

    @OneToMany(mappedBy = "plante", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<NomPlante> nomsPlante;

    @ManyToMany
    @JoinTable(
            name = "plante_maladies",
            joinColumns = @JoinColumn(name = "plante_id"),
            inverseJoinColumns = @JoinColumn(name = "maladie_id")
    )
    private Set<Maladie> maladies;

    @OneToMany(mappedBy = "plante", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<EtudeScientifique> etudesScientifiques;

    @OneToMany(mappedBy = "plante", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<VertuDeLaPlante> vertus;

    @OneToMany(mappedBy = "plante")
    @JsonIgnore
    private List<CompositionProduit> compositions;

    public Plante() {
        this.statut = StatutPlante.BROUILLON;
        this.nomsPlante = new ArrayList<>();
        this.maladies = new HashSet<>();
        this.etudesScientifiques = new ArrayList<>();
        this.vertus = new ArrayList<>();
        this.compositions = new ArrayList<>();
    }

    @Builder
    public Plante(Long id, String nomScientifique, String description, String photoUrl,
                  StatutPlante statut, List<NomPlante> nomsPlante, Set<Maladie> maladies,
                  List<EtudeScientifique> etudesScientifiques, List<VertuDeLaPlante> vertus,
                  List<CompositionProduit> compositions) {
        this.id = id;
        this.nomScientifique = nomScientifique;
        this.description = description;
        this.photoUrl = photoUrl;
        this.statut = statut != null ? statut : StatutPlante.BROUILLON;
        this.nomsPlante = nomsPlante != null ? nomsPlante : new ArrayList<>();
        this.maladies = maladies != null ? maladies : new HashSet<>();
        this.etudesScientifiques = etudesScientifiques != null ? etudesScientifiques : new ArrayList<>();
        this.vertus = vertus != null ? vertus : new ArrayList<>();
        this.compositions = compositions != null ? compositions : new ArrayList<>();
    }
}
