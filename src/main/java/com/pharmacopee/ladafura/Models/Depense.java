package com.pharmacopee.ladafura.Models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.pharmacopee.ladafura.enums.CategorieDepense;
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
 * Charge d'exploitation (l'achat de marchandises passe par {@link Achat}). Toujours payée depuis une caisse
 * ou un compte : crée exactement une {@link OperationCaisse} de sortie. Jamais modifiée : annulée.
 */
@Entity
@Table(name = "depenses",
    indexes = {
        @Index(name = "idx_depenses_pharmacopee_date", columnList = "pharmacopee_id, date_depense")
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Depense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Tenant propriétaire de la donnée. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pharmacopee_id", nullable = false)
    private Pharmacopee pharmacopee;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "enregistre_par_id", nullable = false)
    private MembrePharmacopee enregistrePar;

    @Column(nullable = false)
    private String libelle;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CategorieDepense categorie;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal montant;

    @Column(nullable = false)
    private LocalDateTime dateDepense;

    private String beneficiaire;

    private String justificatifUrl;

    @Column(columnDefinition = "TEXT")
    private String observation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private StatutEcriture statut;

    @Column(nullable = false)
    private LocalDateTime dateCreation;

    private LocalDateTime dateAnnulation;

    @Column(length = 500)
    private String motifAnnulation;

    @PrePersist
    public void prePersist() {
        if (this.categorie == null) this.categorie = CategorieDepense.AUTRE;
        if (this.statut == null) this.statut = StatutEcriture.VALIDE;
        if (this.dateDepense == null) this.dateDepense = LocalDateTime.now();
        if (this.dateCreation == null) this.dateCreation = LocalDateTime.now();
    }
}
