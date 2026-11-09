package com.pharmacopee.ladafura.Models;

import java.time.LocalDateTime;

import org.hibernate.annotations.Immutable;

import com.pharmacopee.ladafura.enums.OrigineAction;
import com.pharmacopee.ladafura.enums.TypeActionAudit;

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
 * Trace de sécurité des opérations sensibles, en ajout seul. Écrite dans la même transaction que l'opération.
 * Ne jamais y enregistrer de jeton, de mot de passe ni de donnée personnelle superflue. Voir tracabilite.puml.
 */
@Entity
@Immutable
@Table(name = "journal_audit",
    indexes = {
        @Index(name = "idx_journal_audit_pharmacopee_date", columnList = "pharmacopee_id, date_action"),
        @Index(name = "idx_journal_audit_entite", columnList = "type_entite, entite_id")
    })
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JournalAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Null pour une action de niveau plateforme. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pharmacopee_id")
    private Pharmacopee pharmacopee;

    /** Null si l'action vient du système. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utilisateur_id")
    private Utilisateur utilisateur;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private TypeActionAudit action;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrigineAction origine;

    @Column(nullable = false, length = 100)
    private String typeEntite;

    private Long entiteId;

    /** Valeur avant l'opération, en JSON. */
    @Column(columnDefinition = "TEXT")
    private String ancienneValeur;

    /** Valeur après l'opération, en JSON. */
    @Column(columnDefinition = "TEXT")
    private String nouvelleValeur;

    @Column(length = 500)
    private String description;

    @Column(length = 45)
    private String adresseIp;

    @Column(nullable = false)
    private LocalDateTime dateAction;

    @PrePersist
    public void prePersist() {
        if (this.dateAction == null) this.dateAction = LocalDateTime.now();
    }
}
