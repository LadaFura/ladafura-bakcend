package com.pharmacopee.ladafura.Models;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "etudes_scientifiques")
@Getter
@Setter
public class EtudeScientifique {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String titre;

    private String auteurs;

    private Integer annee;

    private String reference;

    @Column(columnDefinition = "TEXT")
    private String resume;

    private String documentUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plante_id")
    @JsonIgnore
    private Plante plante;

    public EtudeScientifique() {
    }

    @Builder
    public EtudeScientifique(Long id, String titre, String auteurs, Integer annee, String reference, String resume, String documentUrl, Plante plante) {
        this.id = id;
        this.titre = titre;
        this.auteurs = auteurs;
        this.annee = annee;
        this.reference = reference;
        this.resume = resume;
        this.documentUrl = documentUrl;
        this.plante = plante;
    }
}
