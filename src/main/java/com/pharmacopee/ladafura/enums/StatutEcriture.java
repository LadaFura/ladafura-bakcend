package com.pharmacopee.ladafura.enums;

/**
 * Statut d'une écriture financière immuable (règlement d'achat, paiement de vente, dépense).
 * Une écriture n'est jamais modifiée : elle est annulée avec contre-passation en caisse.
 */
public enum StatutEcriture {
    VALIDE,
    ANNULE
}
