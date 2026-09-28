package com.pharmacopee.ladafura.services.interfaces;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.pharmacopee.ladafura.dto.population.produit.PopulationOffrePharmacopeeDto;
import com.pharmacopee.ladafura.dto.population.produit.PopulationProduitDetailResponse;
import com.pharmacopee.ladafura.dto.population.produit.PopulationProduitSummaryResponse;

public interface IPopulationProduitService {

    /**
     * Liste paginée des produits validés avec filtres optionnels par mot-clé, catégorie et prix maximum.
     */
    Page<PopulationProduitSummaryResponse> listerProduits(String keyword, Long categorieId, Double prixMax, Pageable pageable);

    /**
     * Fiche détaillée d'un produit validé incluant sa composition détaillée, plantes, maladies associées,
     * et l'ensemble des offres des pharmacopées agréées avec disponibilité, prix et modes de retrait.
     */
    PopulationProduitDetailResponse getProduitDetail(Long id);

    /**
     * Liste spécifique des offres de pharmacopées proposant ce produit avec prix et disponibilité.
     */
    List<PopulationOffrePharmacopeeDto> getOffresByProduit(Long id);
}
