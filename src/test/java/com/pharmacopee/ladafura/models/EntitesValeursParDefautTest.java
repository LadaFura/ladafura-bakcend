package com.pharmacopee.ladafura.models;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.pharmacopee.ladafura.Models.Abonnement;
import com.pharmacopee.ladafura.Models.Caisse;
import com.pharmacopee.ladafura.Models.DemandeContrat;
import com.pharmacopee.ladafura.Models.Depense;
import com.pharmacopee.ladafura.Models.Fournisseur;
import com.pharmacopee.ladafura.Models.FournisseurProduit;
import com.pharmacopee.ladafura.Models.Lot;
import com.pharmacopee.ladafura.Models.OffreAbonnement;
import com.pharmacopee.ladafura.Models.PaiementAbonnement;
import com.pharmacopee.ladafura.Models.PaiementVente;
import com.pharmacopee.ladafura.Models.ReglementAchat;
import com.pharmacopee.ladafura.Models.SessionCaisse;
import com.pharmacopee.ladafura.enums.StatutAbonnement;
import com.pharmacopee.ladafura.enums.StatutDemandeContrat;
import com.pharmacopee.ladafura.enums.StatutEcriture;
import com.pharmacopee.ladafura.enums.StatutFournisseur;
import com.pharmacopee.ladafura.enums.StatutPaiement;
import com.pharmacopee.ladafura.enums.StatutSessionCaisse;
import com.pharmacopee.ladafura.enums.TypeCaisse;

class EntitesValeursParDefautTest {

    @Test
    @DisplayName("offre : XOF, active, 7 jours de grâce par défaut")
    void offre() {
        OffreAbonnement offre = new OffreAbonnement();
        offre.prePersist();

        assertThat(offre.getDevise()).isEqualTo("XOF");
        assertThat(offre.getActif()).isTrue();
        assertThat(offre.getJoursGrace()).isEqualTo(OffreAbonnement.JOURS_GRACE_PAR_DEFAUT);
        assertThat(offre.getDateCreation()).isNotNull();
    }

    @Test
    @DisplayName("abonnement : en attente de paiement, sans renouvellement automatique")
    void abonnement() {
        Abonnement abonnement = new Abonnement();
        abonnement.prePersist();

        assertThat(abonnement.getStatut()).isEqualTo(StatutAbonnement.EN_ATTENTE_PAIEMENT);
        assertThat(abonnement.getRenouvellementAutomatique()).isFalse();
        assertThat(abonnement.getDateSouscription()).isNotNull();
        assertThat(abonnement.getLimites()).isEmpty();
    }

    @Test
    @DisplayName("paiement d'abonnement : déclaré (EN_ATTENTE) par défaut")
    void paiementAbonnement() {
        PaiementAbonnement paiement = new PaiementAbonnement();
        paiement.prePersist();

        assertThat(paiement.getStatut()).isEqualTo(StatutPaiement.EN_ATTENTE);
        assertThat(paiement.getDevise()).isEqualTo("XOF");
    }

    @Test
    @DisplayName("demande de contrat : NOUVELLE par défaut")
    void demandeContrat() {
        DemandeContrat demande = new DemandeContrat();
        demande.prePersist();

        assertThat(demande.getStatut()).isEqualTo(StatutDemandeContrat.NOUVELLE);
        assertThat(demande.getDateDemande()).isNotNull();
    }

    @Test
    @DisplayName("fournisseur : ACTIF par défaut ; proposition de produit disponible par défaut")
    void fournisseur() {
        Fournisseur fournisseur = new Fournisseur();
        fournisseur.prePersist();
        FournisseurProduit proposition = new FournisseurProduit();
        proposition.prePersist();

        assertThat(fournisseur.getStatut()).isEqualTo(StatutFournisseur.ACTIF);
        assertThat(fournisseur.estActif()).isTrue();
        assertThat(proposition.getDisponible()).isTrue();
    }

    @Test
    @DisplayName("écritures financières : VALIDE par défaut")
    void ecritures() {
        ReglementAchat reglement = new ReglementAchat();
        reglement.prePersist();
        PaiementVente paiement = new PaiementVente();
        paiement.prePersist();
        Depense depense = new Depense();
        depense.prePersist();

        assertThat(reglement.getStatut()).isEqualTo(StatutEcriture.VALIDE);
        assertThat(paiement.getStatut()).isEqualTo(StatutEcriture.VALIDE);
        assertThat(depense.getStatut()).isEqualTo(StatutEcriture.VALIDE);
    }

    @Test
    @DisplayName("caisse : physique et active par défaut")
    void caisse() {
        Caisse caisse = new Caisse();
        caisse.prePersist();

        assertThat(caisse.getType()).isEqualTo(TypeCaisse.PHYSIQUE);
        assertThat(caisse.estPhysique()).isTrue();
        assertThat(caisse.getActif()).isTrue();
    }

    @Test
    @DisplayName("session de caisse : ouverte avec un fond nul par défaut")
    void session() {
        SessionCaisse session = new SessionCaisse();
        session.prePersist();

        assertThat(session.getStatut()).isEqualTo(StatutSessionCaisse.OUVERTE);
        assertThat(session.getFondOuverture()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("lot : la quantité restante part de la quantité initiale")
    void lot() {
        Lot lot = Lot.builder().quantiteInitiale(40).build();
        lot.prePersist();

        assertThat(lot.getQuantiteRestante()).isEqualTo(40);
        assertThat(lot.getDateReception()).isNotNull();
    }
}
