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
 * Attribution d'un rôle de pharmacopée à un membre.
 * Un membre peut posséder plusieurs rôles dans la même pharmacopée.
 * Règle métier (vérifiée côté service) : le rôle doit être un rôle système
 * ou un rôle personnalisé de la pharmacopée du membre.
 */
@Entity
@Table(name = "attributions_roles_pharmacopee", uniqueConstraints = {
    @UniqueConstraint(name = "uk_attribution_role_pharmacopee", columnNames = {"membre_pharmacopee_id", "role_pharmacopee_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttributionRolePharmacopee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "membre_pharmacopee_id", nullable = false)
    private MembrePharmacopee membrePharmacopee;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "role_pharmacopee_id", nullable = false)
    private RolePharmacopee rolePharmacopee;

    /** Gérant ayant attribué le rôle (null pour les reprises de données). */
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
