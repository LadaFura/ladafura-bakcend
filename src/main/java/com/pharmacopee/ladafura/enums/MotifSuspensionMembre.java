package com.pharmacopee.ladafura.enums;

/**
 * Motif de la suspension d'un membre de pharmacopée.
 * Au renouvellement d'un abonnement, seuls les membres suspendus pour
 * {@link #QUOTA_ABONNEMENT} sont réactivés automatiquement.
 */
public enum MotifSuspensionMembre {
    /** Suspendu par le gérant de la pharmacopée. */
    DECISION_GERANT,
    /** Suspendu par l'administrateur LADAFURA. */
    DECISION_ADMIN,
    /** Suspendu automatiquement : quota d'utilisateurs actifs de l'abonnement dépassé. */
    QUOTA_ABONNEMENT
}
