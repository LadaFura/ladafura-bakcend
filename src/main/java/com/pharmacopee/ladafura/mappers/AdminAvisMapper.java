package com.pharmacopee.ladafura.mappers;

import org.springframework.stereotype.Component;

import com.pharmacopee.ladafura.Models.Avis;
import com.pharmacopee.ladafura.dto.admin.avis.AdminAvisResponse;

@Component
public class AdminAvisMapper {

    public AdminAvisResponse toDto(Avis avis) {
        if (avis == null) {
            return null;
        }

        AdminAvisResponse response = AdminAvisResponse.builder()
                .id(avis.getId())
                .note(avis.getNote())
                .commentaire(avis.getCommentaire())
                .dateAvis(avis.getDateAvis())
                .statut(avis.getStatut())
                .build();

        if (avis.getUtilisateur() != null) {
            response.setUtilisateurId(avis.getUtilisateur().getId());
            String prenom = avis.getUtilisateur().getPrenom() != null ? avis.getUtilisateur().getPrenom() : "";
            String nom = avis.getUtilisateur().getNom() != null ? avis.getUtilisateur().getNom() : "";
            response.setNomCompletUtilisateur((prenom + " " + nom).trim());
            response.setEmailUtilisateur(avis.getUtilisateur().getEmail());
        }

        if (avis.getProduit() != null) {
            response.setProduitId(avis.getProduit().getId());
            response.setNomProduit(avis.getProduit().getNom());
        }

        return response;
    }
}
