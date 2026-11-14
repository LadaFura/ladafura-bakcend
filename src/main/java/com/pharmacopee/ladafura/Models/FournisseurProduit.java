package com.pharmacopee.ladafura.Models;

import java.math.BigDecimal;

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
 * Produit qu'un fournisseur est susceptible de proposer (classe d'association facultative, purement informative).
 * Ce n'est pas une commande. La pharmacopée se déduit du fournisseur.
 */
@Entity
@Table(name = "fournisseurs_produits",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_fournisseur_produit", columnNames = {"fournisseur_id", "produit_id"})
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FournisseurProduit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fournisseur_id", nullable = false)
    private Fournisseur fournisseur;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "produit_id", nullable = false)
    private Produit produit;

    @Column(length = 100)
    private String referenceFournisseur;

    @Column(precision = 15, scale = 2)
    private BigDecimal prixIndicatif;

    /** Délai en jours. */
    private Integer delaiApprovisionnement;

    @Column(nullable = false)
    private Boolean disponible;

    @Column(columnDefinition = "TEXT")
    private String observation;

    @PrePersist
    public void prePersist() {
        if (this.disponible == null) this.disponible = true;
    }
}
