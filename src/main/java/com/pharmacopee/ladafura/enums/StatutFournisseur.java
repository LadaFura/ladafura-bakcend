package com.pharmacopee.ladafura.enums;

/**
 * Statut d'un fournisseur. Un fournisseur lié à des achats n'est jamais supprimé : il passe INACTIF.
 */
public enum StatutFournisseur {
    ACTIF,
    /** Ne peut plus être choisi pour un nouvel achat. */
    INACTIF
}
