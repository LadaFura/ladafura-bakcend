package com.pharmacopee.ladafura.services.interfaces;

import com.pharmacopee.ladafura.Models.Pharmacopee;
import com.pharmacopee.ladafura.Models.Utilisateur;
import com.pharmacopee.ladafura.dto.pharmacopee.auth.PharmacopeeAuthResponse;

public interface IPharmacopeeAuthService {

    /**
     * Récupère l'utilisateur actuellement authentifié via Spring Security (Firebase Token).
     * Vérifie que le compte est actif et possède le rôle PHARMACOPEE.
     */
    Utilisateur getCurrentUtilisateur();

    /**
     * Récupère l'entité Pharmacopee associée à l'utilisateur actuellement connecté.
     * Lève une ResourceNotFoundException si l'utilisateur n'a pas encore de pharmacopée associée.
     */
    Pharmacopee getCurrentPharmacopee();

    /**
     * Récupère la liste de toutes les structures associées à l'utilisateur / praticien connecté.
     */
    java.util.List<Pharmacopee> getMyPharmacopees();

    /**
     * Renvoie le profil résumé d'authentification de la pharmacopée connectée avec structures et quotas.
     */
    PharmacopeeAuthResponse getMe();

    /**
     * Vérifie strictement que l'utilisateur connecté est le propriétaire de la pharmacopée demandée.
     * Lève une ForbiddenException si les identifiants ne correspondent pas.
     */
    void verifyPharmacopeeOwnership(Long requestedPharmacopeeId);

    /**
     * Vérifie que la pharmacopée connectée possède le statut VALIDE.
     * Lève une ForbiddenException si la pharmacopée est EN_ATTENTE, SUSPENDU ou REJETE.
     */
    void verifyPharmacopeeValidated();
    void verifyPraticienPrincipal();
}
