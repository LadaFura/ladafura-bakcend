package com.pharmacopee.ladafura.enums;

/**
 * Type de mouvement de stock. Tout changement de quantité passe par un MouvementStock.
 */
public enum TypeMouvementStock {
    ENTREE_ACHAT,
    ANNULATION_ACHAT,
    SORTIE_VENTE,
    ANNULATION_VENTE,
    SORTIE_COMMANDE,
    ANNULATION_COMMANDE
}
