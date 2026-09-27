package com.pharmacopee.ladafura.Models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

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
