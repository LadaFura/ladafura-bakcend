package com.pharmacopee.ladafura.services.interfaces;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.pharmacopee.ladafura.dto.pharmacopee.paiement.EncaisserCashRequest;
import com.pharmacopee.ladafura.dto.pharmacopee.paiement.PharmacopeePaiementResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.paiement.PharmacopeePaiementSummaryResponse;
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
     * Validation d'un encaissement en espèces (Cash) lors du retrait au guichet ou à la livraison.
     */
    PharmacopeePaiementResponse encaisserCash(Long paiementId, EncaisserCashRequest request);

    /**
     * Confirme qu'un paiement Mobile Money déclaré est bien arrivé sur le compte : entrée en trésorerie et commande confirmée.
     */
    PharmacopeePaiementResponse confirmerReception(Long paiementId);

    /**
     * Rejette un paiement Mobile Money déclaré. La commande reste en attente : le client peut corriger sa déclaration ou l'annuler.
     */
    PharmacopeePaiementResponse rejeterPaiement(Long paiementId, String motif);
}
