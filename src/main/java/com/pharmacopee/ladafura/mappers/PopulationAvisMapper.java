package com.pharmacopee.ladafura.mappers;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.pharmacopee.ladafura.Models.Avis;
import com.pharmacopee.ladafura.Models.Pharmacopee;
import com.pharmacopee.ladafura.Models.Utilisateur;
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

        Pharmacopee p = avis.getPharmacopee();
        Utilisateur u = avis.getUtilisateur();

        return PopulationAvisResponse.builder()
                .id(avis.getId())
                .pharmacopeeId(p != null ? p.getId() : null)
                .nomPharmacopee(p != null ? p.getNom() : null)
                .auteurPrenom(u != null ? u.getPrenom() : null)
                .auteurNom(u != null ? u.getNom() : null)
                .reponseOfficine(avis.getReponseOfficine())
                .dateReponse(avis.getDateReponse())
                .note(avis.getNote())
                .commentaire(avis.getCommentaire())
                .dateAvis(avis.getDateAvis())
                .statut(avis.getStatut())
                .statutLibelle(getStatutLibelle(avis.getStatut()))
                .build();
    }

    public PopulationEligibiliteAvisResponse toEligibiliteResponse(
            Pharmacopee pharmacopee, boolean eligible, Optional<Avis> existingAvis) {
        Long pharmaId = pharmacopee != null ? pharmacopee.getId() : null;
        String nomPharma = pharmacopee != null ? pharmacopee.getNom() : null;
        boolean dejaEvalue = existingAvis.isPresent();
        Long avisId = dejaEvalue ? existingAvis.get().getId() : null;

        String message;
        if (!eligible) {
            message = "Vous devez avoir commandé auprès de cette pharmacopée (commande livrée ou retirée) pour pouvoir déposer un avis.";
        } else if (dejaEvalue) {
            message = "Vous avez déjà évalué cette pharmacopée. Vous pouvez modifier votre note et votre commentaire.";
        } else {
            message = "Vous êtes éligible à déposer un avis sur cette pharmacopée.";
        }

        return PopulationEligibiliteAvisResponse.builder()
                .pharmacopeeId(pharmaId)
                .nomPharmacopee(nomPharma)
                .eligible(eligible)
                .dejaEvalue(dejaEvalue)
                .avisId(avisId)
                .message(message)
                .build();
    }
}
