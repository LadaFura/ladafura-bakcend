package com.pharmacopee.ladafura.services.interfaces;

import java.util.List;

import com.pharmacopee.ladafura.dto.agent.vernaculaire.AgentNomVernaculaireRequest;
import com.pharmacopee.ladafura.dto.agent.vernaculaire.AgentNomVernaculaireResponse;

public interface IAgentNomVernaculaireService {

    /**
     * Récupère la liste de tous les noms vernaculaires associés à une plante.
     */
    List<AgentNomVernaculaireResponse> getNomsByPlanteId(Long planteId);

    /**
     * Ajoute un nouveau nom vernaculaire à une plante (avec contrôle d'unicité par plante et langue).
     */
    AgentNomVernaculaireResponse addNomVernaculaire(Long planteId, AgentNomVernaculaireRequest request);

    /**
     * Met à jour un nom vernaculaire existant rattaché à une plante.
     */
    AgentNomVernaculaireResponse updateNomVernaculaire(Long planteId, Long nomId, AgentNomVernaculaireRequest request);

    /**
     * Supprime un nom vernaculaire d'une plante.
     */
    void deleteNomVernaculaire(Long planteId, Long nomId);

    /**
     * Recherche des noms vernaculaires par terme et/ou par langue locale.
     */
    List<AgentNomVernaculaireResponse> searchNomsVernaculaires(String query, String langue);
}
