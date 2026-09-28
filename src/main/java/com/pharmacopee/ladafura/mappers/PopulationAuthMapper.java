package com.pharmacopee.ladafura.mappers;

import org.springframework.stereotype.Component;

import com.pharmacopee.ladafura.Models.Utilisateur;
import com.pharmacopee.ladafura.dto.population.auth.PopulationAuthResponse;

@Component
public class PopulationAuthMapper {

    public PopulationAuthResponse toDto(Utilisateur user) {
        if (user == null) {
            return null;
        }

        return PopulationAuthResponse.builder()
                .id(user.getId())
                .nom(user.getNom())
                .prenom(user.getPrenom())
                .email(user.getEmail())
                .telephone(user.getTelephone())
                .role(user.getRole())
                .statut(user.getStatut())
                .firebaseUid(user.getFirebaseUid())
                .dateCreation(user.getDateCreation())
                .build();
    }
}
