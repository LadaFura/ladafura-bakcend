package com.pharmacopee.ladafura.services.interfaces;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.pharmacopee.ladafura.dto.agent.source.AgentCreateSourceRequest;
import com.pharmacopee.ladafura.dto.agent.source.AgentSourceResponse;
import com.pharmacopee.ladafura.dto.agent.source.AgentUpdateSourceRequest;
import com.pharmacopee.ladafura.enums.Role;

public interface IAgentSourceService {

    /**
     * Enregistre une nouvelle source de terrain (thérapeute traditionnel / herboriste).
     * Ne crée AUCUN compte utilisateur ni identifiant Firebase.
     *
     * @param request Données de la source (nom, prénom, coordonnées, spécialité, expérience)
     * @return DTO AgentSourceResponse
     */
    AgentSourceResponse creerSource(AgentCreateSourceRequest request);

    /**
     * Met à jour les informations et coordonnées d'une source existante.
     *
     * @param id Identifiant de la source
     * @param request Modifications à apporter
     * @return DTO AgentSourceResponse
     */
    AgentSourceResponse modifierSource(Long id, AgentUpdateSourceRequest request);

    /**
     * Récupère la fiche détaillée d'une source pour traçabilité.
     *
     * @param id Identifiant de la source
     * @return DTO AgentSourceResponse
     */
    AgentSourceResponse getSourceById(Long id);

    /**
     * Recherche des sources de terrain avec pagination et filtres (mot-clé et profil).
     *
     * @param query Terme de recherche (nom, prénom, spécialité, adresse, téléphone)
     * @param role Filtre de rôle (THERAPEUTE, HERBORISTE, ou null pour tous)
     * @param pageable Paramètres de pagination
     * @return Page de DTOs AgentSourceResponse
     */
    Page<AgentSourceResponse> rechercherSources(String query, Role role, Pageable pageable);

    /**
     * Associe une source existante à une fiche de collecte de terrain.
     * Uniquement autorisée si la collecte appartient à l'agent connecté et est en statut modifiable (BROUILLON ou REJETEE).
     *
     * @param collecteId Identifiant de la collecte
     * @param sourceId Identifiant de la source
     */
    void associerSourceACollecte(Long collecteId, Long sourceId);

    /**
     * Dissocie la source d'une fiche de collecte.
     * Uniquement autorisée si la collecte appartient à l'agent connecté et est en statut modifiable (BROUILLON ou REJETEE).
     *
     * @param collecteId Identifiant de la collecte
     */
    void dissocierSourceDeCollecte(Long collecteId);
}
