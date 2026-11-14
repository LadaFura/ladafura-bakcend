package com.pharmacopee.ladafura.Models;

import java.math.BigDecimal;
import java.time.LocalDate;

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
 * Ligne d'un achat. Quantité entière : uniquement des produits finis du catalogue.
 * Le lot n'est créé à la validation que si numeroLot ou datePeremption est renseigné.
 */
@Entity
@Table(name = "lignes_achat",
    indexes = {
        @Index(name = "idx_lignes_achat_achat", columnList = "achat_id"),
        @Index(name = "idx_lignes_achat_produit", columnList = "produit_id")
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LigneAchat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "achat_id", nullable = false)
    private Achat achat;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "produit_id", nullable = false)
    private Produit produit;

    @Column(nullable = false)
    private Integer quantite;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal prixUnitaire;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal remise;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal sousTotal;

    @Column(length = 50)
    private String numeroLot;

    private LocalDate datePeremption;

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
