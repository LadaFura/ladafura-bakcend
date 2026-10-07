package com.pharmacopee.ladafura.services.interfaces;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.pharmacopee.ladafura.dto.population.plante.PopulationConnaissanceTraditionnelleDto;
import com.pharmacopee.ladafura.dto.population.plante.PopulationEtudeScientifiqueDto;
import com.pharmacopee.ladafura.dto.population.plante.PopulationPlanteDetailResponse;
import com.pharmacopee.ladafura.dto.population.plante.PopulationPlanteSummaryResponse;
import com.pharmacopee.ladafura.dto.population.produit.PopulationProduitSummaryResponse;

public interface IPopulationPlanteService {

    /**
     * Liste paginée des plantes médicinales validées du catalogue LADAFURA.
     */
    Page<PopulationPlanteSummaryResponse> listerPlantes(Pageable pageable);

    /**
     * Consultation détaillée d'une plante validée (noms vernaculaires, savoirs traditionnels validés,
     * études scientifiques, maladies, médias et localités associées) avec avertissement médical strict.
     */
    PopulationPlanteDetailResponse getPlanteDetail(Long id);

    /**
     * Récupère spécifiquement les savoirs traditionnels validés associés à une plante.
     */
    List<PopulationConnaissanceTraditionnelleDto> getConnaissancesByPlante(Long id);

    /**
     * Récupère spécifiquement les études scientifiques associées à une plante.
     */
    List<PopulationEtudeScientifiqueDto> getEtudesByPlante(Long id);

    /**
     * Récupère les produits traditionnels associés à une plante médicinale.
     */
    List<PopulationProduitSummaryResponse> getProduitsByPlante(Long id);
}

