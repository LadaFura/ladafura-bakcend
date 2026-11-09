package com.pharmacopee.ladafura.Models;

import java.time.LocalDate;
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
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Lot de produit, facultatif. Si des lots existent pour une ligne de stock, la somme de leurs
 * quantités restantes doit être égale à la quantité en stock (règle portée par le service de stock).
 */
@Entity
@Table(name = "lots",
    indexes = {
        @Index(name = "idx_lots_disponibilite_peremption", columnList = "disponibilite_produit_id, date_peremption"),
        @Index(name = "idx_lots_pharmacopee", columnList = "pharmacopee_id")
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Lot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Tenant propriétaire de la donnée. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pharmacopee_id", nullable = false)
    private Pharmacopee pharmacopee;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "disponibilite_produit_id", nullable = false)
    private DisponibiliteProduit disponibiliteProduit;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ligne_achat_id", unique = true)
    private LigneAchat ligneAchat;

    @Column(length = 50)
    private String numeroLot;

    private LocalDate datePeremption;

    @Column(nullable = false)
    private Integer quantiteInitiale;

    @Column(nullable = false)
    private Integer quantiteRestante;

    @Column(nullable = false)
    private LocalDateTime dateReception;

    @PrePersist
    public void prePersist() {
        if (this.quantiteRestante == null) this.quantiteRestante = this.quantiteInitiale;
        if (this.dateReception == null) this.dateReception = LocalDateTime.now();
    }

    public boolean estPerime(LocalDate date) {
        return this.datePeremption != null && this.datePeremption.isBefore(date);
    }
}
