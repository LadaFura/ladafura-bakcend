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
 * Association entre un rôle de pharmacopée et une permission.
 * Règle métier (vérifiée côté service) : seule une permission de portée
 * {@code PHARMACOPEE} peut être associée à un rôle de pharmacopée.
 */
@Entity
@Table(name = "roles_pharmacopee_permissions", uniqueConstraints = {
    @UniqueConstraint(name = "uk_role_pharmacopee_permission", columnNames = {"role_pharmacopee_id", "permission_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RolePharmacopeePermission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "role_pharmacopee_id", nullable = false)
    private RolePharmacopee rolePharmacopee;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "permission_id", nullable = false)
    private Permission permission;

    @Column(nullable = false)
    private LocalDateTime dateAttribution;

    @PrePersist
    public void prePersist() {
        if (this.dateAttribution == null) this.dateAttribution = LocalDateTime.now();
    }
}
