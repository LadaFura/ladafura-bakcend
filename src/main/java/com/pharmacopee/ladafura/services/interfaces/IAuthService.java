package com.pharmacopee.ladafura.services.interfaces;

import com.pharmacopee.ladafura.dto.auth.AuthMeResponse;

/**
 * Service neutre d'authentification et de résolution de session.
 */
public interface IAuthService {

    /**
     * Récupère les informations de l'utilisateur authentifié sans restriction de rôle spécifique.
     * Permet à l'application mobile de résoudre le profil et le rôle (POPULATION / AGENT_COLLECTE).
     *
     * @return AuthMeResponse contenant les informations utilisateur et son rôle.
     */
    AuthMeResponse getCurrentUser();
}
