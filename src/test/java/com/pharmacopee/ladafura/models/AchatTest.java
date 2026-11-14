package com.pharmacopee.ladafura.models;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.pharmacopee.ladafura.Models.Achat;
import com.pharmacopee.ladafura.Models.LigneAchat;

class AchatTest {

    private LigneAchat ligne(int quantite, String prix) {
        return LigneAchat.builder().quantite(quantite).prixUnitaire(new BigDecimal(prix)).build();
    }

    @Test
    @DisplayName("le total net est la somme des lignes moins la remise globale")
    void recalculeLeTotalNet() {
        Achat achat = Achat.builder().montantRemise(new BigDecimal("1000.00")).build();
        achat.ajouterLigne(ligne(10, "500.00"));
        achat.ajouterLigne(ligne(2, "1500.00"));

        achat.recalculerMontants();

        assertThat(achat.getMontantTotal()).isEqualByComparingTo("7000.00");
    }

    @Test
    @DisplayName("sans remise ni ligne, le total est nul")
    void achatVide() {
        Achat achat = new Achat();

        achat.recalculerMontants();

        assertThat(achat.getMontantTotal()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(achat.getLignes()).isEmpty();
    }

    @Test
    @DisplayName("ajouterLigne rattache la ligne à l'achat")
    void ajouterLigneRattacheLeParent() {
        Achat achat = new Achat();
        LigneAchat ligne = ligne(1, "100.00");

        achat.ajouterLigne(ligne);

        assertThat(ligne.getAchat()).isSameAs(achat);
    }

    @Test
    @DisplayName("un achat peut être construit sans fournisseur")
    void fournisseurFacultatif() {
        Achat achat = Achat.builder().numeroAchat("ACH-2026-0001").build();

        achat.prePersist();

        assertThat(achat.getFournisseur()).isNull();
        assertThat(achat.estBrouillon()).isTrue();
    }
}
