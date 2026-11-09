package com.pharmacopee.ladafura.services.interfaces;

import com.pharmacopee.ladafura.Models.Commande;
import com.pharmacopee.ladafura.Models.MembrePharmacopee;
import com.pharmacopee.ladafura.Models.Paiement;

/**
 * Effets d'une commande en ligne sur le stock et la trésorerie de la pharmacopée, communs aux espaces
 * population et pharmacopée. S'exécute obligatoirement dans la transaction de l'appelant.
 */
public interface ICommandeStockCaisseService {

    /**
     * Retire du stock les lignes d'une commande déjà enregistrée.
     *
     * @throws com.pharmacopee.ladafura.exceptions.ConflictException si la quantité vendable est insuffisante
     */
    void sortirStock(Commande commande);

    /** Réintègre le stock d'une commande annulée, y compris pour une commande antérieure aux mouvements de stock. */
    void reintegrerStock(Commande commande, MembrePharmacopee auteur);

    /**
     * Enregistre l'entrée en trésorerie d'un paiement réussi.
     *
     * @param caisseId caisse physique choisie pour des espèces ; facultative s'il n'y a qu'une session ouverte
     * @throws com.pharmacopee.ladafura.exceptions.ConflictException si le paiement est déjà encaissé,
     *         si aucun compte actif ne correspond au mode de paiement ou si aucune session de caisse n'est ouverte
     * @throws com.pharmacopee.ladafura.exceptions.BadRequestException si plusieurs caisses sont ouvertes sans choix
     */
    void encaisser(Paiement paiement, Long caisseId, MembrePharmacopee auteur);

    /** Contre-passe l'encaissement éventuel et passe le paiement à ANNULE. Sans effet si la commande n'a pas de paiement. */
    void annulerEncaissement(Commande commande, MembrePharmacopee auteur);
}
