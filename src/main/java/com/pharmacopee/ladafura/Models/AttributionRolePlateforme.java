package com.pharmacopee.ladafura.Models;

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
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Attribution d'un rôle de plateforme à un utilisateur (par l'administrateur LADAFURA).
 * Un utilisateur peut cumuler plusieurs rôles de plateforme.
 */
@Entity
@Table(name = "attributions_roles_plateforme", uniqueConstraints = {
    @UniqueConstraint(name = "uk_attribution_role_plateforme", columnNames = {"utilisateur_id", "role_plateforme_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttributionRolePlateforme {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "utilisateur_id", nullable = false)
    private Utilisateur utilisateur;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "role_plateforme_id", nullable = false)
    private RolePlateforme rolePlateforme;

    /** Administrateur ayant effectué l'attribution (null pour les reprises de données). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attribue_par_id")
    private Utilisateur attribuePar;

    @Column(nullable = false)
    private LocalDateTime dateAttribution;

    @PrePersist
    public void prePersist() {
        if (this.dateAttribution == null) this.dateAttribution = LocalDateTime.now();
    }
}
