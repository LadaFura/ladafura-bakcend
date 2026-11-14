package com.pharmacopee.ladafura.enums;

/**
 * Type d'offre d'abonnement SaaS. Aucune offre de catalogue ne dépasse 12 mois.
 */
public enum TypeOffre {
    /** Prix nul, sans échéance. */
    GRATUITE,
    /** 1 mois. */
    MENSUELLE,
    /** 6 mois. */
    SEMESTRIELLE,
    /** 12 mois. */
    ANNUELLE,
    /** Jamais souscrite directement : abonnement créé par l'administrateur après une DemandeContrat. */
    CONTRAT_PERSONNALISE
}
