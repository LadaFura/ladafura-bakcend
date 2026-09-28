package com.pharmacopee.ladafura.services.interfaces;

import com.pharmacopee.ladafura.dto.agent.submission.AgentCollecteRecapitulatifResponse;
import com.pharmacopee.ladafura.dto.agent.submission.AgentSubmissionResultResponse;

public interface IAgentSubmissionService {

    /**
     * Génère la fiche récapitulative complète d'une collecte avant soumission :
     * contrôle plante, noms vernaculaires, connaissances traditionnelles, source, médias, localisation,
     * et établit la checklist de conformité (erreurs bloquantes et points de vigilance).
     *
     * @param collecteId Identifiant de la collecte
     * @return DTO AgentCollecteRecapitulatifResponse
     */
    AgentCollecteRecapitulatifResponse getRecapitulatif(Long collecteId);

    /**
     * Effectue la vérification d'intégrité et soumet formellement la collecte pour validation.
     * En cas de succès :
     * - Collecte passe à statut SOUMISE
     * - Date de soumission enregistrée
     * - Vertus passent à statut EN_ATTENTE
     * - Les modifications ultérieures sont immédiatement verrouillées.
     *
     * @param collecteId Identifiant de la collecte
     * @return DTO AgentSubmissionResultResponse
     */
    AgentSubmissionResultResponse soumettreCollecteVerifiee(Long collecteId);
}
