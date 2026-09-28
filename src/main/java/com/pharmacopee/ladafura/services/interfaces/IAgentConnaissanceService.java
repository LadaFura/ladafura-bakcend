package com.pharmacopee.ladafura.services.interfaces;

import java.util.List;

import com.pharmacopee.ladafura.dto.agent.connaissance.AgentConnaissanceRequest;
import com.pharmacopee.ladafura.dto.agent.connaissance.AgentConnaissanceResponse;
import com.pharmacopee.ladafura.dto.agent.connaissance.AgentConnaissanceUpdateRequest;

public interface IAgentConnaissanceService {

    /**
     * Enregistre une connaissance traditionnelle recueillie sur le terrain pour une plante et une collecte.
     * Si une vertu existe déjà pour ce couple (collecte, plante), elle est complétée et mise à jour.
     *
     * @param request Données de la connaissance traditionnelle (usage, partie utilisée, préparation, précautions, etc.)
     * @return DTO AgentConnaissanceResponse
     */
    AgentConnaissanceResponse enregistrerConnaissance(AgentConnaissanceRequest request);

    /**
     * Modifie une connaissance traditionnelle existante.
     * Uniquement autorisée si la collecte parente appartient à l'agent connecté et est en statut modifiable (BROUILLON ou REJETEE).
     *
     * @param id Identifiant de l'enregistrement de connaissance (VertuDeLaPlante)
     * @param request Nouvelles informations
     * @return DTO AgentConnaissanceResponse
     */
    AgentConnaissanceResponse modifierConnaissance(Long id, AgentConnaissanceUpdateRequest request);

    /**
     * Récupère le détail d'une connaissance traditionnelle recueillie.
     *
     * @param id Identifiant de la connaissance
     * @return DTO AgentConnaissanceResponse
     */
    AgentConnaissanceResponse getConnaissanceById(Long id);

    /**
     * Récupère toutes les connaissances traditionnelles rattachées à une fiche de collecte.
     *
     * @param collecteId Identifiant de la fiche de collecte
     * @return Liste de DTOs AgentConnaissanceResponse
     */
    List<AgentConnaissanceResponse> getConnaissancesByCollecte(Long collecteId);

    /**
     * Récupère toutes les connaissances traditionnelles recensées pour une plante donnée.
     *
     * @param planteId Identifiant de la plante
     * @return Liste de DTOs AgentConnaissanceResponse
     */
    List<AgentConnaissanceResponse> getConnaissancesByPlante(Long planteId);

    /**
     * Supprime une connaissance traditionnelle d'une collecte en brouillon.
     *
     * @param id Identifiant de la connaissance à supprimer
     */
    void supprimerConnaissance(Long id);
}
