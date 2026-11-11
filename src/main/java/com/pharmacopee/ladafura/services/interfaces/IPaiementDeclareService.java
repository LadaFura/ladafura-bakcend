package com.pharmacopee.ladafura.services.interfaces;

import java.util.List;

import com.pharmacopee.ladafura.Models.Paiement;
import com.pharmacopee.ladafura.dto.population.paiement.PopulationCompteMobileMoneyResponse;
import com.pharmacopee.ladafura.enums.MethodePaiement;

/**
 * Paiement Mobile Money sans API d'opérateur : le client paie sur le compte de la pharmacopée puis
 * déclare son numéro de transaction ; seule la pharmacopée, qui voit son compte, peut confirmer la réception.
 */
public interface IPaiementDeclareService {

    /** @throws com.pharmacopee.ladafura.exceptions.BadRequestException pour la carte, faute de prestataire */
    void exigerMethodeDisponible(MethodePaiement methode);

    /**
     * Enregistre la déclaration sur le paiement, qui reste en attente de vérification.
     *
     * @throws com.pharmacopee.ladafura.exceptions.BadRequestException champ manquant ou compte non valable pour cette pharmacopée
     * @throws com.pharmacopee.ladafura.exceptions.ConflictException numéro de transaction déjà déclaré sur ce compte
     */
    void declarerMobileMoney(Paiement paiement, Long pharmacopeeId, Long compteId, String referenceTransaction, String telephone);

    /** Comptes à payer, vide si la pharmacopée n'est pas validée ou n'a renseigné aucun numéro. */
    List<PopulationCompteMobileMoneyResponse> getComptesMobileMoney(Long pharmacopeeId);
}
