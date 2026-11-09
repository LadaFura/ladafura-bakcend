package com.pharmacopee.ladafura.Models;

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
 * Valeur d'une limite pour une offre donnée (classe d'association OffreAbonnement / LimiteOffre).
 */
@Entity
@Table(name = "configurations_limites_offre",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_configuration_offre_limite", columnNames = {"offre_abonnement_id", "limite_offre_id"})
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConfigurationLimiteOffre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "offre_abonnement_id", nullable = false)
    private OffreAbonnement offre;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "limite_offre_id", nullable = false)
    private LimiteOffre limite;

    /** Null si la limite est illimitée. */
    private Integer valeurMaximale;

    @Column(nullable = false)
    private Boolean illimite;

    @PrePersist
    public void prePersist() {
        if (this.illimite == null) this.illimite = false;
    }
}
