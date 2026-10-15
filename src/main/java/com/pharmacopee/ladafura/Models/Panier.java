package com.pharmacopee.ladafura.Models;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "paniers")
@Getter
@Setter
public class Panier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime dateCreation;

    @Column(nullable = false)
    private LocalDateTime dateModification;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utilisateur_id", nullable = false, unique = true)
    private Utilisateur utilisateur;

    @OneToMany(mappedBy = "panier", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LignePanier> lignes;

    public Panier() {
        this.dateCreation = LocalDateTime.now();
        this.dateModification = LocalDateTime.now();
        this.lignes = new ArrayList<>();
    }

    @Builder
    public Panier(Long id, LocalDateTime dateCreation, LocalDateTime dateModification,
                  Utilisateur utilisateur, List<LignePanier> lignes) {
        this.id = id;
        this.dateCreation = dateCreation != null ? dateCreation : LocalDateTime.now();
        this.dateModification = dateModification != null ? dateModification : LocalDateTime.now();
        this.utilisateur = utilisateur;
        this.lignes = lignes != null ? lignes : new ArrayList<>();
    }

    @PrePersist
    public void onCreate() {
        this.dateCreation = LocalDateTime.now();
        this.dateModification = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        this.dateModification = LocalDateTime.now();
    }
}
