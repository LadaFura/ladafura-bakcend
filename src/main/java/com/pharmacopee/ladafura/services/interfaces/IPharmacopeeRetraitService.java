package com.pharmacopee.ladafura.services.interfaces;

import com.pharmacopee.ladafura.dto.pharmacopee.retrait.ConfigureModesRetraitGlobalRequest;
import com.pharmacopee.ladafura.dto.pharmacopee.retrait.ModeRetraitResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.retrait.PharmacopeeModesRetraitSummaryResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.retrait.UpdateModeRetraitRequest;
import com.pharmacopee.ladafura.enums.TypeModeRetrait;

public interface IPharmacopeeRetraitService {

    /**
     * Récupère la synthèse et la configuration des deux modes de retrait (Livraison et Pickup).
     * Si les modes n'ont pas encore été enregistrés en base, ils sont initialisés automatiquement.
     */
    PharmacopeeModesRetraitSummaryResponse getModesRetrait();

    /**
     * Récupère la configuration d'un mode spécifique (LIVRAISON ou PICKUP).
     */
    ModeRetraitResponse getModeRetrait(TypeModeRetrait type);

    /**
     * Met à jour un mode spécifique (activation/désactivation et frais applicables).
     */
    ModeRetraitResponse updateModeRetrait(TypeModeRetrait type, UpdateModeRetraitRequest request);

    /**
     * Bascule rapidement l'état d'activation d'un mode (actif <-> inactif).
     */
    ModeRetraitResponse toggleModeRetrait(TypeModeRetrait type);

    /**
     * Configure simultanément les deux modes de retrait en une seule requête.
     * Permet explicitement aux deux modes d'être actifs simultanément.
     */
    PharmacopeeModesRetraitSummaryResponse configureModesRetraitGlobal(ConfigureModesRetraitGlobalRequest request);
}
