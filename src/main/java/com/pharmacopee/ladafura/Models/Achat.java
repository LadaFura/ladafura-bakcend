package com.pharmacopee.ladafura.Models;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.pharmacopee.ladafura.enums.StatutAchat;

import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Achat de marchandises. Le fournisseur est facultatif. Le stock n'est modifié qu'à la validation.
 * Le montant payé et le reste à payer sont calculés à partir des {@link ReglementAchat} : jamais stockés.
 * Le service vérifie que le fournisseur appartient à la pharmacopée de l'achat.
 */
@Entity
@Table(name = "achats",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_achat_pharmacopee_numero", columnNames = {"pharmacopee_id", "numero_achat"})
    },
    indexes = {
        @Index(name = "idx_achats_pharmacopee_statut_date", columnList = "pharmacopee_id, statut, date_achat"),
        @Index(name = "idx_achats_fournisseur", columnList = "fournisseur_id")
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Achat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Tenant propriétaire de la donnée. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pharmacopee_id", nullable = false)
    private Pharmacopee pharmacopee;

    /** Facultatif : un achat peut être enregistré sans fournisseur. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fournisseur_id")
    private Fournisseur fournisseur;

    @Column(nullable = false, length = 30)
    private String numeroAchat;

    @Column(nullable = false)
    private LocalDateTime dateAchat;

    /** Net à payer : somme des lignes moins la remise globale. */
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal montantTotal;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal montantRemise;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatutAchat statut;

    @Column(columnDefinition = "TEXT")
    private String observation;

    @Column(nullable = false)
    private LocalDateTime dateCreation;

    private LocalDateTime dateModification;

    private LocalDateTime dateValidation;

    private LocalDateTime dateAnnulation;

    @Column(length = 500)
    private String motifAnnulation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "enregistre_par_id", nullable = false)
    private MembrePharmacopee enregistrePar;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "valide_par_id")
    private MembrePharmacopee validePar;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "annule_par_id")
    private MembrePharmacopee annulePar;

    @OneToMany(mappedBy = "achat", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<LigneAchat> lignes = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        if (this.statut == null) this.statut = StatutAchat.BROUILLON;
        if (this.montantRemise == null) this.montantRemise = BigDecimal.ZERO;
        if (this.montantTotal == null) this.montantTotal = BigDecimal.ZERO;
        if (this.dateAchat == null) this.dateAchat = LocalDateTime.now();
        if (this.dateCreation == null) this.dateCreation = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.dateModification = LocalDateTime.now();
    }

    public void ajouterLigne(LigneAchat ligne) {
        ligne.setAchat(this);
        this.lignes.add(ligne);
    }

    /** Recalcule le net à payer ; à appeler tant que l'achat est en brouillon. */
    public void recalculerMontants() {
        BigDecimal somme = BigDecimal.ZERO;
        for (LigneAchat ligne : this.lignes) {
            ligne.calculerSousTotal();
            if (ligne.getSousTotal() != null) {
                somme = somme.add(ligne.getSousTotal());
            }
        }
        BigDecimal remise = this.montantRemise != null ? this.montantRemise : BigDecimal.ZERO;
        this.montantTotal = somme.subtract(remise);
    }

    public boolean estBrouillon() {
        return this.statut == StatutAchat.BROUILLON;
    }

    public boolean estValide() {
        return this.statut == StatutAchat.VALIDE;
    }
}
