package com.pharmacopee.ladafura.Models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.pharmacopee.ladafura.enums.StatutValidation;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    @Builder.Default
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
