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
@Table(name = "noms_plantes")
@Getter
@Setter
public class NomPlante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nom;

    private String langue;

    private String pays;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plante_id", nullable = false)
    @JsonIgnore
    private Plante plante;

    public NomPlante() {
        this.pays = "Mali";
    }

    @Builder
    public NomPlante(Long id, String nom, String langue, String pays, Plante plante) {
        this.id = id;
        this.nom = nom;
        this.langue = langue;
        this.pays = pays != null ? pays : "Mali";
        this.plante = plante;
    }
}
