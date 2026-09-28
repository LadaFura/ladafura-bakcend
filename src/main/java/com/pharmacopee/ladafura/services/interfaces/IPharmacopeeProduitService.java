package com.pharmacopee.ladafura.services.interfaces;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.pharmacopee.ladafura.dto.pharmacopee.produit.AssocierProduitRequest;
import com.pharmacopee.ladafura.dto.pharmacopee.produit.CatalogueProduitItemResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.produit.PharmacopeeProduitResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.produit.UpdateProduitDisponibiliteRequest;

public interface IPharmacopeeProduitService {

    /**
     * Liste paginée des produits associés à la pharmacopée connectée avec filtres optionnels.
     */
    Page<PharmacopeeProduitResponse> getMesProduits(String keyword, Long categorieId, Boolean disponible, Pageable pageable);

    /**
     * Détail d'un produit associé à la pharmacopée connectée.
     */
    PharmacopeeProduitResponse getMonProduit(Long produitId);

    /**
     * Liste paginée des produits validés du catalogue national LADAFURA pouvant être associés.
     */
    Page<CatalogueProduitItemResponse> getCatalogueGlobal(String keyword, Long categorieId, Pageable pageable);

    /**
     * Associe un produit du catalogue à la pharmacopée connectée (DisponibiliteProduit).
     * Exige que la pharmacopée soit VALIDEE.
     */
    PharmacopeeProduitResponse associerProduit(AssocierProduitRequest request);

    /**
     * Met à jour la disponibilité et/ou le stock d'un produit associé à sa pharmacopée.
     */
    PharmacopeeProduitResponse updateDisponibilite(Long produitId, UpdateProduitDisponibiliteRequest request);

    /**
     * Retire un produit de la pharmacopée (supprime la liaison DisponibiliteProduit).
     */
    void retirerProduit(Long produitId);
}
