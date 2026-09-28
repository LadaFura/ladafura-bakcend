package com.pharmacopee.ladafura.mappers;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Component;

import com.pharmacopee.ladafura.Models.Avis;
import com.pharmacopee.ladafura.Models.Commande;
import com.pharmacopee.ladafura.Models.Favori;
import com.pharmacopee.ladafura.Models.Notification;
import com.pharmacopee.ladafura.Models.Paiement;
import com.pharmacopee.ladafura.Models.Utilisateur;
import com.pharmacopee.ladafura.dto.population.historique.PopulationDepenseMensuelleDto;
import com.pharmacopee.ladafura.dto.population.historique.PopulationDepenseMethodeDto;
import com.pharmacopee.ladafura.dto.population.historique.PopulationDepensesSyntheseResponse;
import com.pharmacopee.ladafura.dto.population.historique.PopulationJournalActiviteItem;
import com.pharmacopee.ladafura.dto.population.historique.PopulationReleveActiviteResponse;
import com.pharmacopee.ladafura.enums.TypeEvenementHistorique;

@Component
public class PopulationHistoriqueMapper {

    public PopulationJournalActiviteItem mapCommande(Commande c) {
        if (c == null) return null;

        int nbArticles = c.getLignes() != null ? c.getLignes().size() : 0;
        String phNom = c.getPharmacopee() != null ? c.getPharmacopee().getNom() : "";

        return PopulationJournalActiviteItem.builder()
                .id("CMD-" + c.getId())
                .type(TypeEvenementHistorique.COMMANDE)
                .titre("Commande " + c.getStatut().name())
                .description("Commande " + c.getNumero() + " (" + nbArticles + " articles) auprès de " + phNom)
                .dateEvenement(c.getDateCommande())
                .reference(c.getNumero())
                .statut(c.getStatut().name())
                .montant(c.getMontantTotal())
                .lienDetail("/api/v1/population/commandes/" + c.getId())
                .build();
    }

    public PopulationJournalActiviteItem mapPaiement(Paiement p) {
        if (p == null) return null;

        String cmdNumero = p.getCommande() != null ? p.getCommande().getNumero() : "";
        Long cmdId = p.getCommande() != null ? p.getCommande().getId() : null;

        return PopulationJournalActiviteItem.builder()
                .id("PAY-" + p.getId())
                .type(TypeEvenementHistorique.PAIEMENT)
                .titre("Paiement " + p.getStatut().name())
                .description("Règlement de " + p.getMontant() + " FCFA par " + p.getMethode().name() + " pour la commande " + cmdNumero)
                .dateEvenement(p.getDatePaiement())
                .reference(p.getReference())
                .statut(p.getStatut().name())
                .montant(p.getMontant())
                .lienDetail(cmdId != null ? "/api/v1/population/commandes/" + cmdId : null)
                .build();
    }

    public PopulationJournalActiviteItem mapAvis(Avis a) {
        if (a == null) return null;

        String prodNom = a.getProduit() != null ? a.getProduit().getNom() : "Produit";

        return PopulationJournalActiviteItem.builder()
                .id("AVIS-" + a.getId())
                .type(TypeEvenementHistorique.AVIS)
                .titre("Avis " + a.getNote() + "/5 étoiles")
                .description("Évaluation sur le produit " + prodNom + " : \"" + a.getCommentaire() + "\"")
                .dateEvenement(a.getDateAvis())
                .reference(prodNom)
                .statut(a.getStatut().name())
                .montant(null)
                .lienDetail("/api/v1/population/avis/" + a.getId())
                .build();
    }

    public PopulationJournalActiviteItem mapFavori(Favori f) {
        if (f == null) return null;

        String desc = "Élément ajouté aux favoris";
        String ref = "";
        if (f.getPlante() != null) {
            desc = "Plante médicinale : " + f.getPlante().getNomScientifique();
            ref = f.getPlante().getNomScientifique();
        } else if (f.getProduit() != null) {
            desc = "Produit : " + f.getProduit().getNom();
            ref = f.getProduit().getNom();
        } else if (f.getPharmacopee() != null) {
            desc = "Officine : " + f.getPharmacopee().getNom();
            ref = f.getPharmacopee().getNom();
        }

        return PopulationJournalActiviteItem.builder()
                .id("FAV-" + f.getId())
                .type(TypeEvenementHistorique.FAVORI)
                .titre("Mise en favori")
                .description(desc)
                .dateEvenement(f.getDateAjout())
                .reference(ref)
                .statut("ENREGISTRE")
                .montant(null)
                .lienDetail("/api/v1/population/favoris")
                .build();
    }

    public PopulationJournalActiviteItem mapNotification(Notification n) {
        if (n == null) return null;

        return PopulationJournalActiviteItem.builder()
                .id("NOTIF-" + n.getId())
                .type(TypeEvenementHistorique.NOTIFICATION)
                .titre(n.getTitre())
                .description(n.getMessage())
                .dateEvenement(n.getDateNotification())
                .reference(n.getReferenceId())
                .statut(Boolean.TRUE.equals(n.getLue()) ? "LUE" : "NON_LUE")
                .montant(null)
                .lienDetail(n.getLien())
                .build();
    }

    public PopulationDepensesSyntheseResponse toSyntheseDepenses(
            Double totalDepense,
            long totalTransactions,
            Double panierMoyen,
            List<PopulationDepenseMensuelleDto> depensesMois,
            List<PopulationDepenseMethodeDto> depensesMethodes) {

        return PopulationDepensesSyntheseResponse.builder()
                .montantTotalDepense(totalDepense)
                .nombreTotalTransactions(totalTransactions)
                .panierMoyen(panierMoyen)
                .depensesParMois(depensesMois)
                .depensesParMethode(depensesMethodes)
                .build();
    }

    public PopulationReleveActiviteResponse toReleveActivite(
            Utilisateur user,
            long totalCommandes,
            long totalCommandesLivrees,
            Double totalDepense,
            long totalAvis,
            long totalFavoris,
            List<PopulationJournalActiviteItem> dernieresActivites) {

        String nomComplet = (user.getPrenom() != null ? user.getPrenom() : "") + " " +
                            (user.getNom() != null ? user.getNom() : "");

        return PopulationReleveActiviteResponse.builder()
                .utilisateurId(user.getId())
                .nomComplet(nomComplet.trim())
                .email(user.getEmail())
                .dateGeneration(LocalDateTime.now())
                .totalCommandes(totalCommandes)
                .totalCommandesLivrees(totalCommandesLivrees)
                .totalDepense(totalDepense)
                .totalAvis(totalAvis)
                .totalFavoris(totalFavoris)
                .dernieresActivites(dernieresActivites)
                .build();
    }
}
