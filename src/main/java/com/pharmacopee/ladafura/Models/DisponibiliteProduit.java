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
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "disponibilites_produits")
@Getter
@Setter
public class DisponibiliteProduit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Boolean disponible;

    private Integer quantiteStock;

    private Double prix;

    @Column(nullable = false)
    private LocalDateTime dateMiseAJour;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pharmacopee_id", nullable = false)
    private Pharmacopee pharmacopee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "produit_id", nullable = false)
    private Produit produit;

    public DisponibiliteProduit() {
        this.disponible = true;
        this.quantiteStock = 0;
        this.dateMiseAJour = LocalDateTime.now();
    }

    @Builder
    public DisponibiliteProduit(Long id, Boolean disponible, Integer quantiteStock, Double prix, LocalDateTime dateMiseAJour, Pharmacopee pharmacopee, Produit produit) {
        this.id = id;
        this.disponible = disponible != null ? disponible : true;
        this.quantiteStock = quantiteStock != null ? quantiteStock : 0;
        this.prix = prix;
        this.dateMiseAJour = dateMiseAJour != null ? dateMiseAJour : LocalDateTime.now();
        this.pharmacopee = pharmacopee;
        this.produit = produit;
    }

    @PrePersist
    @PreUpdate
    public void onUpdate() {
        this.dateMiseAJour = LocalDateTime.now();
    }
}
