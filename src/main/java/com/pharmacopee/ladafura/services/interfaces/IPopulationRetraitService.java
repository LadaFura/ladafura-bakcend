package com.pharmacopee.ladafura.services.interfaces;

import com.pharmacopee.ladafura.dto.population.retrait.PopulationEstimationRetraitRequest;
import com.pharmacopee.ladafura.dto.population.retrait.PopulationEstimationRetraitResponse;
import com.pharmacopee.ladafura.dto.population.retrait.PopulationPharmacopeeRetraitOptionsResponse;

public interface IPopulationRetraitService {

    /**
     * Consulte l'ensemble des modes de retrait configurés pour une officine (Livraison et/ou Pickup, tarifs, adresses).
     */
    PopulationPharmacopeeRetraitOptionsResponse getOptionsRetraitPharmacopee(Long pharmacopeeId);

    /**
     * Vérifie l'éligibilité et calcule le montant exact des frais pour le mode de mise à disposition sélectionné.
     */
    PopulationEstimationRetraitResponse estimerOptionRetrait(PopulationEstimationRetraitRequest request);
}
