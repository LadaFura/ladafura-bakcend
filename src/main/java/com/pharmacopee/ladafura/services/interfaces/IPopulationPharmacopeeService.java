package com.pharmacopee.ladafura.services.interfaces;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.pharmacopee.ladafura.dto.population.pharmacopee.PopulationPharmacopeeDetailResponse;
import com.pharmacopee.ladafura.dto.population.pharmacopee.PopulationPharmacopeeModeRetraitDto;
import com.pharmacopee.ladafura.dto.population.pharmacopee.PopulationPharmacopeeProduitItemResponse;
import com.pharmacopee.ladafura.dto.population.pharmacopee.PopulationPharmacopeeSummaryResponse;

public interface IPopulationPharmacopeeService {

    /**
     * Recherche et liste paginée des officines de pharmacopée agréées avec filtres géographiques (région, cercle, commune) et mot-clé.
     */
    Page<PopulationPharmacopeeSummaryResponse> listerPharmacopees(
            String keyword, String region, String cercle, String commune, Pageable pageable);

    /**
     * Liste des officines de pharmacopée agréées les plus proches de la position GPS du citoyen (maximum 10).
     */
    List<PopulationPharmacopeeSummaryResponse> listerPharmacopeesProximite(
            Double latitude, Double longitude, int limit);

    /**
     * Fiche détaillée complète d'une officine de pharmacopée (profil, contact, localisation GPS, modes de retrait, avis).
     */
    PopulationPharmacopeeDetailResponse getPharmacopeeDetail(Long id);

    /**
     * Liste paginée des produits de pharmacopée validés proposés par cette officine, avec filtre optionnel sur le stock disponible.
     */
    Page<PopulationPharmacopeeProduitItemResponse> getProduitsByPharmacopee(
            Long id, Boolean disponibleOnly, Pageable pageable);

    /**
     * Modes de retrait proposés par l'officine (Livraison et/ou Pickup) avec frais et statuts d'activation.
     */
    List<PopulationPharmacopeeModeRetraitDto> getModesRetraitByPharmacopee(Long id);
}
