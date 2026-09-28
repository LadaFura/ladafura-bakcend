package com.pharmacopee.ladafura.mappers;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.pharmacopee.ladafura.Models.Avis;
import com.pharmacopee.ladafura.Models.Produit;
import com.pharmacopee.ladafura.dto.population.avis.PopulationAvisResponse;
import com.pharmacopee.ladafura.dto.population.avis.PopulationEligibiliteAvisResponse;
import com.pharmacopee.ladafura.enums.StatutAvis;

@Component
public class PopulationAvisMapper {

    public String getStatutLibelle(StatutAvis statut) {
        if (statut == null) {
            return "Inconnu";
        }
        return switch (statut) {
            case EN_ATTENTE -> "En attente de modération";
            case PUBLIE -> "Publié";
            case REJETE -> "Rejeté par la modération";
            case MASQUE -> "Masqué";
        };
    }

    public PopulationAvisResponse toResponse(Avis avis) {
        if (avis == null) {
            return null;
        }

        Produit p = avis.getProduit();

        return PopulationAvisResponse.builder()
                .id(avis.getId())
                .produitId(p != null ? p.getId() : null)
                .nomProduit(p != null ? p.getNom() : null)
                .formeProduit(p != null ? p.getForme() : null)
                .photoProduitUrl(p != null ? p.getPhotoUrl() : null)
                .note(avis.getNote())
                .commentaire(avis.getCommentaire())
                .dateAvis(avis.getDateAvis())
                .statut(avis.getStatut())
                .statutLibelle(getStatutLibelle(avis.getStatut()))
                .build();
    }

    public PopulationEligibiliteAvisResponse toEligibiliteResponse(
            Produit produit, boolean eligible, Optional<Avis> existingAvis) {
        Long prodId = produit != null ? produit.getId() : null;
        String nomProd = produit != null ? produit.getNom() : null;
        boolean dejaEvalue = existingAvis.isPresent();
        Long avisId = dejaEvalue ? existingAvis.get().getId() : null;

        String message;
        if (!eligible) {
            message = "Vous devez avoir commandé et réceptionné ce produit (commande livrée ou retirée) pour pouvoir déposer un avis vérifié.";
        } else if (dejaEvalue) {
            message = "Vous avez déjà évalué ce produit. Vous pouvez modifier votre note et votre commentaire.";
        } else {
            message = "Vous êtes éligible à déposer un avis vérifié sur ce produit.";
        }

        return PopulationEligibiliteAvisResponse.builder()
                .produitId(prodId)
                .nomProduit(nomProd)
                .eligible(eligible)
                .dejaEvalue(dejaEvalue)
                .avisId(avisId)
                .message(message)
                .build();
    }
}
