package com.pharmacopee.ladafura.services.interfaces;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.pharmacopee.ladafura.dto.pharmacopee.commande.PharmacopeeCommandeDetailResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.commande.PharmacopeeCommandeItemResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.commande.PharmacopeeCommandeSummaryResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.commande.UpdateStatutCommandeRequest;
import com.pharmacopee.ladafura.enums.StatutCommande;

public interface IPharmacopeeCommandeService {

    /**
     * Liste paginée des commandes de l'officine avec filtre optionnel par statut.
     * Isole strictement les données de la pharmacopée connectée.
     */
    Page<PharmacopeeCommandeItemResponse> getCommandes(StatutCommande statut, Pageable pageable);

    /**
     * Synthèse chiffrée et tableau de bord des commandes de l'officine.
     */
    PharmacopeeCommandeSummaryResponse getSummary();

    /**
     * Fiche détaillée complète d'une commande (acheteur, articles, mode retrait, paiement, statuts autorisés).
     */
    PharmacopeeCommandeDetailResponse getCommandeDetail(Long id);

    /**
     * Mise à jour du statut d'une commande selon la matrice stricte des transitions autorisées.
     */
    PharmacopeeCommandeDetailResponse updateStatut(Long id, UpdateStatutCommandeRequest request);

    /**
     * Accepte et confirme la commande reçue (EN_ATTENTE -> CONFIRMEE).
     */
    PharmacopeeCommandeDetailResponse confirmerCommande(Long id);

    /**
     * Marque la commande comme préparée et emballée en officine (CONFIRMEE -> PREPAREE).
     */
    PharmacopeeCommandeDetailResponse preparerCommande(Long id);

    /**
     * Expédie ou met à disposition la commande préparée :
     * - Si Livraison : PREPAREE -> EN_LIVRAISON
     * - Si Pickup : PREPAREE -> DISPONIBLE_PICKUP
     */
    PharmacopeeCommandeDetailResponse acheminerCommande(Long id);

    /**
     * Clôture avec succès la commande acheminée :
     * - Si Livraison : EN_LIVRAISON -> LIVREE
     * - Si Pickup : DISPONIBLE_PICKUP -> RETIREE
     */
    PharmacopeeCommandeDetailResponse finaliserCommande(Long id);

    /**
     * Annule une commande en cours avec motif optionnel.
     */
    PharmacopeeCommandeDetailResponse annulerCommande(Long id, String motif);
}
