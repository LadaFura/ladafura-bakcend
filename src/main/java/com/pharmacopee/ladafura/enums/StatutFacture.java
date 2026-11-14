package com.pharmacopee.ladafura.enums;

/**
 * Statut d'une facture d'abonnement. Une facture n'est jamais modifiée : elle est annulée puis réémise.
 */
public enum StatutFacture {
    EMISE,
    PAYEE,
    ANNULEE
}
