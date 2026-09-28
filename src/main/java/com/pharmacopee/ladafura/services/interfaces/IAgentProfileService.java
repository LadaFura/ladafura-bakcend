package com.pharmacopee.ladafura.services.interfaces;

import com.pharmacopee.ladafura.dto.agent.profil.AgentProfileResponse;
import com.pharmacopee.ladafura.dto.agent.profil.AgentUpdateProfileRequest;

public interface IAgentProfileService {

    /**
     * Récupère le profil complet de l'agent de collecte connecté ainsi que son statut et ses statistiques.
     *
     * @return DTO AgentProfileResponse
     */
    AgentProfileResponse getProfile();

    /**
     * Met à jour les informations modifiables autorisées du profil de l'agent connecté
     * (nom, prénom, téléphone, zone de couverture).
     *
     * @param request Données de mise à jour validées
     * @return DTO AgentProfileResponse mis à jour
     */
    AgentProfileResponse updateProfile(AgentUpdateProfileRequest request);
}
