package com.pharmacopee.ladafura.services.interfaces;

import com.pharmacopee.ladafura.dto.population.panier.PopulationAddToCartRequest;
import com.pharmacopee.ladafura.dto.population.panier.PopulationPanierResponse;
import com.pharmacopee.ladafura.dto.population.panier.PopulationUpdateQuantityRequest;

public interface IPopulationPanierService {

    /**
     * Récupère le panier actif de l'utilisateur Population connecté (le crée s'il n'existe pas encore).
     */
    PopulationPanierResponse getPanier();

    /**
     * Ajoute un produit au panier actif (ou incrémente sa quantité s'il y figure déjà).
     */
    PopulationPanierResponse ajouterProduitAuPanier(PopulationAddToCartRequest request);

    /**
     * Modifie la quantité d'un article présent dans le panier.
     */
    PopulationPanierResponse modifierQuantiteLigne(Long ligneId, PopulationUpdateQuantityRequest request);

    /**
     * Supprime une ligne d'article du panier actif.
     */
    PopulationPanierResponse supprimerLignePanier(Long ligneId);

    /**
     * Vide intégralement le panier de l'utilisateur.
     */
    void viderPanier();
}
