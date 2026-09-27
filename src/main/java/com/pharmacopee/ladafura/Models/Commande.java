package com.pharmacopee.ladafura.Models;

import com.pharmacopee.ladafura.enums.StatutCommande;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "commandes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Commande {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String numero;

    @Builder.Default
    @Column(nullable = false)
    private LocalDateTime dateCommande = LocalDateTime.now();

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutCommande statut = StatutCommande.EN_ATTENTE;

    @Builder.Default
    @Column(nullable = false)
    private Double totalProduit = 0.0;

    @Builder.Default
    @Column(nullable = false)
    private Double montantLivraison = 0.0;

    @Builder.Default
    @Column(nullable = false)
    private Double montantTotal = 0.0;

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

    @Builder.Default
    @OneToMany(mappedBy = "commande", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LigneCommande> lignes = new ArrayList<>();

    @OneToOne(mappedBy = "commande", cascade = CascadeType.ALL)
    private Paiement paiement;

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
