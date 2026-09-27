package com.pharmacopee.ladafura.Models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.pharmacopee.ladafura.enums.StatutPlante;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
