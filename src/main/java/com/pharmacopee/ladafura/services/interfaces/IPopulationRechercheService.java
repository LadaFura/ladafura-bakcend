package com.pharmacopee.ladafura.services.interfaces;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.pharmacopee.ladafura.dto.population.recherche.PopulationGlobalSearchResponse;
import com.pharmacopee.ladafura.dto.population.recherche.PopulationMaladieSearchItem;
import com.pharmacopee.ladafura.dto.population.recherche.PopulationPharmacopeeSearchItem;
import com.pharmacopee.ladafura.dto.population.recherche.PopulationPlanteSearchItem;
import com.pharmacopee.ladafura.dto.population.recherche.PopulationProduitSearchItem;
import com.pharmacopee.ladafura.dto.population.recherche.PopulationVernaculaireSearchItem;

public interface IPopulationRechercheService {

    /**
     * Recherche globale et unifiée (simple) à travers toutes les entités de LADAFURA.
     *
     * @param query Terme de recherche
     * @return PopulationGlobalSearchResponse consolidant plantes, vernaculaires, maladies, produits et pharmacopées
     */
    PopulationGlobalSearchResponse rechercheGlobale(String query);

    /**
     * Recherche paginée de plantes validées par mot-clé (nom scientifique ou description).
     *
     * @param query Terme de recherche optionnel
     * @param pageable Pagination et tri
     * @return Page de plantes correspondantes
     */
    Page<PopulationPlanteSearchItem> rechercherPlantes(String query, Pageable pageable);

    /**
     * Recherche paginée multicritère de noms vernaculaires par nom et/ou langue.
     *
     * @param nom Nom local (ex: Kinkéliba)
     * @param langue Langue locale (ex: Bambara)
     * @param pageable Pagination
     * @return Page de noms vernaculaires correspondants
     */
    Page<PopulationVernaculaireSearchItem> rechercherNomsVernaculaires(String nom, String langue, Pageable pageable);

    /**
     * Recherche paginée de maladies documentées.
     *
     * @param query Terme de recherche optionnel
     * @param pageable Pagination
     * @return Page de maladies correspondantes
     */
    Page<PopulationMaladieSearchItem> rechercherMaladies(String query, Pageable pageable);

    /**
     * Recherche paginée multicritère de produits validés par mot-clé, catégorie et/ou budget max.
     *
     * @param query Mot-clé optionnel
     * @param categorieId Identifiant de catégorie optionnel
     * @param prixMax Prix plafond en FCFA optionnel
     * @param pageable Pagination
     * @return Page de produits correspondants
     */
    Page<PopulationProduitSearchItem> rechercherProduits(String query, Long categorieId, Double prixMax, Pageable pageable);

    /**
     * Recherche paginée multicritère d'officines de pharmacopées agréées par mot-clé et critères géographiques.
     *
     * @param query Nom ou mot-clé optionnel
     * @param region Région administrative optionnelle
     * @param cercle Cercle optionnel
     * @param commune Commune optionnelle
     * @param pageable Pagination
     * @return Page d'officines correspondantes
     */
    Page<PopulationPharmacopeeSearchItem> rechercherPharmacopees(String query, String region, String cercle, String commune, Pageable pageable);
}
