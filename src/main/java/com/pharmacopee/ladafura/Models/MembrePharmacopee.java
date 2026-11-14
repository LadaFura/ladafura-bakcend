package com.pharmacopee.ladafura.Models;

import java.time.LocalDateTime;

import com.pharmacopee.ladafura.enums.MotifSuspensionMembre;
import com.pharmacopee.ladafura.enums.StatutMembre;

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
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Appartenance d'un utilisateur à une pharmacopée (tenant).
 * <p>
 * Un utilisateur peut être membre de plusieurs pharmacopées ; une pharmacopée
 * possède plusieurs membres (propriétaire, gérants, vendeurs, caissiers…).
 * Les droits du membre dans la pharmacopée proviennent de ses
 * {@link AttributionRolePharmacopee}.
 */
@Entity
@Table(name = "membres_pharmacopee",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_membre_utilisateur_pharmacopee", columnNames = {"utilisateur_id", "pharmacopee_id"})
    },
    indexes = {
        @Index(name = "idx_membres_pharmacopee_statut", columnList = "pharmacopee_id, statut"),
        @Index(name = "idx_membres_utilisateur_statut", columnList = "utilisateur_id, statut")
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MembrePharmacopee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "utilisateur_id", nullable = false)
    private Utilisateur utilisateur;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pharmacopee_id", nullable = false)
    private Pharmacopee pharmacopee;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatutMembre statut;

    /** Gérant propriétaire de la pharmacopée : seul habilité à gérer membres, rôles et permissions. */
    @Column(nullable = false)
    private Boolean estProprietaire;

    /** Intitulé libre affiché (ex. « Vendeuse », « Gérant adjoint »). */
    @Column(length = 100)
    private String poste;

    /** Données professionnelles reprises de l'ancien modèle Praticien. */
    private String numeroAgrement;

    private String specialite;

    /** Utilisateur ayant invité ou ajouté ce membre (null pour le propriétaire initial). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invite_par_id")
    private Utilisateur invitePar;

    private LocalDateTime dateInvitation;

    /** Au-delà de cette date, l'invitation ne peut plus être acceptée. */
    private LocalDateTime dateExpirationInvitation;

    /** Date à laquelle le membre est devenu ACTIF. */
    private LocalDateTime dateAdhesion;

    /** Date à laquelle le membre a quitté la pharmacopée (statut RETIRE). */
    private LocalDateTime dateDepart;

    /** Renseigné lorsque le statut est SUSPENDU. */
    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private MotifSuspensionMembre motifSuspension;

    @Column(nullable = false)
    private LocalDateTime dateCreation;

    @Column(name = "date_mise_a_jour")
    private LocalDateTime dateMiseAJour;

    @PrePersist
    public void prePersist() {
        if (this.statut == null) this.statut = StatutMembre.INVITE;
        if (this.estProprietaire == null) this.estProprietaire = false;
        if (this.dateCreation == null) this.dateCreation = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.dateMiseAJour = LocalDateTime.now();
    }

    public boolean estActif() {
        return this.statut == StatutMembre.ACTIF;
    }
}
