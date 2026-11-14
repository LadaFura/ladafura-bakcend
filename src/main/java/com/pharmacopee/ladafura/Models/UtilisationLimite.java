package com.pharmacopee.ladafura.Models;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Compteur périodique, réservé aux limites non recalculables (ModeCalculLimite.COMPTEUR).
 */
@Entity
@Table(name = "utilisations_limite",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_utilisation_limite_periode", columnNames = {"limite_abonnement_id", "periode_debut"})
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UtilisationLimite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "limite_abonnement_id", nullable = false)
    private LimiteAbonnement limiteAbonnement;

    @Column(nullable = false)
    private LocalDate periodeDebut;

    @Column(nullable = false)
    private LocalDate periodeFin;

    @Column(nullable = false)
    private Integer valeurUtilisee;

    @Column(nullable = false)
    private LocalDateTime dateDerniereMiseAJour;

    @PrePersist
    public void prePersist() {
        if (this.valeurUtilisee == null) this.valeurUtilisee = 0;
        if (this.dateDerniereMiseAJour == null) this.dateDerniereMiseAJour = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.dateDerniereMiseAJour = LocalDateTime.now();
    }
}
