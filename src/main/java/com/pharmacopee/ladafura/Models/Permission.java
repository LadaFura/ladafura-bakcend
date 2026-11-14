package com.pharmacopee.ladafura.Models;

import java.time.LocalDateTime;

import com.pharmacopee.ladafura.enums.PorteePermission;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Permission élémentaire (ex. {@code VENTES_CREER}, {@code STOCK_AJUSTER}).
 * Le catalogue est global et alimenté par les scripts de migration :
 * il n'est ni créé ni modifié par les gérants de pharmacopée.
 */
@Entity
@Table(name = "permissions", indexes = {
    @Index(name = "idx_permissions_portee", columnList = "portee"),
    @Index(name = "idx_permissions_module", columnList = "module")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Permission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Code technique unique, utilisé dans les contrôles d'accès. */
    @Column(nullable = false, unique = true, length = 100)
    private String code;

    @Column(nullable = false)
    private String libelle;

    @Column(columnDefinition = "TEXT")
    private String description;

    /** Regroupement fonctionnel (MEMBRES, VENTES, STOCK, CAISSE, COMPTABILITE…). */
    @Column(nullable = false, length = 50)
    private String module;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PorteePermission portee;

    @Column(nullable = false)
    private LocalDateTime dateCreation;

    @PrePersist
    public void prePersist() {
        if (this.dateCreation == null) {
            this.dateCreation = LocalDateTime.now();
        }
    }
}
