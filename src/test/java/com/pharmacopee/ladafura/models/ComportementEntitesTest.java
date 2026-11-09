package com.pharmacopee.ladafura.models;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.pharmacopee.ladafura.Models.Abonnement;
import com.pharmacopee.ladafura.Models.FactureAbonnement;
import com.pharmacopee.ladafura.Models.LimiteAbonnement;
import com.pharmacopee.ladafura.Models.Lot;
import com.pharmacopee.ladafura.Models.MembrePharmacopee;
import com.pharmacopee.ladafura.Models.MouvementStock;
import com.pharmacopee.ladafura.Models.OperationCaisse;
import com.pharmacopee.ladafura.Models.SessionCaisse;
import com.pharmacopee.ladafura.enums.SensOperation;
import com.pharmacopee.ladafura.enums.StatutAbonnement;
import com.pharmacopee.ladafura.enums.StatutSessionCaisse;

class ComportementEntitesTest {

    @Test
    @DisplayName("quota : la limite de 1 utilisateur refuse un deuxième membre actif")
    void quotaOffreGratuite() {
        LimiteAbonnement limite = LimiteAbonnement.builder().valeurMaximale(1).illimite(false).build();

        assertThat(limite.autorise(0)).isTrue();
        assertThat(limite.autorise(1)).isFalse();
    }

    @Test
    @DisplayName("quota : une limite illimitée autorise toujours la création")
    void quotaIllimite() {
        LimiteAbonnement limite = LimiteAbonnement.builder().illimite(true).build();

        assertThat(limite.autorise(1_000_000)).isTrue();
    }

    @Test
    @DisplayName("quota : une limite sans valeur ni illimité refuse par prudence")
    void quotaIncomplet() {
        LimiteAbonnement limite = LimiteAbonnement.builder().illimite(false).build();

        assertThat(limite.autorise(0)).isFalse();
    }

    @Test
    @DisplayName("abonnement : en grâce, il reste en vigueur mais en lecture seule")
    void abonnementEnGrace() {
        Abonnement abonnement = Abonnement.builder().statut(StatutAbonnement.EN_GRACE).build();

        assertThat(abonnement.estEnVigueur()).isTrue();
        assertThat(abonnement.autoriseEcriture()).isFalse();
    }

    @Test
    @DisplayName("abonnement : seul ACTIF autorise les écritures")
    void abonnementActif() {
        Abonnement actif = Abonnement.builder().statut(StatutAbonnement.ACTIF).build();
        Abonnement enAttente = Abonnement.builder().statut(StatutAbonnement.EN_ATTENTE_PAIEMENT).build();

        assertThat(actif.autoriseEcriture()).isTrue();
        assertThat(enAttente.estEnVigueur()).isFalse();
        assertThat(enAttente.autoriseEcriture()).isFalse();
    }

    @Test
    @DisplayName("facture : TVA de 18 % appliquée au montant HT")
    void factureAvecTaxe() {
        FactureAbonnement facture = FactureAbonnement.builder()
                .montantHT(new BigDecimal("10000.00"))
                .tauxTaxe(new BigDecimal("18.00"))
                .build();

        facture.appliquerTaxe();

        assertThat(facture.getMontantTaxe()).isEqualByComparingTo("1800.00");
        assertThat(facture.getMontantTTC()).isEqualByComparingTo("11800.00");
    }

    @Test
    @DisplayName("facture : à 0 % par défaut, le TTC est égal au HT")
    void factureSansTaxe() {
        FactureAbonnement facture = FactureAbonnement.builder().montantHT(new BigDecimal("5000.00")).build();

        facture.prePersist();

        assertThat(facture.getMontantTaxe()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(facture.getMontantTTC()).isEqualByComparingTo("5000.00");
    }

    @Test
    @DisplayName("session : la clôture fige les montants et calcule l'écart")
    void clotureSession() {
        SessionCaisse session = SessionCaisse.builder().statut(StatutSessionCaisse.OUVERTE).build();
        MembrePharmacopee membre = new MembrePharmacopee();

        session.cloturer(new BigDecimal("75000.00"), new BigDecimal("74500.00"), membre);

        assertThat(session.getEcart()).isEqualByComparingTo("-500.00");
        assertThat(session.estOuverte()).isFalse();
        assertThat(session.getCloturePar()).isSameAs(membre);
        assertThat(session.getDateCloture()).isNotNull();
    }

    @Test
    @DisplayName("opération de caisse : une sortie a un montant signé négatif")
    void montantSigne() {
        OperationCaisse entree = OperationCaisse.builder().sens(SensOperation.ENTREE).montant(new BigDecimal("2000.00")).build();
        OperationCaisse sortie = OperationCaisse.builder().sens(SensOperation.SORTIE).montant(new BigDecimal("2000.00")).build();

        assertThat(entree.montantSigne()).isEqualByComparingTo("2000.00");
        assertThat(sortie.montantSigne()).isEqualByComparingTo("-2000.00");
    }

    @Test
    @DisplayName("mouvement de stock : stockApres = stockAvant + quantité signée")
    void mouvementStock() {
        MouvementStock sortie = MouvementStock.builder().stockAvant(10).quantite(-3).build();

        sortie.prePersist();

        assertThat(sortie.getStockApres()).isEqualTo(7);
        assertThat(sortie.getDateMouvement()).isNotNull();
    }

    @Test
    @DisplayName("lot : périmé seulement après sa date de péremption")
    void lotPerime() {
        LocalDate aujourdhui = LocalDate.of(2026, 11, 9);
        Lot perime = Lot.builder().datePeremption(aujourdhui.minusDays(1)).build();
        Lot limite = Lot.builder().datePeremption(aujourdhui).build();
        Lot sansDate = new Lot();

        assertThat(perime.estPerime(aujourdhui)).isTrue();
        assertThat(limite.estPerime(aujourdhui)).isFalse();
        assertThat(sansDate.estPerime(aujourdhui)).isFalse();
    }
}
