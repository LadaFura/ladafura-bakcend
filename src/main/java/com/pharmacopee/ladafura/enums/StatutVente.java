package com.pharmacopee.ladafura.enums;

/**
 * Cycle de vie d'une vente au comptoir.
 */
public enum StatutVente {
    /** Mise de côté, modifiable, sans effet sur le stock ni sur la caisse. */
    EN_ATTENTE,
    /** Stock sorti, prix figés, paiements enregistrés. */
    VALIDEE,
    /** Annulation totale : stock remis, paiements contre-passés. */
    ANNULEE,
    /** Vente en attente abandonnée, conservée sans effet. */
    ABANDONNEE
}
