package com.pharmacopee.ladafura.Models;

import java.util.ArrayList;
import java.util.List;

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
@NoArgsConstructor
@AllArgsConstructor
public class Praticien extends Utilisateur {

    private String numeroAgrement;

    private String specialite;

    @Column(nullable = false)
    private String planAbonnement = "GRATUIT";

    @Column(nullable = false)
    private Integer quotaMaxStructures = 6;

    @Column(nullable = false)
    private Boolean estPraticienPrincipal = true;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "praticiens_pharmacopees",
        joinColumns = @JoinColumn(name = "praticien_id"),
        inverseJoinColumns = @JoinColumn(name = "pharmacopee_id")
    )
    @JsonIgnore
    private List<Pharmacopee> pharmacopees = new ArrayList<>();
}
