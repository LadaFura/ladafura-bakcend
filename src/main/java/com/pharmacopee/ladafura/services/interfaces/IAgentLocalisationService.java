package com.pharmacopee.ladafura.services.interfaces;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.pharmacopee.ladafura.dto.agent.localisation.AgentLocalisationRequest;
import com.pharmacopee.ladafura.dto.agent.localisation.AgentLocalisationResponse;

public interface IAgentLocalisationService {

    /**
     * Enregistre ou met à jour la localisation géographique d'une fiche de collecte (relation 1 ── 1).
     * Uniquement autorisée si la collecte appartient à l'agent connecté et est en statut modifiable (BROUILLON ou REJETEE).
     *
     * @param collecteId Identifiant de la collecte
     * @param request Données de localisation (région, cercle, commune, localité, coordonnées GPS)
     * @return DTO AgentLocalisationResponse
     */
    AgentLocalisationResponse saveOrUpdateLocalisation(Long collecteId, AgentLocalisationRequest request);

    /**
     * Récupère la localisation géographique associée à une fiche de collecte.
     *
     * @param collecteId Identifiant de la collecte
     * @return DTO AgentLocalisationResponse
     */
    AgentLocalisationResponse getLocalisationByCollecte(Long collecteId);

    /**
     * Supprime ou dissocie la localisation d'une fiche de collecte modifiable.
     *
     * @param collecteId Identifiant de la collecte
     */
    void supprimerLocalisation(Long collecteId);

    /**
     * Recherche des localisations enregistrées sur le terrain avec pagination.
     *
     * @param query Mot-clé de recherche (région, cercle, commune, localité)
     * @param pageable Pagination
     * @return Page de localisations
     */
    Page<AgentLocalisationResponse> rechercherLocalisations(String query, Pageable pageable);
}
