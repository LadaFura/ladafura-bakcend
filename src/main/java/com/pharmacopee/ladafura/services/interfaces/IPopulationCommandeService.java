package com.pharmacopee.ladafura.services.interfaces;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.pharmacopee.ladafura.dto.population.commande.PopulationCommandeDetailResponse;
import com.pharmacopee.ladafura.dto.population.commande.PopulationCommandeRecapitulatifRequest;
import com.pharmacopee.ladafura.dto.population.commande.PopulationCommandeRecapitulatifResponse;
import com.pharmacopee.ladafura.dto.population.commande.PopulationCommandeStatutResponse;
import com.pharmacopee.ladafura.dto.population.commande.PopulationCommandeSummaryResponse;
import com.pharmacopee.ladafura.dto.population.commande.PopulationCreateCommandeRequest;
import com.pharmacopee.ladafura.enums.StatutCommande;

public interface IPopulationCommandeService {

    /**
     * Calcule le récapitulatif chiffré avant confirmation (disponibilité, prix, frais de livraison, totaux).
     */
    PopulationCommandeRecapitulatifResponse getRecapitulatif(PopulationCommandeRecapitulatifRequest request);

    /**
     * Confirme et valide définitivement la commande à partir du panier actif, décrémente les stocks et vide le panier.
     */
    PopulationCommandeDetailResponse passerCommande(PopulationCreateCommandeRequest request);

    /**
     * Récupère l'historique paginé des commandes de l'utilisateur connecté avec possibilité de filtrer par statut.
     */
    Page<PopulationCommandeSummaryResponse> getHistoriqueCommandes(StatutCommande statut, Pageable pageable);

    /**
     * Récupère la fiche détaillée d'une commande appartenant à l'utilisateur connecté.
     */
    PopulationCommandeDetailResponse getCommandeDetail(Long commandeId);

    /**
     * Consulte le statut en temps réel et les informations d'acheminement d'une commande.
     */
    PopulationCommandeStatutResponse getCommandeStatut(Long commandeId);

    /**
     * Annule une commande lorsque son statut actuel le permet (EN_ATTENTE ou CONFIRMEE) et réintègre les stocks.
     */
    PopulationCommandeDetailResponse annulerCommande(Long commandeId);
}
