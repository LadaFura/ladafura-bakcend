package com.pharmacopee.ladafura.services.interfaces;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.pharmacopee.ladafura.dto.pharmacopee.stock.PharmacopeeStockResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.stock.PharmacopeeStockSummaryResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.stock.UpdatePrixRequest;
import com.pharmacopee.ladafura.dto.pharmacopee.stock.UpdateStockCompletRequest;
import com.pharmacopee.ladafura.dto.pharmacopee.stock.UpdateStockRequest;

public interface IPharmacopeeStockService {

    /**
     * Liste paginée des stocks et disponibilités de l'officine avec filtres optionnels.
     */
    Page<PharmacopeeStockResponse> getStocks(Boolean disponible, Boolean enRupture, Pageable pageable);

    /**
     * Synthèse globale des stocks (valeur marchande, références actives, ruptures).
     */
    PharmacopeeStockSummaryResponse getSummary();

    /**
     * Détail du stock et de la tarification d'un produit dans l'officine.
     */
    PharmacopeeStockResponse getStockByProduit(Long produitId);

    /**
     * Mise à jour rapide de la quantité physique en stock.
     */
    PharmacopeeStockResponse updateQuantiteStock(Long produitId, UpdateStockRequest request);

    /**
     * Définition ou mise à jour du prix de vente pratiqué par l'officine.
     */
    PharmacopeeStockResponse updatePrix(Long produitId, UpdatePrixRequest request);

    /**
     * Inverse rapidement l'état de disponibilité d'un produit (actif <-> désactivé).
     */
    PharmacopeeStockResponse toggleDisponibilite(Long produitId);

    /**
     * Mise à jour complète (quantité, prix de vente et disponibilité).
     */
    PharmacopeeStockResponse updateStockComplet(Long produitId, UpdateStockCompletRequest request);
}
