package com.pharmacopee.ladafura.Models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.pharmacopee.ladafura.enums.TypeOffre;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Offre commerciale du catalogue SaaS, gérée uniquement par l'administrateur.
 * Une modification de l'offre n'affecte jamais les abonnements existants (voir {@link Abonnement}).
 * Une offre déjà souscrite est désactivée, jamais supprimée.
 */
@Entity
@Table(name = "offres_abonnement")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OffreAbonnement {

    /** Devise par défaut des montants LADAFURA (franc CFA). */
    public static final String DEVISE_PAR_DEFAUT = "XOF";

    public static final int JOURS_GRACE_PAR_DEFAUT = 7;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false)
    private String nom;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TypeOffre typeOffre;

    /** Null pour l'offre gratuite ; au plus 12 mois dans le catalogue. */
    private Integer dureeMois;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal prix;

    @Column(nullable = false, length = 3)
    private String devise;

    /** Durée de la période de grâce après l'échéance. */
    @Column(nullable = false)
    private Integer joursGrace;

    @Column(nullable = false)
    private Boolean actif;

    @Column(nullable = false)
    private LocalDateTime dateCreation;

    private LocalDateTime dateModification;

    @PrePersist
    public void prePersist() {
        if (this.devise == null) this.devise = DEVISE_PAR_DEFAUT;
        if (this.joursGrace == null) this.joursGrace = JOURS_GRACE_PAR_DEFAUT;
        if (this.actif == null) this.actif = true;
        if (this.dateCreation == null) this.dateCreation = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.dateModification = LocalDateTime.now();
    }

    public boolean estGratuite() {
        return this.typeOffre == TypeOffre.GRATUITE;
    }
}
