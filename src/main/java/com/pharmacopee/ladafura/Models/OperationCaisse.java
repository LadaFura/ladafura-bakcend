package com.pharmacopee.ladafura.Models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.hibernate.annotations.Immutable;

import com.pharmacopee.ladafura.enums.NatureOperationCaisse;
import com.pharmacopee.ladafura.enums.SensOperation;

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
 * Toute entrée ou sortie d'argent. Immuable : une correction se fait par contre-passation.
 * Au plus une origine renseignée (paiement de vente, paiement de commande, règlement d'achat, dépense) ;
 * aucune pour un apport, un retrait, un transfert, une recette diverse ou un ajustement.
 */
@Entity
@Immutable
@Table(name = "operations_caisse",
    indexes = {
        @Index(name = "idx_operations_caisse_date", columnList = "caisse_id, date_operation"),
        @Index(name = "idx_operations_pharmacopee_date", columnList = "pharmacopee_id, date_operation"),
        @Index(name = "idx_operations_session", columnList = "session_caisse_id")
    })
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OperationCaisse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Tenant propriétaire de la donnée. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pharmacopee_id", nullable = false)
    private Pharmacopee pharmacopee;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "caisse_id", nullable = false)
    private Caisse caisse;

    /** Obligatoire pour une caisse physique. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_caisse_id")
    private SessionCaisse session;

    /** Null pour une opération du système. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "effectue_par_id")
    private MembrePharmacopee effectuePar;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paiement_vente_id")
    private PaiementVente paiementVente;

    /** Paiement d'une commande en ligne. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paiement_commande_id")
    private Paiement paiement;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reglement_achat_id")
    private ReglementAchat reglementAchat;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "depense_id")
    private Depense depense;

    /** Opération contre-passée, ou opération jumelle d'un transfert. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operation_liee_id")
    private OperationCaisse operationLiee;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private SensOperation sens;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private NatureOperationCaisse nature;

    /** Toujours positif ; le sens porte le signe. */
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal montant;

    private String libelle;

    @Column(nullable = false)
    private LocalDateTime dateOperation;

    @PrePersist
    public void prePersist() {
        if (this.dateOperation == null) this.dateOperation = LocalDateTime.now();
    }

    /** Montant positif pour une entrée, négatif pour une sortie. */
    public BigDecimal montantSigne() {
        return this.sens == SensOperation.ENTREE ? this.montant : this.montant.negate();
    }
}
