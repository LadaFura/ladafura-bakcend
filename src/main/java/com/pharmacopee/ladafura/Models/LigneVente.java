package com.pharmacopee.ladafura.Models;

import java.math.BigDecimal;

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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Ligne d'une vente. Pointe vers la fiche de stock de la pharmacopée : on ne vend que ce qu'elle référence.
 * Le prix est recalculé tant que la vente est en attente, puis figé à la validation.
 */
@Entity
@Table(name = "lignes_vente",
    indexes = {
        @Index(name = "idx_lignes_vente_vente", columnList = "vente_id"),
        @Index(name = "idx_lignes_vente_disponibilite", columnList = "disponibilite_produit_id")
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LigneVente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vente_id", nullable = false)
    private Vente vente;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "disponibilite_produit_id", nullable = false)
    private DisponibiliteProduit disponibiliteProduit;

    @Column(nullable = false)
    private Integer quantite;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal prixUnitaire;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal remise;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal sousTotal;

    /** sousTotal = quantite × prixUnitaire − remise. */
    @PrePersist
    @PreUpdate
    public void calculerSousTotal() {
        if (this.remise == null) {
            this.remise = BigDecimal.ZERO;
        }
        if (this.quantite != null && this.prixUnitaire != null) {
            this.sousTotal = this.prixUnitaire.multiply(BigDecimal.valueOf(this.quantite)).subtract(this.remise);
        }
    }
}
