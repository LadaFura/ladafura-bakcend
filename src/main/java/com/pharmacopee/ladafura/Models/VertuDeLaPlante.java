package com.pharmacopee.ladafura.Models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.pharmacopee.ladafura.enums.StatutValidation;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "vertus_plantes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VertuDeLaPlante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String usageRapporte;

    private String partieUtilisee;

    private String preparation;

    @Column(columnDefinition = "TEXT")
    private String precaution;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutValidation statut = StatutValidation.BROUILLON;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "collecte_id")
    @JsonIgnore
    private Collecte collecte;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plante_id")
    private Plante plante;
}
