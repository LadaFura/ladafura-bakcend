package com.pharmacopee.ladafura.Models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.pharmacopee.ladafura.enums.MethodePaiement;
import com.pharmacopee.ladafura.enums.StatutEcriture;

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
 * Règlement, éventuellement partiel, d'un achat validé. Jamais modifié : annulé (statut ANNULE).
 * Crée exactement une {@link OperationCaisse} de sortie.
 */
@Entity
@Table(name = "reglements_achat",
    indexes = {
        @Index(name = "idx_reglements_achat_achat", columnList = "achat_id")
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReglementAchat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "achat_id", nullable = false)
    private Achat achat;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal montant;

    @Column(nullable = false)
    private LocalDateTime dateReglement;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MethodePaiement methode;

    @Column(length = 50)
    private String reference;

    @Column(columnDefinition = "TEXT")
    private String observation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private StatutEcriture statut;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "enregistre_par_id", nullable = false)
    private MembrePharmacopee enregistrePar;

    private LocalDateTime dateAnnulation;

    @Column(length = 500)
    private String motifAnnulation;

    @PrePersist
    public void prePersist() {
        if (this.statut == null) this.statut = StatutEcriture.VALIDE;
        if (this.dateReglement == null) this.dateReglement = LocalDateTime.now();
    }
}
