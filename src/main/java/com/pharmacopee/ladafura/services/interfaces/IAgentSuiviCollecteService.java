package com.pharmacopee.ladafura.services.interfaces;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.pharmacopee.ladafura.dto.agent.collecte.AgentCollecteSummaryResponse;
import com.pharmacopee.ladafura.dto.agent.suivi.AgentCollecteRejetDetailResponse;
import com.pharmacopee.ladafura.dto.agent.suivi.AgentSuiviStatsResponse;
import com.pharmacopee.ladafura.enums.StatutCollecte;

public interface IAgentSuiviCollecteService {

    /**
     * Calcule les statistiques et compteurs globaux de suivi des collectes pour l'agent connecté.
     * (total, brouillons, soumises, en examen, validées, rejetées/à corriger, taux de validation).
     *
     * @return DTO AgentSuiviStatsResponse
     */
    AgentSuiviStatsResponse getStatsSuivi();

    /**
     * Récupère la liste paginée des collectes de l'agent connecté selon un statut donné.
     *
     * @param statut StatutCollecte optionnel
     * @param pageable Pagination et tri
     * @return Page de AgentCollecteSummaryResponse
     */
    Page<AgentCollecteSummaryResponse> getCollectesParStatut(StatutCollecte statut, Pageable pageable);

    /**
     * Récupère les brouillons (statut BROUILLON) de l'agent connecté.
     */
    Page<AgentCollecteSummaryResponse> getBrouillons(Pageable pageable);

    /**
     * Récupère les collectes soumises (statut SOUMISE) en attente d'examen.
     */
    Page<AgentCollecteSummaryResponse> getSoumises(Pageable pageable);

    /**
     * Récupère les collectes en cours d'examen administratif (statut EN_EXAMEN).
     */
    Page<AgentCollecteSummaryResponse> getEnExamen(Pageable pageable);

    /**
     * Récupère les collectes validées (statut VALIDEE).
     */
    Page<AgentCollecteSummaryResponse> getValidees(Pageable pageable);

    /**
     * Récupère les collectes rejetées nécessitant des corrections (statut REJETEE).
     */
    Page<AgentCollecteSummaryResponse> getRejetees(Pageable pageable);

    /**
     * Fournit l'explication détaillée du rejet d'une collecte pour permettre à l'agent
     * de comprendre le motif du rejet et de suivre le guide de correction approprié.
     *
     * @param collecteId Identifiant de la collecte
     * @return DTO AgentCollecteRejetDetailResponse
     */
    AgentCollecteRejetDetailResponse getDetailRejet(Long collecteId);

    /**
     * Recherche avancée multi-critères dans le suivi des collectes de l'agent.
     *
     * @param statut Statut optionnel
     * @param query Texte recherché (description, localité, région, cercle, nom de plante, source)
     * @param pageable Pagination et tri
     * @return Page de AgentCollecteSummaryResponse
     */
    Page<AgentCollecteSummaryResponse> searchSuiviCollectes(StatutCollecte statut, String query, Pageable pageable);
}
