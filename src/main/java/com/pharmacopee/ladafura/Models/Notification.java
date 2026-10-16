package com.pharmacopee.ladafura.Models;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.pharmacopee.ladafura.enums.NiveauNotification;
import com.pharmacopee.ladafura.enums.TypeNotification;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "notifications")
@Getter
@Setter
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String titre;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeNotification type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NiveauNotification niveau;

    @Column(nullable = false)
    private Boolean lue;

    @Column(nullable = false)
    private LocalDateTime dateNotification;

    private LocalDateTime dateLecture;

    @Column(name = "type_entite_liee", length = 60)
    private String typeEntiteLiee;

    private String referenceId;

    private String lien;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pharmacopee_id")
    @JsonIgnore
    private Pharmacopee pharmacopee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utilisateur_id")
    @JsonIgnore
    private Utilisateur utilisateur;

    public Notification() {
        this.niveau = NiveauNotification.INFO;
        this.lue = false;
        this.dateNotification = LocalDateTime.now();
    }

    @Builder
    public Notification(Long id, String titre, String message, TypeNotification type, NiveauNotification niveau,
                        Boolean lue, LocalDateTime dateNotification, LocalDateTime dateLecture,
                        String typeEntiteLiee, String referenceId, String lien,
                        Pharmacopee pharmacopee, Utilisateur utilisateur) {
        this.id = id;
        this.titre = titre;
        this.message = message;
        this.type = type;
        this.niveau = niveau != null ? niveau : NiveauNotification.INFO;
        this.lue = lue != null ? lue : false;
        this.dateNotification = dateNotification != null ? dateNotification : LocalDateTime.now();
        this.dateLecture = dateLecture;
        this.typeEntiteLiee = typeEntiteLiee;
        this.referenceId = referenceId;
        this.lien = lien;
        this.pharmacopee = pharmacopee;
        this.utilisateur = utilisateur;
    }

    @PrePersist
    public void onCreate() {
        if (this.dateNotification == null) {
            this.dateNotification = LocalDateTime.now();
        }
        if (this.lue == null) {
            this.lue = false;
        }
        if (this.niveau == null) {
            this.niveau = NiveauNotification.INFO;
        }
    }

    public String getRelatedEntityType() {
        return this.typeEntiteLiee;
    }

    public String getRelatedEntityId() {
        return this.referenceId;
    }

    public Boolean getIsRead() {
        return this.lue;
    }

    public LocalDateTime getCreatedAt() {
        return this.dateNotification;
    }
}
