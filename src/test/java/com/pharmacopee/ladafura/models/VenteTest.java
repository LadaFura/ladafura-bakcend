package com.pharmacopee.ladafura.models;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.pharmacopee.ladafura.Models.LigneVente;
import com.pharmacopee.ladafura.Models.Vente;

class VenteTest {

    private LigneVente ligne(int quantite, String prix, String remise) {
        return LigneVente.builder()
                .quantite(quantite)
                .prixUnitaire(new BigDecimal(prix))
                .remise(new BigDecimal(remise))
                .build();
    }

    @Test
    @DisplayName("le brut est la somme des lignes, le net en retire la remise globale")
    void recalculeBrutEtNet() {
        Vente vente = Vente.builder().montantRemise(new BigDecimal("500.00")).build();
        vente.ajouterLigne(ligne(2, "3000.00", "1000.00"));
        vente.ajouterLigne(ligne(1, "1500.00", "0.00"));

        vente.recalculerMontants();

        assertThat(vente.getMontantBrut()).isEqualByComparingTo("6500.00");
        assertThat(vente.getMontantTotal()).isEqualByComparingTo("6000.00");
    }

    @Test
    @DisplayName("une nouvelle vente est EN_ATTENTE et peut être anonyme")
    void nouvelleVenteEnAttente() {
        Vente vente = Vente.builder().numeroVente("VTE-2026-0001").build();

        vente.prePersist();

        assertThat(vente.estEnAttente()).isTrue();
        assertThat(vente.estValidee()).isFalse();
        assertThat(vente.getClient()).isNull();
    }
}
