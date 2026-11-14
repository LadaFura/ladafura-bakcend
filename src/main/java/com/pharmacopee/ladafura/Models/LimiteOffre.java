package com.pharmacopee.ladafura.Models;

import com.pharmacopee.ladafura.enums.ModeCalculLimite;
import com.pharmacopee.ladafura.enums.PeriodiciteLimite;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
 * Catalogue des limites applicables aux offres (MAX_UTILISATEURS_ACTIFS, MAX_PRODUITS, MAX_VENTES_MENSUELLES).
 * La valeur dépend de l'offre : voir {@link ConfigurationLimiteOffre}.
 */
@Entity
@Table(name = "limites_offre")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LimiteOffre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String codeLimite;

    @Column(nullable = false)
    private String nom;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PeriodiciteLimite periodicite;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ModeCalculLimite modeCalcul;

    @PrePersist
    public void prePersist() {
        if (this.periodicite == null) this.periodicite = PeriodiciteLimite.AUCUNE;
        if (this.modeCalcul == null) this.modeCalcul = ModeCalculLimite.COMPTAGE;
    }
}
