package com.pharmacopee.ladafura.services.interfaces;

import com.pharmacopee.ladafura.Models.Pharmacopee;
import com.pharmacopee.ladafura.Models.Utilisateur;
import com.pharmacopee.ladafura.dto.pharmacopee.auth.PharmacopeeAuthResponse;

public interface IPharmacopeeAuthService {

    /**
     * Récupère l'utilisateur actuellement authentifié via Spring Security (Firebase Token).
     * Vérifie que le compte est actif et possède le rôle PHARMACOPEE ou une adhésion en cours.
     */
    Utilisateur getCurrentUtilisateur();

    /**
     * Récupère la pharmacopée de l'adhésion courante, voir {@link #getMembreCourant()}.
     */
    Pharmacopee getCurrentPharmacopee();

    /**
     * Récupère les pharmacopées dont l'utilisateur connecté est membre actif.
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

    /**
     * Adhésion active de l'utilisateur connecté dans la pharmacopée courante. {@code X-Pharmacopee-Id} n'est
     * qu'un choix parmi ses adhésions ; sans en-tête, la première pharmacopée VALIDEE est retenue, sinon la première.
     *
     * @throws com.pharmacopee.ladafura.exceptions.ForbiddenException si l'utilisateur n'est pas membre actif
     *         de la pharmacopée demandée ou d'aucune pharmacopée
     * @throws com.pharmacopee.ladafura.exceptions.BadRequestException si l'en-tête n'est pas numérique
     */
    com.pharmacopee.ladafura.Models.MembrePharmacopee getMembreCourant();

    /**
     * Inscrit un praticien principal et crée sa pharmacopée avec le statut EN_ATTENTE.
     */
    PharmacopeeAuthResponse registerPraticien(com.pharmacopee.ladafura.dto.pharmacopee.auth.PraticienRegisterRequest request);
}
