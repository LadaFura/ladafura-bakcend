package com.pharmacopee.ladafura.services.interfaces;

import com.pharmacopee.ladafura.Models.AgentCollecte;
import com.pharmacopee.ladafura.dto.agent.auth.AgentAuthResponse;

public interface IAgentAuthService {

    /**
     * Récupère l'agent de collecte actuellement authentifié via Spring Security (Firebase Token).
     * Vérifie que le compte est actif et possède le rôle AGENT_COLLECTE.
     *
     * @return L'entité AgentCollecte correspondante
     */
    AgentCollecte getCurrentAgent();

    /**
     * Renvoie le profil et les informations de session de l'agent de collecte connecté.
     *
     * @return DTO AgentAuthResponse
     */
    AgentAuthResponse getMe();

    /**
     * Vérifie strictement que l'agent connecté est le propriétaire de la ressource demandée.
     * Lève une ForbiddenException si les identifiants ne correspondent pas.
     *
     * @param requestedAgentId Identifiant de l'agent ciblé
     */
    void verifyAgentOwnership(Long requestedAgentId);
}
