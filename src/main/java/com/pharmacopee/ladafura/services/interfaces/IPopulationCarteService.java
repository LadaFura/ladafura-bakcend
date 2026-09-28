package com.pharmacopee.ladafura.services.interfaces;

import java.util.List;

import com.pharmacopee.ladafura.dto.population.cartographie.PopulationCarteDetailPharmacopeeResponse;
import com.pharmacopee.ladafura.dto.population.cartographie.PopulationCartePharmacopeeItem;
import com.pharmacopee.ladafura.dto.population.cartographie.PopulationCarteProduitItem;

public interface IPopulationCarteService {

    /**
     * Retourne les points d'intérêt des officines de pharmacopée agréées géolocalisées sur la carte.
     * Permet le filtrage par mot-clé, division administrative (région, cercle, commune), ou rayon kilométrique autour d'une position GPS.
     */
    List<PopulationCartePharmacopeeItem> getPharmacopeesSurCarte(
            String query, String region, String cercle, String commune,
            Double userLat, Double userLng, Double rayonKm);

    /**
     * Recherche et localise les officines proposant un produit donné sur la carte, triées par proximité kilométrique.
     */
    List<PopulationCarteProduitItem> localiserProduitSurCarte(
            Long produitId, String query,
            Double userLat, Double userLng, Double rayonKm,
            Boolean disponibleOnly);

    /**
     * Fiche cartographique complète d'une pharmacopée avec calcul de la distance estimée par rapport à l'utilisateur.
     */
    PopulationCarteDetailPharmacopeeResponse getPharmacopeeCarteDetail(
            Long id, Double userLat, Double userLng);
}
