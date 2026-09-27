package com.pharmacopee.ladafura.services.interfaces;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.pharmacopee.ladafura.dto.pharmacopee.avis.PharmacopeeAvisItemResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.avis.PharmacopeeAvisSummaryResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.avis.PharmacopeeProduitAvisResponse;

public interface IPharmacopeeAvisService {

    /**
     * Liste paginée de tous les avis publiés (validés par la modération) sur les produits de l'officine.
     */
    Page<PharmacopeeAvisItemResponse> getAvis(Pageable pageable);

    /**
     * Synthèse et statistiques de satisfaction globale sur les produits de l'officine.
     */
    PharmacopeeAvisSummaryResponse getSummary();

    /**
     * Consultation des avis publiés et de la note moyenne pour un produit spécifique de l'officine.
     */
    PharmacopeeProduitAvisResponse getAvisByProduit(Long produitId, Pageable pageable);

    /**
     * Consultation détaillée d'un avis spécifique (produit associé à l'officine).
     */
    PharmacopeeAvisItemResponse getAvisDetail(Long id);
}
