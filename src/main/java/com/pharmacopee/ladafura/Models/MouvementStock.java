package com.pharmacopee.ladafura.Models;

import java.time.LocalDateTime;

import org.hibernate.annotations.Immutable;

import com.pharmacopee.ladafura.enums.TypeMouvementStock;

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

/**
 * Journal immuable des mouvements de stock : toute variation de quantité passe par un mouvement.
 * Correction uniquement par un mouvement inverse.
 */
@Entity
@Immutable
@Table(name = "mouvements_stock",
    indexes = {
        @Index(name = "idx_mouvements_pharmacopee_date", columnList = "pharmacopee_id, date_mouvement"),
        @Index(name = "idx_mouvements_disponibilite", columnList = "disponibilite_produit_id")
    })
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MouvementStock {

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lot_id")
    private Lot lot;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ligne_achat_id")
    private LigneAchat ligneAchat;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ligne_vente_id")
    private LigneVente ligneVente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ligne_commande_id")
    private LigneCommande ligneCommande;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TypeMouvementStock type;

    /** Quantité signée : positive en entrée, négative en sortie. */
    @Column(nullable = false)
    private Integer quantite;

    @Column(nullable = false)
    private Integer stockAvant;

    @Column(nullable = false)
    private Integer stockApres;

    private String motif;

    /** Null pour un mouvement du système (commande passée en ligne par un client). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "effectue_par_id")
    private MembrePharmacopee effectuePar;

    @Column(nullable = false)
    private LocalDateTime dateMouvement;

    @PrePersist
    public void prePersist() {
        if (this.dateMouvement == null) this.dateMouvement = LocalDateTime.now();
        if (this.stockApres == null && this.stockAvant != null && this.quantite != null) {
            this.stockApres = this.stockAvant + this.quantite;
        }
    }
}
