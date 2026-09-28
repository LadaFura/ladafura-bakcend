package com.pharmacopee.ladafura.services.interfaces;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.pharmacopee.ladafura.dto.population.historique.PopulationDepensesSyntheseResponse;
import com.pharmacopee.ladafura.dto.population.historique.PopulationJournalActiviteItem;
import com.pharmacopee.ladafura.dto.population.historique.PopulationProduitAcheteItem;
import com.pharmacopee.ladafura.dto.population.historique.PopulationReleveActiviteResponse;
import com.pharmacopee.ladafura.enums.TypeEvenementHistorique;

public interface IPopulationHistoriqueService {

    /**
     * Récupère le journal d'activité chronologique consolidé du client (commandes, paiements, avis, favoris, notifications).
     */
    Page<PopulationJournalActiviteItem> getJournalActivite(
            TypeEvenementHistorique type,
            LocalDateTime dateDebut,
            LocalDateTime dateFin,
            Pageable pageable);

    /**
     * Calcule la synthèse financière et analytique des dépenses du client.
     */
    PopulationDepensesSyntheseResponse getSyntheseDepenses();

    /**
     * Récupère l'historique consolidé des produits distincts achetés et reçus par le client.
     */
    Page<PopulationProduitAcheteItem> getHistoriqueProduitsAchetes(Pageable pageable);

    /**
     * Génère un relevé complet de synthèse de l'activité du compte client.
     */
    PopulationReleveActiviteResponse getReleveActivite();
}
