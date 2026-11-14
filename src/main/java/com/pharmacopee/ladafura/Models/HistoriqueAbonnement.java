package com.pharmacopee.ladafura.Models;

import java.time.LocalDateTime;

import org.hibernate.annotations.Immutable;

import com.pharmacopee.ladafura.enums.TypeEvenementAbonnement;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Historique métier d'un abonnement : une ligne par changement de statut ou d'offre. Jamais modifié.
 */
@Entity
@Immutable
@Table(name = "historiques_abonnement",
    indexes = {
        @Index(name = "idx_historiques_abonnement_date", columnList = "abonnement_id, date_evenement")
    })
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistoriqueAbonnement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "abonnement_id", nullable = false)
    private Abonnement abonnement;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TypeEvenementAbonnement typeEvenement;

    private String ancienneValeur;

    private String nouvelleValeur;

    @Column(nullable = false)
    private LocalDateTime dateEvenement;

    @Column(columnDefinition = "TEXT")
    private String description;

    /** Null si l'action vient du système. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "acteur_id")
    private Utilisateur acteur;

    @PrePersist
    public void prePersist() {
        if (this.dateEvenement == null) this.dateEvenement = LocalDateTime.now();
    }
}
