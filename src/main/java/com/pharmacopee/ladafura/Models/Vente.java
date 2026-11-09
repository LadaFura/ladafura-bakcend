package com.pharmacopee.ladafura.Models;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.pharmacopee.ladafura.enums.StatutVente;

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
 * Vente au comptoir, distincte de la {@link Commande} en ligne.
 * Le montant payé et le reste à payer sont calculés à partir des {@link PaiementVente} : jamais stockés.
 * Voir ventes-caisses.puml.
 */
@Entity
@Table(name = "ventes",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_vente_pharmacopee_numero", columnNames = {"pharmacopee_id", "numero_vente"})
    },
    indexes = {
        @Index(name = "idx_ventes_pharmacopee_statut_date", columnList = "pharmacopee_id, statut, date_vente"),
        @Index(name = "idx_ventes_client", columnList = "client_id")
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Tenant propriétaire de la donnée. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pharmacopee_id", nullable = false)
    private Pharmacopee pharmacopee;

    /** Obligatoire seulement si un reste à payer subsiste à la validation. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id")
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vendeur_id", nullable = false)
    private MembrePharmacopee vendeur;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "valide_par_id")
    private MembrePharmacopee validePar;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "annule_par_id")
    private MembrePharmacopee annulePar;

    @Column(nullable = false, length = 30)
    private String numeroVente;

    @Column(nullable = false)
    private LocalDateTime dateVente;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatutVente statut;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal montantBrut;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal montantRemise;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal montantTotal;

    @Column(columnDefinition = "TEXT")
    private String observation;

    @Column(nullable = false)
    private LocalDateTime dateCreation;

    private LocalDateTime dateModification;

    private LocalDateTime dateValidation;

    private LocalDateTime dateAnnulation;

    @Column(length = 500)
    private String motifAnnulation;

    @OneToMany(mappedBy = "vente", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<LigneVente> lignes = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        if (this.statut == null) this.statut = StatutVente.EN_ATTENTE;
        if (this.montantBrut == null) this.montantBrut = BigDecimal.ZERO;
        if (this.montantRemise == null) this.montantRemise = BigDecimal.ZERO;
        if (this.montantTotal == null) this.montantTotal = BigDecimal.ZERO;
        if (this.dateVente == null) this.dateVente = LocalDateTime.now();
        if (this.dateCreation == null) this.dateCreation = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.dateModification = LocalDateTime.now();
    }

    public void ajouterLigne(LigneVente ligne) {
        ligne.setVente(this);
        this.lignes.add(ligne);
    }

    /** Recalcule le brut et le net ; à appeler tant que la vente est en attente. */
    public void recalculerMontants() {
        BigDecimal somme = BigDecimal.ZERO;
        for (LigneVente ligne : this.lignes) {
            ligne.calculerSousTotal();
            if (ligne.getSousTotal() != null) {
                somme = somme.add(ligne.getSousTotal());
            }
        }
        BigDecimal remise = this.montantRemise != null ? this.montantRemise : BigDecimal.ZERO;
        this.montantBrut = somme;
        this.montantTotal = somme.subtract(remise);
    }

    public boolean estEnAttente() {
        return this.statut == StatutVente.EN_ATTENTE;
    }

    public boolean estValidee() {
        return this.statut == StatutVente.VALIDEE;
    }
}
