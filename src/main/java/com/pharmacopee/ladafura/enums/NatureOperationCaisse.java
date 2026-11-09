package com.pharmacopee.ladafura.enums;

/**
 * Nature d'une opération de caisse.
 */
public enum NatureOperationCaisse {
    /** Paiement d'une vente au comptoir. */
    VENTE,
    /** Paiement d'une commande en ligne. */
    PAIEMENT_COMMANDE,
    /** Règlement d'un achat fournisseur. */
    ACHAT,
    /** Paiement d'une dépense. */
    DEPENSE,
    APPORT,
    RETRAIT,
    /** Mouvement entre deux caisses (deux opérations jumelles). */
    TRANSFERT,
    /** Recette hors ventes. */
    RECETTE_DIVERSE,
    /** Réalignement après un écart de clôture. */
    AJUSTEMENT,
    /** Contre-passation d'une opération. */
    ANNULATION
}
