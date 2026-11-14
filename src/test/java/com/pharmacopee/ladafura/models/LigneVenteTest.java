package com.pharmacopee.ladafura.models;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.pharmacopee.ladafura.Models.LigneVente;

class LigneVenteTest {

    @Test
    @DisplayName("sousTotal = quantité × prix unitaire − remise")
    void calculeLeSousTotalAvecRemise() {
        LigneVente ligne = LigneVente.builder()
                .quantite(2)
                .prixUnitaire(new BigDecimal("3500.00"))
                .remise(new BigDecimal("500.00"))
                .build();

        ligne.calculerSousTotal();

        assertThat(ligne.getSousTotal()).isEqualByComparingTo("6500.00");
    }

    @Test
    @DisplayName("une remise absente est traitée comme zéro")
    void remiseAbsenteVautZero() {
        LigneVente ligne = LigneVente.builder()
                .quantite(4)
                .prixUnitaire(new BigDecimal("1000.00"))
                .build();

        ligne.calculerSousTotal();

        assertThat(ligne.getSousTotal()).isEqualByComparingTo("4000.00");
    }
}
