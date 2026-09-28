package com.pharmacopee.ladafura.services.interfaces;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.pharmacopee.ladafura.dto.pharmacopee.paiement.EncaisserCashRequest;
import com.pharmacopee.ladafura.dto.pharmacopee.paiement.PharmacopeePaiementResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.paiement.PharmacopeePaiementSummaryResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.paiement.SimulerPaiementRequest;
import com.pharmacopee.ladafura.enums.MethodePaiement;
import com.pharmacopee.ladafura.enums.StatutPaiement;

public interface IPharmacopeePaiementService {

    /**
     * Liste paginée des règlements financiers liés aux commandes de l'officine avec filtres optionnels.
     */
    Page<PharmacopeePaiementResponse> getPaiements(StatutPaiement statut, MethodePaiement methode, Pageable pageable);

    /**
     * Synthèse financière : encaissements globaux et ventilation par canal (Mobile Money, Cash, CB).
     */
    PharmacopeePaiementSummaryResponse getSummary();

    /**
     * Détail d'un paiement spécifique.
     */
    PharmacopeePaiementResponse getPaiementDetail(Long id);

    /**
     * Détail du paiement rattaché à une commande donnée.
     */
    PharmacopeePaiementResponse getPaiementByCommande(Long commandeId);

    /**
     * Simulation de paiement pour les tests (Mobile Money, Cash, Carte Bancaire).
     * Enregistre ou met à jour la transaction et synchronise le statut de la commande.
     */
    PharmacopeePaiementResponse simulerPaiement(SimulerPaiementRequest request);

    /**
     * Validation d'un encaissement en espèces (Cash) lors du retrait au guichet ou à la livraison.
     */
    PharmacopeePaiementResponse encaisserCash(Long paiementId, EncaisserCashRequest request);
}
