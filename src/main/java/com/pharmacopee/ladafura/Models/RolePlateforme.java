package com.pharmacopee.ladafura.Models;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Rôle global de la plateforme LADAFURA (ADMINISTRATEUR, AGENT_COLLECTE, CITOYEN…).
 * Géré exclusivement par l'administrateur LADAFURA.
 */
@Entity
@Table(name = "roles_plateforme")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RolePlateforme {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false)
    private String libelle;

    @Column(columnDefinition = "TEXT")
    private String description;

    /** Rôle système : ni supprimable ni renommable. */
    @Column(nullable = false)
    private Boolean systeme;

    @Column(nullable = false)
    private Boolean actif;

    @Column(nullable = false)
    private LocalDateTime dateCreation;

    @PrePersist
    public void prePersist() {
        if (this.systeme == null) this.systeme = false;
        if (this.actif == null) this.actif = true;
        if (this.dateCreation == null) this.dateCreation = LocalDateTime.now();
    }
}
