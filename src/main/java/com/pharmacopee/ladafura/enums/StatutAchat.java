package com.pharmacopee.ladafura.enums;

/**
 * Cycle de vie d'un achat. Le stock n'est modifié qu'à la validation.
 */
public enum StatutAchat {
    /** Modifiable et supprimable, sans effet sur le stock. */
    BROUILLON,
    /** Marchandise reçue : entrées de stock enregistrées, achat non modifiable. */
    VALIDE,
    /** Annulé : mouvements de stock inverses si l'achat était validé. */
    ANNULE
}
