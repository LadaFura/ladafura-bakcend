package com.pharmacopee.ladafura.Models;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Rôle utilisable au sein d'une pharmacopée.
 * <ul>
 *   <li>{@code pharmacopee == null} et {@code systeme == true} : rôle système partagé
 *       (GERANT, MANAGER, VENDEUR_CAISSIER, COMPTABLE, GESTIONNAIRE_STOCK, GESTIONNAIRE_ACHATS),
 *       en lecture seule pour les gérants ;</li>
 *   <li>{@code pharmacopee != null} : rôle personnalisé créé par le gérant propriétaire,
 *       visible et utilisable uniquement dans sa pharmacopée.</li>
 * </ul>
 * L'unicité du code des rôles système (pharmacopee NULL) est garantie par le service,
 * MySQL n'appliquant pas les contraintes UNIQUE sur les valeurs NULL.
 */
@Entity
@Table(name = "roles_pharmacopee",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_role_pharmacopee_code", columnNames = {"pharmacopee_id", "code"})
    },
    indexes = {
        @Index(name = "idx_roles_pharmacopee_systeme", columnList = "systeme")
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RolePharmacopee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String code;

    @Column(nullable = false)
    private String libelle;

    @Column(columnDefinition = "TEXT")
    private String description;

    /** Pharmacopée propriétaire du rôle personnalisé ; null pour un rôle système. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pharmacopee_id")
    private Pharmacopee pharmacopee;

    @Column(nullable = false)
    private Boolean systeme;

    @Column(nullable = false)
    private Boolean actif;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cree_par_id")
    private Utilisateur creePar;

    @Column(nullable = false)
    private LocalDateTime dateCreation;

    @Column(name = "date_mise_a_jour")
    private LocalDateTime dateMiseAJour;

    @PrePersist
    public void prePersist() {
        if (this.systeme == null) this.systeme = false;
        if (this.actif == null) this.actif = true;
        if (this.dateCreation == null) this.dateCreation = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.dateMiseAJour = LocalDateTime.now();
    }

    /** Vrai si le rôle est utilisable dans la pharmacopée donnée (système ou personnalisé de ce tenant). */
    public boolean estDisponiblePour(Long pharmacopeeId) {
        return this.pharmacopee == null || this.pharmacopee.getId().equals(pharmacopeeId);
    }
}
