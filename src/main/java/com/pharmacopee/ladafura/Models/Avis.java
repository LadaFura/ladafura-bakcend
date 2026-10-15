package com.pharmacopee.ladafura.Models;

import com.pharmacopee.ladafura.enums.StatutAvis;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "avis")
@Getter
@Setter
public class Avis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Min(1)
    @Max(5)
    @Column(nullable = false)
    private Integer note;

    @Column(columnDefinition = "TEXT")
    private String commentaire;

    @Column(nullable = false)
    private LocalDateTime dateAvis;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutAvis statut;

    @Column(columnDefinition = "TEXT")
    private String reponseOfficine;

    private LocalDateTime dateReponse;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utilisateur_id", nullable = false)
    private Utilisateur utilisateur;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pharmacopee_id", nullable = false)
    private Pharmacopee pharmacopee;

    public Avis() {
        this.dateAvis = LocalDateTime.now();
        this.statut = StatutAvis.EN_ATTENTE;
    }

    @Builder
    public Avis(Long id, Integer note, String commentaire, LocalDateTime dateAvis, StatutAvis statut, String reponseOfficine, LocalDateTime dateReponse, Utilisateur utilisateur, Pharmacopee pharmacopee) {
        this.id = id;
        this.note = note;
        this.commentaire = commentaire;
        this.dateAvis = dateAvis != null ? dateAvis : LocalDateTime.now();
        this.statut = statut != null ? statut : StatutAvis.EN_ATTENTE;
        this.reponseOfficine = reponseOfficine;
        this.dateReponse = dateReponse;
        this.utilisateur = utilisateur;
        this.pharmacopee = pharmacopee;
    }

    @PrePersist
    public void onCreate() {
        if (this.dateAvis == null) {
            this.dateAvis = LocalDateTime.now();
        }
        if (this.statut == null) {
            this.statut = StatutAvis.EN_ATTENTE;
        }
    }
}
