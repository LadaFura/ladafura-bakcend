package com.pharmacopee.ladafura.Models;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "favoris")
@Getter
@Setter
public class Favori {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime dateAjout;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utilisateur_id", nullable = false)
    private Utilisateur utilisateur;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plante_id")
    private Plante plante;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "produit_id")
    private Produit produit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pharmacopee_id")
    private Pharmacopee pharmacopee;

    public Favori() {
        this.dateAjout = LocalDateTime.now();
    }

    @Builder
    public Favori(Long id, LocalDateTime dateAjout, Utilisateur utilisateur, Plante plante, Produit produit, Pharmacopee pharmacopee) {
        this.id = id;
        this.dateAjout = dateAjout != null ? dateAjout : LocalDateTime.now();
        this.utilisateur = utilisateur;
        this.plante = plante;
        this.produit = produit;
        this.pharmacopee = pharmacopee;
    }

    @PrePersist
    public void onCreate() {
        if (this.dateAjout == null) {
            this.dateAjout = LocalDateTime.now();
        }
    }
}
