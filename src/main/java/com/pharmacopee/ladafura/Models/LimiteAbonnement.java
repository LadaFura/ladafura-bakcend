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
 * Instantané d'une limite au moment de la souscription : c'est cette valeur qui fait foi pour les quotas.
 */
@Entity
@Table(name = "limites_abonnement",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_limite_abonnement", columnNames = {"abonnement_id", "limite_offre_id"})
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LimiteAbonnement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "abonnement_id", nullable = false)
    private Abonnement abonnement;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "limite_offre_id", nullable = false)
    private LimiteOffre limiteOffre;

    @Column(nullable = false, length = 50)
    private String codeLimite;

    private Integer valeurMaximale;

    @Column(nullable = false)
    private Boolean illimite;

    @PrePersist
    public void prePersist() {
        if (this.illimite == null) this.illimite = false;
    }

    /** Vrai si une création supplémentaire reste permise pour la consommation actuelle donnée. */
    public boolean autorise(long consommationActuelle) {
        return Boolean.TRUE.equals(this.illimite)
                || (this.valeurMaximale != null && consommationActuelle < this.valeurMaximale);
    }
}
