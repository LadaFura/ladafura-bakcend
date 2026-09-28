package com.pharmacopee.ladafura.mappers;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.pharmacopee.ladafura.Models.Commande;
import com.pharmacopee.ladafura.Models.LigneCommande;
import com.pharmacopee.ladafura.Models.Localisation;
import com.pharmacopee.ladafura.Models.ModeRetrait;
import com.pharmacopee.ladafura.Models.Pharmacopee;
import com.pharmacopee.ladafura.Models.Produit;
import com.pharmacopee.ladafura.dto.population.commande.PopulationCommandeDetailResponse;
import com.pharmacopee.ladafura.dto.population.commande.PopulationCommandeStatutResponse;
import com.pharmacopee.ladafura.dto.population.commande.PopulationCommandeSummaryResponse;
import com.pharmacopee.ladafura.dto.population.commande.PopulationLigneCommandeDto;
import com.pharmacopee.ladafura.enums.StatutCommande;
import com.pharmacopee.ladafura.enums.TypeModeRetrait;

@Component
public class PopulationCommandeMapper {

    public boolean isAnnulable(StatutCommande statut) {
        return statut == StatutCommande.EN_ATTENTE || statut == StatutCommande.CONFIRMEE;
    }

    public String getStatusMessage(StatutCommande statut, TypeModeRetrait typeRetrait) {
        if (statut == null) {
            return "Statut inconnu";
        }
        return switch (statut) {
            case EN_ATTENTE -> "Votre commande a été transmise à la pharmacopée et attend sa confirmation.";
            case CONFIRMEE -> "Votre commande a été confirmée par la pharmacopée et va être préparée sous peu.";
            case PREPAREE -> typeRetrait == TypeModeRetrait.PICKUP
                    ? "Vos produits ont été préparés et sont prêts à être retirés en officine."
                    : "Votre commande est préparée et attend sa prise en charge pour la livraison.";
            case EN_LIVRAISON -> "Votre commande est en cours d'acheminement vers votre adresse de livraison.";
            case DISPONIBLE_PICKUP -> "Votre commande est à votre disposition en officine. Vous pouvez la retirer dès maintenant.";
            case LIVREE -> "Votre commande vous a été livrée avec succès.";
            case RETIREE -> "Votre commande a été retirée en officine. Merci pour votre confiance !";
            case ANNULEE -> "Cette commande a été annulée.";
        };
    }

    public PopulationLigneCommandeDto toLigneDto(LigneCommande lc) {
        if (lc == null) {
            return null;
        }
        Produit p = lc.getProduit();
        return PopulationLigneCommandeDto.builder()
                .id(lc.getId())
                .produitId(p != null ? p.getId() : null)
                .nomProduit(p != null ? p.getNom() : null)
                .forme(p != null ? p.getForme() : null)
                .photoUrl(p != null ? p.getPhotoUrl() : null)
                .prixUnitaire(lc.getPrixUnitaire())
                .quantite(lc.getQuantite())
                .sousTotal(lc.getSousTotal())
                .build();
    }

    public PopulationCommandeSummaryResponse toSummaryResponse(Commande c) {
        if (c == null) {
            return null;
        }

        Pharmacopee ph = c.getPharmacopee();
        ModeRetrait mr = c.getModeRetrait();

        int totalArticles = 0;
        if (c.getLignes() != null) {
            for (LigneCommande lc : c.getLignes()) {
                totalArticles += (lc.getQuantite() != null ? lc.getQuantite() : 0);
            }
        }

        return PopulationCommandeSummaryResponse.builder()
                .id(c.getId())
                .numero(c.getNumero())
                .dateCommande(c.getDateCommande())
                .statut(c.getStatut())
                .pharmacopeeId(ph != null ? ph.getId() : null)
                .nomPharmacopee(ph != null ? ph.getNom() : null)
                .modeRetrait(mr != null && mr.getType() != null ? mr.getType().name() : null)
                .totalProduit(c.getTotalProduit())
                .montantLivraison(c.getMontantLivraison())
                .montantTotal(c.getMontantTotal())
                .nombreArticles(totalArticles)
                .annulable(isAnnulable(c.getStatut()))
                .build();
    }

    public PopulationCommandeDetailResponse toDetailResponse(Commande c) {
        if (c == null) {
            return null;
        }

        Pharmacopee ph = c.getPharmacopee();
        ModeRetrait mr = c.getModeRetrait();

        String adressePharmacie = null;
        if (ph != null && ph.getLocalisation() != null) {
            Localisation loc = ph.getLocalisation();
            StringBuilder sb = new StringBuilder();
            if (loc.getLocalite() != null) sb.append(loc.getLocalite());
            if (loc.getCommune() != null) sb.append(sb.length() > 0 ? ", " : "").append(loc.getCommune());
            if (loc.getCercle() != null) sb.append(sb.length() > 0 ? ", " : "").append(loc.getCercle());
            if (loc.getRegion() != null) sb.append(sb.length() > 0 ? ", " : "").append(loc.getRegion());
            adressePharmacie = sb.toString();
        }

        List<PopulationLigneCommandeDto> lignesDto = new ArrayList<>();
        if (c.getLignes() != null) {
            for (LigneCommande lc : c.getLignes()) {
                PopulationLigneCommandeDto dto = toLigneDto(lc);
                if (dto != null) {
                    lignesDto.add(dto);
                }
            }
        }

        String statutPaiement = (c.getPaiement() != null && c.getPaiement().getStatut() != null)
                ? c.getPaiement().getStatut().name()
                : "EN_ATTENTE";

        return PopulationCommandeDetailResponse.builder()
                .id(c.getId())
                .numero(c.getNumero())
                .dateCommande(c.getDateCommande())
                .dateMiseAJour(c.getDateMiseAJour())
                .statut(c.getStatut())
                .pharmacopeeId(ph != null ? ph.getId() : null)
                .nomPharmacopee(ph != null ? ph.getNom() : null)
                .telephonePharmacopee(ph != null ? ph.getTelephone() : null)
                .adressePharmacopee(adressePharmacie)
                .modeRetrait(mr != null && mr.getType() != null ? mr.getType().name() : null)
                .montantLivraison(c.getMontantLivraison())
                .adresseLivraison(c.getAdresseLivraison())
                .totalProduit(c.getTotalProduit())
                .montantTotal(c.getMontantTotal())
                .annulable(isAnnulable(c.getStatut()))
                .lignes(lignesDto)
                .statutPaiement(statutPaiement)
                .build();
    }

    public PopulationCommandeStatutResponse toStatutResponse(Commande c) {
        if (c == null) {
            return null;
        }

        TypeModeRetrait typeRetrait = (c.getModeRetrait() != null) ? c.getModeRetrait().getType() : null;

        return PopulationCommandeStatutResponse.builder()
                .id(c.getId())
                .numero(c.getNumero())
                .statut(c.getStatut())
                .dateCommande(c.getDateCommande())
                .dateMiseAJour(c.getDateMiseAJour())
                .modeRetrait(typeRetrait != null ? typeRetrait.name() : null)
                .annulable(isAnnulable(c.getStatut()))
                .message(getStatusMessage(c.getStatut(), typeRetrait))
                .build();
    }
}
