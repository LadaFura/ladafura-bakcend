package com.pharmacopee.ladafura.Models;

import java.time.LocalDateTime;

import com.pharmacopee.ladafura.enums.TypeCaisse;

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
 * Caisse physique ou compte de trésorerie (Mobile Money, banque) d'une pharmacopée.
 * Le solde est calculé à partir des {@link OperationCaisse} : jamais stocké.
 * Une caisse ayant des opérations est désactivée, jamais supprimée.
 */
@Entity
@Table(name = "caisses",
    indexes = {
        @Index(name = "idx_caisses_pharmacopee_type", columnList = "pharmacopee_id, type")
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Caisse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Tenant propriétaire de la donnée. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pharmacopee_id", nullable = false)
    private Pharmacopee pharmacopee;

    @Column(nullable = false, length = 100)
    private String nom;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TypeCaisse type;

    /** Numéro Mobile Money ou bancaire. */
    @Column(length = 100)
    private String referenceCompte;

    private String description;

    @Column(nullable = false)
    private Boolean actif;

    @Column(nullable = false)
    private LocalDateTime dateCreation;

    @PrePersist
    public void prePersist() {
        if (this.type == null) this.type = TypeCaisse.PHYSIQUE;
        if (this.actif == null) this.actif = true;
        if (this.dateCreation == null) this.dateCreation = LocalDateTime.now();
    }

    public boolean estPhysique() {
        return this.type == TypeCaisse.PHYSIQUE;
    }
}
