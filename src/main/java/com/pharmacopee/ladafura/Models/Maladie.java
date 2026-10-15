package com.pharmacopee.ladafura.Models;

import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "maladies")
@Getter
@Setter
public class Maladie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nom;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToMany(mappedBy = "maladies")
    @JsonIgnore
    private Set<Plante> plantes;

    public Maladie() {
        this.plantes = new HashSet<>();
    }

    @Builder
    public Maladie(Long id, String nom, String description, Set<Plante> plantes) {
        this.id = id;
        this.nom = nom;
        this.description = description;
        this.plantes = plantes != null ? plantes : new HashSet<>();
    }
}
