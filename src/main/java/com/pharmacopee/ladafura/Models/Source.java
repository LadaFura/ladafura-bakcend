package com.pharmacopee.ladafura.Models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

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
