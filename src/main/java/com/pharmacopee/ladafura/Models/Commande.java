package com.pharmacopee.ladafura.Models;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.pharmacopee.ladafura.enums.StatutCommande;

import jakarta.persistence.CascadeType;
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

@Entity
@Table(name = "commandes")
@Getter
@Setter
public class Commande {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String numero;

    @Column(nullable = false)
    private LocalDateTime dateCommande;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutCommande statut;

    @Column(nullable = false)
    private Double totalProduit;

    @Column(nullable = false)
    private Double montantLivraison;

    @Column(nullable = false)
    private Double montantTotal;

    private String adresseLivraison;

    private LocalDateTime dateMiseAJour;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utilisateur_id", nullable = false)
    private Utilisateur utilisateur;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pharmacopee_id")
    private Pharmacopee pharmacopee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mode_retrait_id")
    private ModeRetrait modeRetrait;

    @OneToMany(mappedBy = "commande", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LigneCommande> lignes;

    @OneToOne(mappedBy = "commande", cascade = CascadeType.ALL)
    private Paiement paiement;

    public Commande() {
        this.dateCommande = LocalDateTime.now();
        this.statut = StatutCommande.EN_ATTENTE;
        this.totalProduit = 0.0;
        this.montantLivraison = 0.0;
        this.montantTotal = 0.0;
        this.lignes = new ArrayList<>();
    }

    @Builder
    public Commande(Long id, String numero, LocalDateTime dateCommande, StatutCommande statut, Double totalProduit, Double montantLivraison, Double montantTotal, String adresseLivraison, LocalDateTime dateMiseAJour, Utilisateur utilisateur, Pharmacopee pharmacopee, ModeRetrait modeRetrait, List<LigneCommande> lignes, Paiement paiement) {
        this.id = id;
        this.numero = numero;
        this.dateCommande = dateCommande != null ? dateCommande : LocalDateTime.now();
        this.statut = statut != null ? statut : StatutCommande.EN_ATTENTE;
        this.totalProduit = totalProduit != null ? totalProduit : 0.0;
        this.montantLivraison = montantLivraison != null ? montantLivraison : 0.0;
        this.montantTotal = montantTotal != null ? montantTotal : 0.0;
        this.adresseLivraison = adresseLivraison;
        this.dateMiseAJour = dateMiseAJour;
        this.utilisateur = utilisateur;
        this.pharmacopee = pharmacopee;
        this.modeRetrait = modeRetrait;
        this.lignes = lignes != null ? lignes : new ArrayList<>();
        this.paiement = paiement;
    }

    @PrePersist
    public void onCreate() {
        if (this.dateCommande == null) {
            this.dateCommande = LocalDateTime.now();
        }
        this.dateMiseAJour = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        this.dateMiseAJour = LocalDateTime.now();
    }
}
