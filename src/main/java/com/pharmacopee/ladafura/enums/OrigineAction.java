package com.pharmacopee.ladafura.enums;

/**
 * Origine d'une action tracée dans le journal d'audit.
 */
public enum OrigineAction {
    /** Action d'un membre dans l'espace de sa pharmacopée. */
    PHARMACOPEE,
    /** Action de l'administrateur LADAFURA. */
    ADMINISTRATION,
    /** Action automatique (tâche planifiée, paiement en ligne). */
    SYSTEME
}
