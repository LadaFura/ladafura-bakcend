package com.pharmacopee.ladafura.mappers;

import org.springframework.stereotype.Component;

import com.pharmacopee.ladafura.Models.Utilisateur;
import com.pharmacopee.ladafura.dto.population.profil.PopulationProfileResponse;

@Component
public class PopulationProfileMapper {

    public PopulationProfileResponse toDto(Utilisateur user, long totalCommandes, long totalFavoris) {
        if (user == null) {
            return null;
        }

        return PopulationProfileResponse.builder()
                .id(user.getId())
                .nom(user.getNom())
                .prenom(user.getPrenom())
                .email(user.getEmail())
                .telephone(user.getTelephone())
                .role(user.getRole())
                .statut(user.getStatut())
                .firebaseUid(user.getFirebaseUid())
                .dateCreation(user.getDateCreation())
                .nombreTotalCommandes(totalCommandes)
                .nombreTotalFavoris(totalFavoris)
                .build();
    }
}
