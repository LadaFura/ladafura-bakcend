package com.pharmacopee.ladafura.models;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.pharmacopee.ladafura.Models.LigneAchat;

class LigneAchatTest {

    @Test
    @DisplayName("sousTotal = quantité × prix unitaire − remise")
    void calculeLeSousTotalAvecRemise() {
        LigneAchat ligne = LigneAchat.builder()
                .quantite(20)
                .prixUnitaire(new BigDecimal("1500.00"))
                .remise(new BigDecimal("2000.00"))
                .build();

        ligne.calculerSousTotal();

        assertThat(ligne.getSousTotal()).isEqualByComparingTo("28000.00");
    }

    @Test
    @DisplayName("une remise absente est traitée comme zéro")
    void remiseAbsenteVautZero() {
        LigneAchat ligne = LigneAchat.builder()
                .quantite(3)
                .prixUnitaire(new BigDecimal("2500.00"))
                .build();

        ligne.calculerSousTotal();

        assertThat(ligne.getRemise()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(ligne.getSousTotal()).isEqualByComparingTo("7500.00");
    }

    @Test
    @DisplayName("sans quantité ou sans prix, le sous-total n'est pas calculé")
    void donneesIncompletes() {
        LigneAchat ligne = LigneAchat.builder().quantite(3).build();

        ligne.calculerSousTotal();

        assertThat(ligne.getSousTotal()).isNull();
    }
}
