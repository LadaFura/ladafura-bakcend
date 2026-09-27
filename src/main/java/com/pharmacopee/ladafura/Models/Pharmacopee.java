package com.pharmacopee.ladafura.Models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.pharmacopee.ladafura.enums.StatutPharmacopee;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pharmacopees")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pharmacopee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nom;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String telephone;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutPharmacopee statut = StatutPharmacopee.EN_ATTENTE;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utilisateur_id")
    private Utilisateur utilisateur;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "localisation_id")
    private Localisation localisation;

    @Builder.Default
    @OneToMany(mappedBy = "pharmacopee", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ModeRetrait> modesRetrait = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "pharmacopee", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<DisponibiliteProduit> disponibilites = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "pharmacopee")
    @JsonIgnore
    private List<Commande> commandes = new ArrayList<>();
}
