package com.pharmacopee.ladafura.services.interfaces;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.pharmacopee.ladafura.dto.population.paiement.PopulationMethodePaiementInfoDto;
import com.pharmacopee.ladafura.dto.population.paiement.PopulationPaiementResponse;
import com.pharmacopee.ladafura.dto.population.paiement.PopulationProcessPaiementRequest;

public interface IPopulationPaiementService {

    /**
     * Retourne la liste des moyens de paiement supportés avec consignes pour le client.
     */
    List<PopulationMethodePaiementInfoDto> getMethodesPaiement();

    /**
     * Exécute ou initie le règlement d'une commande appartenant à l'utilisateur connecté.
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
