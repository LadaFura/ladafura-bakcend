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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "plantes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Plante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nomScientifique;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String photoUrl;

    @Column(name = "image", nullable = true)
    private String image;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutPlante statut = StatutPlante.BROUILLON;

    @Builder.Default
    @OneToMany(mappedBy = "plante", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<NomPlante> nomsPlante = new ArrayList<>();

    @Builder.Default
    @ManyToMany
    @JoinTable(
            name = "plante_maladies",
            joinColumns = @JoinColumn(name = "plante_id"),
            inverseJoinColumns = @JoinColumn(name = "maladie_id")
    )
    private Set<Maladie> maladies = new HashSet<>();

    @Builder.Default
    @OneToMany(mappedBy = "plante", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<EtudeScientifique> etudesScientifiques = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "plante", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<VertuDeLaPlante> vertus = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "plante")
    @JsonIgnore
    private List<CompositionProduit> compositions = new ArrayList<>();
}
