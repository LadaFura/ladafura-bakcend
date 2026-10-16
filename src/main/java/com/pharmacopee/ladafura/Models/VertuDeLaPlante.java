package com.pharmacopee.ladafura.Models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.pharmacopee.ladafura.enums.StatutValidation;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "vertus_plantes")
@Getter
@Setter
public class VertuDeLaPlante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String usageRapporte;

    @Column(columnDefinition = "TEXT")
    private String partieUtilisee;

    @Column(columnDefinition = "TEXT")
    private String preparation;

    @Column(columnDefinition = "TEXT")
    private String precaution;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutValidation statut;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "collecte_id")
    @JsonIgnore
    private Collecte collecte;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plante_id")
    private Plante plante;

    public VertuDeLaPlante() {
        this.statut = StatutValidation.BROUILLON;
    }

    @Builder
    public VertuDeLaPlante(Long id, String usageRapporte, String partieUtilisee, String preparation,
                           String precaution, String description, StatutValidation statut,
                           Collecte collecte, Plante plante) {
        this.id = id;
        this.usageRapporte = usageRapporte;
        this.partieUtilisee = partieUtilisee;
        this.preparation = preparation;
        this.precaution = precaution;
        this.description = description;
        this.statut = statut != null ? statut : StatutValidation.BROUILLON;
        this.collecte = collecte;
        this.plante = plante;
    }
}
