package com.pharmacopee.ladafura.services.interfaces;

import com.pharmacopee.ladafura.dto.pharmacopee.profil.PharmacopeeProfileResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.profil.PharmacopeeUpdateProfileRequest;

public interface IPharmacopeeProfileService {

    /**
     * Récupère le profil complet de la pharmacopée connectée (informations de base, statut,
     * coordonnées du responsable, localisation géographique, modes de retrait et statistiques).
     */
    PharmacopeeProfileResponse getProfile();

    /**
     * Met à jour les informations modifiables du profil de la pharmacopée connectée
     * (nom, description, téléphone, téléphone du responsable et localisation optionnelle).
     */
    PharmacopeeProfileResponse updateProfile(PharmacopeeUpdateProfileRequest request);
}
