package com.pharmacopee.ladafura.Models;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.pharmacopee.ladafura.enums.StatutAbonnement;

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
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Abonnement d'une pharmacopée à une offre, avec l'instantané des conditions souscrites.
 *
 * La règle « un seul abonnement ACTIF ou EN_GRACE par pharmacopée » est garantie par une
 * colonne générée UNIQUE (migration), le service restant responsable des transitions.
 * Voir membres-roles-abonnements.puml.
 */
@Entity
@Table(name = "abonnements",
    indexes = {
        @Index(name = "idx_abonnements_pharmacopee_statut", columnList = "pharmacopee_id, statut"),
        @Index(name = "idx_abonnements_statut_date_fin", columnList = "statut, date_fin")
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Abonnement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Tenant propriétaire de la donnée. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pharmacopee_id", nullable = false)
    private Pharmacopee pharmacopee;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "offre_abonnement_id", nullable = false)
    private OffreAbonnement offre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StatutAbonnement statut;

    /** Null tant que le paiement n'est pas confirmé. */
    private LocalDateTime dateDebut;

    /** Null pour l'offre gratuite. */
    private LocalDateTime dateFin;

    private LocalDateTime dateFinGrace;

    @Column(nullable = false)
    private LocalDateTime dateSouscription;

    private LocalDateTime dateRenouvellement;

    @Column(nullable = false)
    private Boolean renouvellementAutomatique;

    private LocalDateTime dateConsentementRenouvellement;

    private LocalDateTime dateAnnulation;

    @Column(length = 500)
    private String motifAnnulation;

    /** Instantané : code de l'offre à la souscription. */
    @Column(nullable = false, length = 50)
    private String codeOffreSouscrite;

    @Column(nullable = false)
    private String nomOffreSouscrite;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal prixSouscrit;

    @Column(nullable = false, length = 3)
    private String deviseSouscrite;

    private Integer dureeMoisSouscrite;

    @Column(nullable = false)
    private Integer joursGraceSouscrits;

    /** Null pour l'offre gratuite créée automatiquement ou un contrat créé par l'administrateur. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "souscrit_par_id")
    private MembrePharmacopee souscritPar;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cree_par_admin_id")
    private Utilisateur creeParAdmin;

    /** Chaîne des abonnements successifs. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "abonnement_precedent_id")
    private Abonnement abonnementPrecedent;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "demande_contrat_id", unique = true)
    private DemandeContrat demandeContrat;

    @Column(nullable = false)
    private LocalDateTime dateCreation;

    private LocalDateTime dateMiseAJour;

    @OneToMany(mappedBy = "abonnement", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<LimiteAbonnement> limites = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        if (this.statut == null) this.statut = StatutAbonnement.EN_ATTENTE_PAIEMENT;
        if (this.renouvellementAutomatique == null) this.renouvellementAutomatique = false;
        if (this.joursGraceSouscrits == null) this.joursGraceSouscrits = 0;
        if (this.dateSouscription == null) this.dateSouscription = LocalDateTime.now();
        if (this.dateCreation == null) this.dateCreation = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.dateMiseAJour = LocalDateTime.now();
    }

    public void ajouterLimite(LimiteAbonnement limite) {
        limite.setAbonnement(this);
        this.limites.add(limite);
    }

    /** Vrai si cet abonnement est celui qui s'applique actuellement à la pharmacopée. */
    public boolean estEnVigueur() {
        return this.statut == StatutAbonnement.ACTIF || this.statut == StatutAbonnement.EN_GRACE;
    }

    /** Vrai si les opérations d'écriture sont permises (EN_GRACE et SUSPENDU : lecture seule). */
    public boolean autoriseEcriture() {
        return this.statut == StatutAbonnement.ACTIF;
    }
}
