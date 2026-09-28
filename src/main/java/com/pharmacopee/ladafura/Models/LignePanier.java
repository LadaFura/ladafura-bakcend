package com.pharmacopee.ladafura.Models;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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

@Entity
@Table(name = "lignes_panier")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LignePanier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Builder.Default
    @Column(nullable = false)
    private Integer quantite = 1;

    @Column(nullable = false)
    private Double prixUnitaire;

    @Column(nullable = false)
    private Double sousTotal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "panier_id", nullable = false)
    @JsonIgnore
    private Panier panier;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "produit_id", nullable = false)
    private Produit produit;

    @PrePersist
    @PreUpdate
    public void calculateSousTotal() {
        if (prixUnitaire != null && quantite != null) {
            this.sousTotal = prixUnitaire * quantite;
        }
    }
}
