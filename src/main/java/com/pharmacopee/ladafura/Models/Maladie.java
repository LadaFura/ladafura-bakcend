package com.pharmacopee.ladafura.Models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "maladies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
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
    private Set<Plante> plantes = new HashSet<>();
}
