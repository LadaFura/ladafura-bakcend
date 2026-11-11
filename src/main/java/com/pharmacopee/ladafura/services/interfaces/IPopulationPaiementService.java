package com.pharmacopee.ladafura.services.interfaces;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.pharmacopee.ladafura.dto.population.paiement.PopulationCompteMobileMoneyResponse;
import com.pharmacopee.ladafura.dto.population.paiement.PopulationMethodePaiementInfoDto;
import com.pharmacopee.ladafura.dto.population.paiement.PopulationPaiementResponse;
import com.pharmacopee.ladafura.dto.population.paiement.PopulationProcessPaiementRequest;

public interface IPopulationPaiementService {

    /**
     * Retourne la liste des moyens de paiement supportés avec consignes pour le client.
     */
    List<PopulationMethodePaiementInfoDto> getMethodesPaiement();

    /**
     * Comptes Mobile Money sur lesquels payer une pharmacopée ; une liste vide signifie que ce moyen n'est pas proposé.
     */
    List<PopulationCompteMobileMoneyResponse> getComptesMobileMoney(Long pharmacopeeId);

    /**
     * Déclare le règlement d'une commande de l'utilisateur connecté. Un Mobile Money reste en attente
     * jusqu'à la confirmation de la pharmacopée.
     */
    PopulationPaiementResponse payerCommande(PopulationProcessPaiementRequest request);

    /**
     * Récupère le statut et le reçu de règlement lié à une commande de l'utilisateur.
     */
    PopulationPaiementResponse getPaiementByCommande(Long commandeId);

    /**
     * Récupère l'historique paginé des transactions financières effectuées par l'utilisateur connecté.
     */
    Page<PopulationPaiementResponse> getHistoriquePaiements(Pageable pageable);
}
