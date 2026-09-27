package com.pharmacopee.ladafura.services.interfaces;

import com.pharmacopee.ladafura.dto.pharmacopee.localisation.PharmacopeeLocalisationRequest;
import com.pharmacopee.ladafura.dto.pharmacopee.localisation.PharmacopeeLocalisationResponse;

public interface IPharmacopeeLocalisationService {

    /**
     * Consulte les informations géographiques et GPS de la pharmacopée connectée.
     * Lève une ResourceNotFoundException si aucune localisation n'a encore été configurée.
     */
    PharmacopeeLocalisationResponse getLocalisation();

    /**
     * Enregistre ou met à jour les informations géographiques et GPS de la pharmacopée connectée.
     * Établit ou met à jour l'association Pharmacopée 1 ───── 1 Localisation.
     */
    PharmacopeeLocalisationResponse saveOrUpdateLocalisation(PharmacopeeLocalisationRequest request);
}
