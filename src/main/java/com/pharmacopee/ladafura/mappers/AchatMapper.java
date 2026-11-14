package com.pharmacopee.ladafura.mappers;

import java.math.BigDecimal;
import java.util.List;

import com.pharmacopee.ladafura.Models.Achat;
import com.pharmacopee.ladafura.Models.Fournisseur;
import com.pharmacopee.ladafura.Models.FournisseurProduit;
import com.pharmacopee.ladafura.Models.LigneAchat;
import com.pharmacopee.ladafura.Models.ReglementAchat;
import com.pharmacopee.ladafura.dto.pharmacopee.achat.AchatResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.achat.LigneAchatResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.achat.ReglementAchatResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.fournisseur.FournisseurProduitResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.fournisseur.FournisseurResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.fournisseur.HistoriqueAchatFournisseurResponse;

/** Conversions entité vers DTO des fournisseurs et des achats, sans accès à la base. */
public final class AchatMapper {

    private AchatMapper() {
    }

    public static FournisseurResponse fournisseur(Fournisseur f) {
        return FournisseurResponse.builder().id(f.getId()).nom(f.getNom()).typeFournisseur(f.getTypeFournisseur())
                .telephone(f.getTelephone()).email(f.getEmail()).adresse(f.getAdresse()).description(f.getDescription())
                .statut(f.getStatut()).dateCreation(f.getDateCreation()).build();
    }

    public static FournisseurProduitResponse fournisseurProduit(FournisseurProduit f) {
        return FournisseurProduitResponse.builder().id(f.getId()).produitId(f.getProduit().getId()).produitNom(f.getProduit().getNom())
                .referenceFournisseur(f.getReferenceFournisseur()).prixIndicatif(f.getPrixIndicatif())
                .delaiApprovisionnement(f.getDelaiApprovisionnement()).disponible(f.getDisponible()).observation(f.getObservation()).build();
    }

    public static HistoriqueAchatFournisseurResponse historique(LigneAchat l) {
        return HistoriqueAchatFournisseurResponse.builder().achatId(l.getAchat().getId()).numeroAchat(l.getAchat().getNumeroAchat())
                .dateAchat(l.getAchat().getDateAchat()).produitId(l.getProduit().getId()).produitNom(l.getProduit().getNom())
                .quantite(l.getQuantite()).prixUnitaire(l.getPrixUnitaire()).sousTotal(l.getSousTotal()).build();
    }

    public static AchatResponse achat(Achat a, BigDecimal montantPaye, List<LigneAchatResponse> lignes) {
        Fournisseur fournisseur = a.getFournisseur();
        return AchatResponse.builder()
                .id(a.getId())
                .numeroAchat(a.getNumeroAchat())
                .dateAchat(a.getDateAchat())
                .fournisseurId(fournisseur != null ? fournisseur.getId() : null)
                .fournisseurNom(fournisseur != null ? fournisseur.getNom() : null)
                .statut(a.getStatut())
                .montantTotal(a.getMontantTotal())
                .montantRemise(a.getMontantRemise())
                .montantPaye(montantPaye)
                .resteAPayer(a.getMontantTotal().subtract(montantPaye))
                .observation(a.getObservation())
                .dateValidation(a.getDateValidation())
                .dateAnnulation(a.getDateAnnulation())
                .motifAnnulation(a.getMotifAnnulation())
                .lignes(lignes)
                .build();
    }

    public static LigneAchatResponse ligne(LigneAchat l) {
        return LigneAchatResponse.builder().id(l.getId()).produitId(l.getProduit().getId()).produitNom(l.getProduit().getNom())
                .quantite(l.getQuantite()).prixUnitaire(l.getPrixUnitaire()).remise(l.getRemise()).sousTotal(l.getSousTotal())
                .numeroLot(l.getNumeroLot()).datePeremption(l.getDatePeremption()).build();
    }

    public static ReglementAchatResponse reglement(ReglementAchat r) {
        return ReglementAchatResponse.builder().id(r.getId()).achatId(r.getAchat().getId()).montant(r.getMontant())
                .dateReglement(r.getDateReglement()).methode(r.getMethode()).reference(r.getReference()).observation(r.getObservation())
                .statut(r.getStatut()).dateAnnulation(r.getDateAnnulation()).motifAnnulation(r.getMotifAnnulation()).build();
    }
}
