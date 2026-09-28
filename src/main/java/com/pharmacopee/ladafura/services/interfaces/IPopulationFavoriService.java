package com.pharmacopee.ladafura.services.interfaces;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.pharmacopee.ladafura.dto.population.favori.PopulationFavoriCheckResponse;
import com.pharmacopee.ladafura.dto.population.favori.PopulationFavoriCountResponse;
import com.pharmacopee.ladafura.dto.population.favori.PopulationFavoriItemResponse;
import com.pharmacopee.ladafura.dto.population.favori.PopulationToggleFavoriRequest;
import com.pharmacopee.ladafura.dto.population.favori.PopulationToggleFavoriResponse;
import com.pharmacopee.ladafura.dto.population.pharmacopee.PopulationPharmacopeeSummaryResponse;
import com.pharmacopee.ladafura.dto.population.plante.PopulationPlanteSummaryResponse;
import com.pharmacopee.ladafura.dto.population.produit.PopulationProduitSummaryResponse;
import com.pharmacopee.ladafura.enums.TypeFavori;

public interface IPopulationFavoriService {

    /**
     * Basculer l'état favori d'une cible (ajoute si absent, supprime si présent).
     */
    PopulationToggleFavoriResponse toggleFavori(PopulationToggleFavoriRequest request);

    /**
     * Ajouter une plante aux favoris de manière explicite.
     */
    PopulationToggleFavoriResponse ajouterPlanteFavori(Long planteId);

    /**
     * Retirer une plante des favoris de manière explicite.
     */
    PopulationToggleFavoriResponse supprimerPlanteFavori(Long planteId);

    /**
     * Ajouter un produit aux favoris de manière explicite.
     */
    PopulationToggleFavoriResponse ajouterProduitFavori(Long produitId);

    /**
     * Retirer un produit des favoris de manière explicite.
     */
    PopulationToggleFavoriResponse supprimerProduitFavori(Long produitId);

    /**
     * Ajouter une pharmacopée aux favoris de manière explicite.
     */
    PopulationToggleFavoriResponse ajouterPharmacopeeFavori(Long pharmacopeeId);

    /**
     * Retirer une pharmacopée des favoris de manière explicite.
     */
    PopulationToggleFavoriResponse supprimerPharmacopeeFavori(Long pharmacopeeId);

    /**
     * Supprimer un favori par son identifiant unique avec contrôle d'appartenance.
     */
    void supprimerFavoriById(Long favoriId);

    /**
     * Vérifier si une cible est actuellement dans les favoris du client connecté.
     */
    PopulationFavoriCheckResponse checkFavori(TypeFavori type, Long cibleId);

    /**
     * Obtenir les compteurs totaux des favoris (global et par type) de l'utilisateur connecté.
     */
    PopulationFavoriCountResponse getFavoriCounts();

    /**
     * Obtenir la liste paginée de tous les favoris avec filtre optionnel par type.
     */
    Page<PopulationFavoriItemResponse> getMesFavoris(TypeFavori type, Pageable pageable);

    /**
     * Obtenir la liste paginée des plantes favorites.
     */
    Page<PopulationPlanteSummaryResponse> getMesPlantesFavorites(Pageable pageable);

    /**
     * Obtenir la liste paginée des produits favoris.
     */
    Page<PopulationProduitSummaryResponse> getMesProduitsFavoris(Pageable pageable);

    /**
     * Obtenir la liste paginée des pharmacopées favorites.
     */
    Page<PopulationPharmacopeeSummaryResponse> getMesPharmacopeesFavorites(Pageable pageable);
}
