package com.pharmacopee.ladafura.services.interfaces;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.pharmacopee.ladafura.dto.agent.collecte.AgentCollecteDetailResponse;
import com.pharmacopee.ladafura.dto.agent.collecte.AgentCollecteSummaryResponse;
import com.pharmacopee.ladafura.dto.agent.collecte.AgentCreateCollecteRequest;
import com.pharmacopee.ladafura.dto.agent.collecte.AgentUpdateCollecteRequest;
import com.pharmacopee.ladafura.enums.StatutCollecte;

public interface IAgentCollecteService {

    /**
     * Crée une nouvelle collecte terrain rattachée à l'agent connecté.
     * Si request.isSoumettre() est vrai, passe immédiatement en statut SOUMISE, sinon BROUILLON.
     */
    AgentCollecteDetailResponse createCollecte(AgentCreateCollecteRequest request);

    /**
     * Enregistre explicitement une nouvelle collecte sous le statut BROUILLON.
     */
    AgentCollecteDetailResponse saveDraft(AgentCreateCollecteRequest request);

    /**
     * Modifie une collecte existante.
     * Règle métier : autorisée uniquement si statut BROUILLON ou REJETEE (demande de correction).
     */
    AgentCollecteDetailResponse updateCollecte(Long id, AgentUpdateCollecteRequest request);

    /**
     * Récupère la liste paginée des collectes de l'agent connecté avec filtre optionnel par statut.
     */
    Page<AgentCollecteSummaryResponse> getMyCollectes(StatutCollecte statut, Pageable pageable);

    /**
     * Récupère les détails d'une collecte appartenant à l'agent connecté.
     */
    AgentCollecteDetailResponse getMyCollecteById(Long id);

    /**
     * Soumet une collecte pour examen et validation administrative (BROUILLON / REJETEE -> SOUMISE).
     */
    AgentCollecteDetailResponse submitCollecte(Long id);

    /**
     * Supprime un brouillon de collecte appartenant à l'agent connecté.
     * Règle métier : seul le statut BROUILLON peut faire l'objet d'une suppression physique.
     */
    void deleteDraft(Long id);
}
