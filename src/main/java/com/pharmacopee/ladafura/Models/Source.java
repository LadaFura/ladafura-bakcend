package com.pharmacopee.ladafura.Models;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "sources_connaissances")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Source extends Utilisateur {

    private String specialite;

    private Integer anneesExperience;

    private String adresse;

    @OneToMany(mappedBy = "source", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Collecte> collectes = new ArrayList<>();
}
