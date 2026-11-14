package com.pharmacopee.ladafura.enums;

/**
 * Statut d'un abonnement. Un seul abonnement ACTIF ou EN_GRACE par pharmacopée.
 */
public enum StatutAbonnement {
    /** En vigueur : toutes les opérations, dans la limite des quotas. */
    ACTIF,
    /** Souscrit, en attente de confirmation du paiement par l'administrateur. */
    EN_ATTENTE_PAIEMENT,
    /** Payé, démarrera à la fin de l'abonnement en cours (renouvellement anticipé). */
    PROGRAMME,
    /** Échu : lecture seule, export et renouvellement uniquement. */
    EN_GRACE,
    /** Terminé (aucune donnée supprimée). */
    EXPIRE,
    /** Suspendu par l'administrateur : lecture seule. */
    SUSPENDU,
    /** Annulé (changement d'offre, décision du gérant ou de l'administrateur). */
    ANNULE
}
