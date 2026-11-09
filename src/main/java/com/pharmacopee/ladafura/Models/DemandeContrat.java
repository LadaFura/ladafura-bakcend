package com.pharmacopee.ladafura.Models;

import java.time.LocalDateTime;

import com.pharmacopee.ladafura.enums.StatutDemandeContrat;

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
 * Demande de contrat personnalisé. Ne crée jamais d'abonnement automatiquement.
 * Pharmacopée nulle : demande d'un prospect.
 */
@Entity
@Table(name = "demandes_contrat",
    indexes = {
        @Index(name = "idx_demandes_contrat_statut", columnList = "statut")
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DemandeContrat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pharmacopee_id")
    private Pharmacopee pharmacopee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "demandeur_id")
    private Utilisateur demandeur;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "traite_par_id")
    private Utilisateur traitePar;

    private Integer dureeSouhaiteeMois;

    @Column(nullable = false)
    private LocalDateTime dateDemande;

    @Column(nullable = false)
    private String nomContact;

    @Column(nullable = false, length = 30)
    private String telephone;

    private String email;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String descriptionBesoin;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatutDemandeContrat statut;

    @Column(columnDefinition = "TEXT")
    private String observation;

    private LocalDateTime dateTraitement;

    @PrePersist
    public void prePersist() {
        if (this.statut == null) this.statut = StatutDemandeContrat.NOUVELLE;
        if (this.dateDemande == null) this.dateDemande = LocalDateTime.now();
    }
}
