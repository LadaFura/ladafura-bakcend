package com.pharmacopee.ladafura.Models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.pharmacopee.ladafura.enums.ModePaiementAbonnement;
import com.pharmacopee.ladafura.enums.StatutPaiement;

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
 * Tentative de paiement d'un abonnement : déclarée par la pharmacopée, confirmée ou rejetée par l'administrateur.
 * Statuts : EN_ATTENTE = déclaré, REUSSI = confirmé, ECHOUE = rejeté.
 */
@Entity
@Table(name = "paiements_abonnement",
    indexes = {
        @Index(name = "idx_paiements_abonnement_statut", columnList = "statut"),
        @Index(name = "idx_paiements_abonnement_transaction", columnList = "mode_paiement, reference_transaction"),
        @Index(name = "idx_paiements_abonnement_abonnement", columnList = "abonnement_id")
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaiementAbonnement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "abonnement_id", nullable = false)
    private Abonnement abonnement;

    @Column(nullable = false, unique = true, length = 50)
    private String reference;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal montant;

    @Column(nullable = false, length = 3)
    private String devise;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ModePaiementAbonnement modePaiement;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatutPaiement statut;

    @Column(nullable = false)
    private LocalDateTime datePaiement;

    @Column(length = 100)
    private String referenceTransaction;

    /** Personne à joindre pour un paiement en espèces. */
    @Column(length = 150)
    private String contactNom;

    @Column(length = 30)
    private String contactTelephone;

    @Column(columnDefinition = "TEXT")
    private String observation;

    private LocalDateTime dateValidation;

    @Column(length = 500)
    private String motifRejet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "declare_par_id")
    private MembrePharmacopee declarePar;

    /** Administrateur ayant confirmé ou rejeté le paiement. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "valide_par_id")
    private Utilisateur validePar;

    @Column(nullable = false)
    private LocalDateTime dateCreation;

    @PrePersist
    public void prePersist() {
        if (this.statut == null) this.statut = StatutPaiement.EN_ATTENTE;
        if (this.devise == null) this.devise = OffreAbonnement.DEVISE_PAR_DEFAUT;
        if (this.datePaiement == null) this.datePaiement = LocalDateTime.now();
        if (this.dateCreation == null) this.dateCreation = LocalDateTime.now();
    }
}
