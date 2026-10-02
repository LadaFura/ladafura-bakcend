package com.pharmacopee.ladafura.Models;

import java.util.ArrayList;
import java.util.List;

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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utilisateur_id")
    private Utilisateur utilisateur;

    @Builder.Default
    @ManyToMany(mappedBy = "pharmacopees", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Praticien> praticiens = new ArrayList<>();

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
