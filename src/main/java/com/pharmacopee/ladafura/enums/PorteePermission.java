package com.pharmacopee.ladafura.enums;

/**
 * Niveau auquel une permission s'applique.
 * Les deux niveaux sont strictement séparés : un rôle de pharmacopée
 * ne peut jamais porter une permission de PLATEFORME.
 */
public enum PorteePermission {
    /** Administration globale LADAFURA (gérée par l'administrateur). */
    PLATEFORME,
    /** Gestion interne d'une pharmacopée (gérée par son propriétaire). */
    PHARMACOPEE
}
