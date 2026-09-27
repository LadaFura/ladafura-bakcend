package com.pharmacopee.ladafura.services.interfaces;

import com.pharmacopee.ladafura.dto.pharmacopee.referencement.DemandeReferencementRequest;
import com.pharmacopee.ladafura.dto.pharmacopee.referencement.StatutReferencementResponse;

public interface IPharmacopeeReferencementService {

    /**
     * Soumet une nouvelle demande de référencement pour l'utilisateur pharmacopée connecté.
     * Initialise le statut à EN_ATTENTE.
     */
    StatutReferencementResponse soumettreDemandeReferencement(DemandeReferencementRequest request);

    /**
     * Consulte l'état actuel de la demande d'agrément et les informations associées.
     */
    StatutReferencementResponse consulterStatutReferencement();
}
