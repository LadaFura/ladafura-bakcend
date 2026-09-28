package com.pharmacopee.ladafura.services.interfaces;

import com.pharmacopee.ladafura.dto.population.profil.PopulationProfileResponse;
import com.pharmacopee.ladafura.dto.population.profil.PopulationUpdateProfileRequest;

public interface IPopulationProfileService {

    /**
     * Récupère le profil complet et le statut de l'utilisateur de la Population actuellement connecté.
     *
     * @return PopulationProfileResponse
     */
    PopulationProfileResponse getProfile();

    /**
     * Met à jour les informations autorisées du profil de l'utilisateur connecté (nom, prénom, téléphone).
     * Le rôle et le statut restent rigoureusement protégés et inaltérables.
     *
     * @param request Données modifiables
     * @return PopulationProfileResponse mis à jour
     */
    PopulationProfileResponse updateProfile(PopulationUpdateProfileRequest request);
}
