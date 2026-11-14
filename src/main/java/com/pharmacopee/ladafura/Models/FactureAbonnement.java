package com.pharmacopee.ladafura.Models;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

import com.pharmacopee.ladafura.enums.StatutFacture;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
 * Facture d'abonnement, émise à la confirmation du paiement. Jamais modifiée : annulée puis réémise.
 */
@Entity
@Table(name = "factures_abonnement")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FactureAbonnement {

    private static final BigDecimal CENT = new BigDecimal("100");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "abonnement_id", nullable = false)
    private Abonnement abonnement;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paiement_abonnement_id", unique = true)
    private PaiementAbonnement paiement;

    @Column(nullable = false, unique = true, length = 30)
    private String numeroFacture;

    @Column(nullable = false)
    private LocalDateTime dateEmission;

    /** Taux en pourcentage, figé à l'émission. */
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal tauxTaxe;

    @Column(name = "montant_ht", nullable = false, precision = 15, scale = 2)
    private BigDecimal montantHT;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal montantTaxe;

    @Column(name = "montant_ttc", nullable = false, precision = 15, scale = 2)
    private BigDecimal montantTTC;

    @Column(nullable = false, length = 3)
    private String devise;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatutFacture statut;

    @PrePersist
    public void prePersist() {
        if (this.dateEmission == null) this.dateEmission = LocalDateTime.now();
        if (this.devise == null) this.devise = OffreAbonnement.DEVISE_PAR_DEFAUT;
        if (this.statut == null) this.statut = StatutFacture.EMISE;
        if (this.tauxTaxe == null) this.tauxTaxe = BigDecimal.ZERO;
        if (this.montantTaxe == null || this.montantTTC == null) appliquerTaxe();
    }

    /** Calcule la taxe et le TTC à partir du HT et du taux. */
    public void appliquerTaxe() {
        if (this.montantHT == null || this.tauxTaxe == null) return;
        this.montantTaxe = this.montantHT.multiply(this.tauxTaxe).divide(CENT, 2, RoundingMode.HALF_UP);
        this.montantTTC = this.montantHT.add(this.montantTaxe);
    }
}
