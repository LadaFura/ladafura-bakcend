package com.pharmacopee.ladafura.Models;

import java.time.LocalDateTime;

import com.pharmacopee.ladafura.enums.StatutFournisseur;
import com.pharmacopee.ladafura.enums.TypeFournisseur;

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
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Fournisseur d'une pharmacopée. Gestion facultative : une pharmacopée peut ne jamais en créer.
 * N'est ni un Utilisateur ni un agent de collecte. Voir gestion-fournisseurs.puml.
 */
@Entity
@Table(name = "fournisseurs",
    indexes = {
        @Index(name = "idx_fournisseurs_pharmacopee_statut", columnList = "pharmacopee_id, statut")
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Fournisseur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Tenant propriétaire de la donnée. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pharmacopee_id", nullable = false)
    private Pharmacopee pharmacopee;

    @Column(nullable = false)
    private String nom;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TypeFournisseur typeFournisseur;

    @Column(length = 30)
    private String telephone;

    private String email;

    private String adresse;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatutFournisseur statut;

    @Column(nullable = false)
    private LocalDateTime dateCreation;

    private LocalDateTime dateModification;

    @PrePersist
    public void prePersist() {
        if (this.typeFournisseur == null) this.typeFournisseur = TypeFournisseur.AUTRE;
        if (this.statut == null) this.statut = StatutFournisseur.ACTIF;
        if (this.dateCreation == null) this.dateCreation = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.dateModification = LocalDateTime.now();
    }

    public boolean estActif() {
        return this.statut == StatutFournisseur.ACTIF;
    }
}
