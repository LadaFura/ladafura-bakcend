package com.pharmacopee.ladafura.Models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "etudes_scientifiques")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
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
}
