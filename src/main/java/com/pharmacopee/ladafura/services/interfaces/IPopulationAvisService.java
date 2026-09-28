package com.pharmacopee.ladafura.services.interfaces;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.pharmacopee.ladafura.dto.population.avis.PopulationAvisResponse;
import com.pharmacopee.ladafura.dto.population.avis.PopulationCreateAvisRequest;
import com.pharmacopee.ladafura.dto.population.avis.PopulationEligibiliteAvisResponse;
import com.pharmacopee.ladafura.dto.population.avis.PopulationUpdateAvisRequest;
import com.pharmacopee.ladafura.enums.StatutAvis;

public interface IPopulationAvisService {

    /**
     * Vérifie si l'utilisateur est éligible pour déposer un avis sur ce produit (commande livrée ou retirée).
     */
    PopulationEligibiliteAvisResponse verifierEligibiliteAvis(Long produitId);

    /**
     * Crée et soumet un nouvel avis client vérifié (statut EN_ATTENTE de modération par l'administrateur).
     */
    PopulationAvisResponse creerAvis(PopulationCreateAvisRequest request);

    /**
     * Met à jour la note ou le commentaire d'un avis appartenant à l'utilisateur connecté.
     */
    PopulationAvisResponse modifierAvis(Long avisId, PopulationUpdateAvisRequest request);

    /**
     * Supprime définitivement son propre avis.
     */
    void supprimerAvis(Long avisId);

    /**
     * Récupère la liste paginée de tous les avis déposés par l'utilisateur connecté.
     */
    Page<PopulationAvisResponse> getMesAvis(StatutAvis statut, Pageable pageable);

    /**
     * Récupère le détail d'un avis appartenant à l'utilisateur.
     */
    PopulationAvisResponse getAvisDetail(Long avisId);
}
