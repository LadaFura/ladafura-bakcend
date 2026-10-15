package com.pharmacopee.ladafura.Models;

import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "praticiens")
@Getter
@Setter
public class Praticien extends Utilisateur {

    private String numeroAgrement;

    private String specialite;

    @Column(nullable = false)
    private String planAbonnement;

    @Column(nullable = false)
    private Integer quotaMaxStructures;

    @Column(nullable = false)
    private Boolean estPraticienPrincipal;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "praticiens_pharmacopees",
        joinColumns = @JoinColumn(name = "praticien_id"),
        inverseJoinColumns = @JoinColumn(name = "pharmacopee_id")
    )
    @JsonIgnore
    private Set<Pharmacopee> pharmacopees;

    public Praticien() {
        super();
        this.planAbonnement = "GRATUIT";
        this.quotaMaxStructures = 2;
        this.estPraticienPrincipal = true;
        this.pharmacopees = new HashSet<>();
    }
}
