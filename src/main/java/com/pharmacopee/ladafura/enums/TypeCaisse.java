package com.pharmacopee.ladafura.enums;

/**
 * Type de caisse ou de compte de trésorerie.
 */
public enum TypeCaisse {
    /** Espèces, avec sessions d'ouverture et de clôture. */
    PHYSIQUE,
    /** Compte Mobile Money, sans session. */
    MOBILE_MONEY,
    /** Compte bancaire, sans session. */
    BANQUE
}
