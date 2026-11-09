package com.pharmacopee.ladafura.services.interfaces;

import java.math.BigDecimal;

import com.pharmacopee.ladafura.Models.Depense;
import com.pharmacopee.ladafura.Models.MembrePharmacopee;
import com.pharmacopee.ladafura.Models.OperationCaisse;
import com.pharmacopee.ladafura.Models.Paiement;
import com.pharmacopee.ladafura.Models.PaiementVente;
import com.pharmacopee.ladafura.Models.ReglementAchat;
import com.pharmacopee.ladafura.enums.MethodePaiement;
import com.pharmacopee.ladafura.enums.NatureOperationCaisse;
import com.pharmacopee.ladafura.enums.SensOperation;

import lombok.Builder;

/**
 * Seul point d'écriture de la trésorerie. Une opération est immuable : une correction passe par
 * {@link #contrePasser}. S'exécute obligatoirement dans la transaction de l'appelant.
 */
public interface IPharmacopeeOperationCaisseService {

    /** Opération à enregistrer ; {@code methode} est nulle hors paiement, et au plus une origine est renseignée. */
    @Builder
    record NouvelleOperation(Long caisseId, SensOperation sens, NatureOperationCaisse nature, BigDecimal montant,
                             MethodePaiement methode, String libelle, MembrePharmacopee auteur,
                             PaiementVente paiementVente, Paiement paiement, ReglementAchat reglementAchat, Depense depense,
                             OperationCaisse operationLiee) {
    }

    /**
     * @throws com.pharmacopee.ladafura.exceptions.ResourceNotFoundException si la caisse n'appartient pas à la pharmacopée
     * @throws com.pharmacopee.ladafura.exceptions.BadRequestException si la caisse est inactive, le montant non positif,
     *         plusieurs origines sont renseignées, ou le mode de paiement est incompatible avec la caisse
     * @throws com.pharmacopee.ladafura.exceptions.ConflictException si une caisse physique n'a pas de session ouverte
     *         ou si la sortie rendrait son solde négatif
     */
    OperationCaisse enregistrer(Long pharmacopeeId, NouvelleOperation operation);

    /**
     * Crée l'opération inverse, liée à l'originale.
     *
     * @throws com.pharmacopee.ladafura.exceptions.ConflictException si l'opération est déjà contre-passée
     */
    OperationCaisse contrePasser(Long pharmacopeeId, Long operationId, String libelle, MembrePharmacopee auteur);

    /**
     * Caisse physique : fond de la session ouverte plus ses opérations, ou montant constaté à la dernière clôture.
     * Autres comptes : entrées moins sorties.
     */
    BigDecimal soldeTheorique(Long pharmacopeeId, Long caisseId);
}
