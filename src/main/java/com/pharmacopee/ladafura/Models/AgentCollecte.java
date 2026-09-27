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
@Table(name = "agents_collecte")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AgentCollecte extends Utilisateur {

    private String matricule;

    private String zoneCouverture;

    @OneToMany(mappedBy = "agentCollecte", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Collecte> collectes = new ArrayList<>();
}
