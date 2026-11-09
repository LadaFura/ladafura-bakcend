package com.pharmacopee.ladafura.mappers;

import java.util.List;

import com.pharmacopee.ladafura.Models.Abonnement;
import com.pharmacopee.ladafura.Models.ConfigurationLimiteOffre;
import com.pharmacopee.ladafura.Models.DemandeContrat;
import com.pharmacopee.ladafura.Models.FactureAbonnement;
import com.pharmacopee.ladafura.Models.HistoriqueAbonnement;
import com.pharmacopee.ladafura.Models.OffreAbonnement;
import com.pharmacopee.ladafura.Models.PaiementAbonnement;
import com.pharmacopee.ladafura.dto.pharmacopee.abonnement.AbonnementResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.abonnement.DemandeContratResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.abonnement.EvenementAbonnementResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.abonnement.FactureAbonnementResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.abonnement.LimiteConsommationResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.abonnement.OffreResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.abonnement.PaiementAbonnementResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.abonnement.QuotaOffreResponse;

/** Conversions entité vers DTO du module abonnements, sans accès à la base. */
public final class AbonnementMapper {

    private AbonnementMapper() {
    }

    public static AbonnementResponse abonnement(Abonnement a, List<LimiteConsommationResponse> limites) {
        return AbonnementResponse.builder()
                .id(a.getId())
                .pharmacopeeId(a.getPharmacopee().getId())
                .pharmacopeeNom(a.getPharmacopee().getNom())
                .codeOffre(a.getCodeOffreSouscrite())
                .nomOffre(a.getNomOffreSouscrite())
                .prix(a.getPrixSouscrit())
                .devise(a.getDeviseSouscrite())
                .dureeMois(a.getDureeMoisSouscrite())
                .joursGrace(a.getJoursGraceSouscrits())
                .statut(a.getStatut())
                .dateDebut(a.getDateDebut())
                .dateFin(a.getDateFin())
                .dateFinGrace(a.getDateFinGrace())
                .dateSouscription(a.getDateSouscription())
                .renouvellementAutomatique(a.getRenouvellementAutomatique())
                .motifAnnulation(a.getMotifAnnulation())
                .limites(limites)
                .build();
    }

    public static OffreResponse offre(OffreAbonnement o, List<ConfigurationLimiteOffre> configurations) {
        return OffreResponse.builder()
                .id(o.getId())
                .code(o.getCode())
                .nom(o.getNom())
                .description(o.getDescription())
                .typeOffre(o.getTypeOffre())
                .dureeMois(o.getDureeMois())
                .prix(o.getPrix())
                .devise(o.getDevise())
                .joursGrace(o.getJoursGrace())
                .actif(o.getActif())
                .quotas(configurations.stream().map(c -> QuotaOffreResponse.builder()
                        .limiteId(c.getLimite().getId())
                        .codeLimite(c.getLimite().getCodeLimite())
                        .nom(c.getLimite().getNom())
                        .valeurMaximale(c.getValeurMaximale())
                        .illimite(c.getIllimite())
                        .build()).toList())
                .build();
    }

    public static PaiementAbonnementResponse paiement(PaiementAbonnement p) {
        return PaiementAbonnementResponse.builder()
                .id(p.getId())
                .abonnementId(p.getAbonnement().getId())
                .pharmacopeeId(p.getAbonnement().getPharmacopee().getId())
                .pharmacopeeNom(p.getAbonnement().getPharmacopee().getNom())
                .reference(p.getReference())
                .montant(p.getMontant())
                .devise(p.getDevise())
                .modePaiement(p.getModePaiement())
                .statut(p.getStatut())
                .datePaiement(p.getDatePaiement())
                .referenceTransaction(p.getReferenceTransaction())
                .observation(p.getObservation())
                .dateValidation(p.getDateValidation())
                .motifRejet(p.getMotifRejet())
                .build();
    }

    public static FactureAbonnementResponse facture(FactureAbonnement f) {
        return FactureAbonnementResponse.builder()
                .id(f.getId())
                .abonnementId(f.getAbonnement().getId())
                .numeroFacture(f.getNumeroFacture())
                .dateEmission(f.getDateEmission())
                .tauxTaxe(f.getTauxTaxe())
                .montantHT(f.getMontantHT())
                .montantTaxe(f.getMontantTaxe())
                .montantTTC(f.getMontantTTC())
                .devise(f.getDevise())
                .statut(f.getStatut())
                .build();
    }

    public static EvenementAbonnementResponse evenement(HistoriqueAbonnement h) {
        return EvenementAbonnementResponse.builder()
                .id(h.getId())
                .typeEvenement(h.getTypeEvenement())
                .ancienneValeur(h.getAncienneValeur())
                .nouvelleValeur(h.getNouvelleValeur())
                .dateEvenement(h.getDateEvenement())
                .description(h.getDescription())
                .build();
    }

    public static DemandeContratResponse demande(DemandeContrat d) {
        return DemandeContratResponse.builder()
                .id(d.getId())
                .pharmacopeeId(d.getPharmacopee() != null ? d.getPharmacopee().getId() : null)
                .dureeSouhaiteeMois(d.getDureeSouhaiteeMois())
                .dateDemande(d.getDateDemande())
                .nomContact(d.getNomContact())
                .telephone(d.getTelephone())
                .email(d.getEmail())
                .descriptionBesoin(d.getDescriptionBesoin())
                .statut(d.getStatut())
                .observation(d.getObservation())
                .dateTraitement(d.getDateTraitement())
                .build();
    }
}
