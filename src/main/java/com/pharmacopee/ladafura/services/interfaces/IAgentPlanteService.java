package com.pharmacopee.ladafura.services.interfaces;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.pharmacopee.ladafura.dto.agent.plante.AgentCreatePlanteRequest;
import com.pharmacopee.ladafura.dto.agent.plante.AgentPlanteAssociationResponse;
import com.pharmacopee.ladafura.dto.agent.plante.AgentPlanteResponse;

public interface IAgentPlanteService {

    /**
     * Recherche les plantes par mot-clé (nom scientifique ou nom vernaculaire) avec pagination.
     */
    Page<AgentPlanteResponse> searchPlantes(String keyword, Pageable pageable);

    /**
     * Récupère les informations détaillées d'une plante par son identifiant.
     */
    AgentPlanteResponse getPlanteById(Long id);

    /**
     * Crée une nouvelle plante si elle n'existe pas déjà (évite les doublons par nom scientifique).
     */
    AgentPlanteResponse createPlante(AgentCreatePlanteRequest request);

    /**
     * Associe une plante à une fiche de collecte de l'agent connecté.
     */
    AgentPlanteAssociationResponse associatePlanteToCollecte(Long collecteId, Long planteId);

    /**
     * Récupère la liste des plantes associées à une fiche de collecte donnée.
     */
    List<AgentPlanteAssociationResponse> getPlantesByCollecte(Long collecteId);

    /**
     * Dissocie une plante d'une fiche de collecte (si la collecte est encore au statut BROUILLON ou REJETEE).
     */
    void dissociatePlanteFromCollecte(Long collecteId, Long planteId);
}
