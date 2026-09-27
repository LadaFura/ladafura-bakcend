package com.pharmacopee.ladafura.Models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "compositions_produits")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompositionProduit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double quantite;

    private String unite;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plante_id", nullable = false)
    private Plante plante;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "produit_id", nullable = false)
    @JsonIgnore
    private Produit produit;
}
