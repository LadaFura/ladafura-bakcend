package com.pharmacopee.ladafura.Models;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.pharmacopee.ladafura.enums.MethodePaiement;
import com.pharmacopee.ladafura.enums.StatutPaiement;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "paiements")
@Getter
@Setter
public class Paiement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Double montant;

    @Column(nullable = false)
    private LocalDateTime datePaiement;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MethodePaiement methode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutPaiement statut;

    private String reference;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "commande_id", nullable = false, unique = true)
    @JsonIgnore
    private Commande commande;

    public Paiement() {
        this.datePaiement = LocalDateTime.now();
        this.statut = StatutPaiement.EN_ATTENTE;
    }

    @Builder
    public Paiement(Long id, Double montant, LocalDateTime datePaiement, MethodePaiement methode,
                    StatutPaiement statut, String reference, Commande commande) {
        this.id = id;
        this.montant = montant;
        this.datePaiement = datePaiement != null ? datePaiement : LocalDateTime.now();
        this.methode = methode;
        this.statut = statut != null ? statut : StatutPaiement.EN_ATTENTE;
        this.reference = reference;
        this.commande = commande;
    }

    @PrePersist
    public void onCreate() {
        if (this.datePaiement == null) {
            this.datePaiement = LocalDateTime.now();
        }
    }
}
