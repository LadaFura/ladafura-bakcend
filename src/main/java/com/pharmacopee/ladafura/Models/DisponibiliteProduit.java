package com.pharmacopee.ladafura.Models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "disponibilites_produits")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DisponibiliteProduit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Builder.Default
    @Column(nullable = false)
    private Boolean disponible = true;

    @Builder.Default
    private Integer quantiteStock = 0;

    @Builder.Default
    @Column(nullable = false)
    private LocalDateTime dateMiseAJour = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pharmacopee_id", nullable = false)
    private Pharmacopee pharmacopee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "produit_id", nullable = false)
    private Produit produit;

    @PrePersist
    @PreUpdate
    public void onUpdate() {
        this.dateMiseAJour = LocalDateTime.now();
    }
}
