package com.pharmacopee.ladafura.services.interfaces;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.pharmacopee.ladafura.dto.pharmacopee.avis.PharmacopeeAvisItemResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.avis.PharmacopeeAvisSummaryResponse;

public interface IPharmacopeeAvisService {

    /**
     * Liste paginée de tous les avis publiés (validés par la modération) sur la pharmacopée.
     */
    Page<PharmacopeeAvisItemResponse> getAvis(Pageable pageable);

    /**
     * Synthèse et statistiques de satisfaction globale sur la pharmacopée.
     */
    PharmacopeeAvisSummaryResponse getSummary();

    /**
     * Consultation détaillée d'un avis spécifique (produit associé à l'officine).
     */
    PharmacopeeAvisItemResponse getAvisDetail(Long id);

    PharmacopeeAvisItemResponse repondreAvis(Long avisId, String reponseText);
}
