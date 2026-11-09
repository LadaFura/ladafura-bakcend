package com.pharmacopee.ladafura.enums;

/**
 * Statut d'un membre au sein d'une pharmacopée (tenant).
 * Seul un membre ACTIF peut agir dans l'espace de la pharmacopée.
 */
public enum StatutMembre {
    /** Invitation envoyée, pas encore acceptée. */
    INVITE,
    /** Membre opérationnel. */
    ACTIF,
    /** Accès temporairement bloqué par le gérant. */
    SUSPENDU,
    /** Membre retiré (conservé pour la traçabilité). */
    RETIRE
}
