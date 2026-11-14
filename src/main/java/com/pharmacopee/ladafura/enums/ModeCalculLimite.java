package com.pharmacopee.ladafura.enums;

/**
 * Mode de calcul de la consommation d'une limite d'offre.
 */
public enum ModeCalculLimite {
    /** Calculée par comptage des données existantes (jamais stockée). */
    COMPTAGE,
    /** Compteur non recalculable, stocké dans {@code UtilisationLimite}. */
    COMPTEUR
}
