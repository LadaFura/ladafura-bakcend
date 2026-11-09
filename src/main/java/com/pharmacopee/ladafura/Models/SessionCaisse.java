package com.pharmacopee.ladafura.Models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.pharmacopee.ladafura.enums.StatutSessionCaisse;

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
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Période d'ouverture d'une caisse physique. Au plus une session OUVERTE par caisse,
 * garantie par une colonne générée UNIQUE (migration). Les montants sont figés à la clôture.
 */
@Entity
@Table(name = "sessions_caisse",
    indexes = {
        @Index(name = "idx_sessions_caisse_statut", columnList = "caisse_id, statut")
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SessionCaisse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "caisse_id", nullable = false)
    private Caisse caisse;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ouverte_par_id", nullable = false)
    private MembrePharmacopee ouvertePar;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cloture_par_id")
    private MembrePharmacopee cloturePar;

    @Column(nullable = false)
    private LocalDateTime dateOuverture;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal fondOuverture;

    private LocalDateTime dateCloture;

    @Column(precision = 15, scale = 2)
    private BigDecimal montantTheorique;

    @Column(precision = 15, scale = 2)
    private BigDecimal montantConstate;

    @Column(precision = 15, scale = 2)
    private BigDecimal ecart;

    @Column(columnDefinition = "TEXT")
    private String commentaire;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatutSessionCaisse statut;

    @PrePersist
    public void prePersist() {
        if (this.statut == null) this.statut = StatutSessionCaisse.OUVERTE;
        if (this.fondOuverture == null) this.fondOuverture = BigDecimal.ZERO;
        if (this.dateOuverture == null) this.dateOuverture = LocalDateTime.now();
    }

    public boolean estOuverte() {
        return this.statut == StatutSessionCaisse.OUVERTE;
    }

    /** Fige les montants et l'écart ; la justification d'un écart est contrôlée par le service. */
    public void cloturer(BigDecimal theorique, BigDecimal constate, MembrePharmacopee membre) {
        this.montantTheorique = theorique;
        this.montantConstate = constate;
        this.ecart = constate.subtract(theorique);
        this.cloturePar = membre;
        this.dateCloture = LocalDateTime.now();
        this.statut = StatutSessionCaisse.CLOTUREE;
    }
}
