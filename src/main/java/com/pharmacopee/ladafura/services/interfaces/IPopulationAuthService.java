package com.pharmacopee.ladafura.services.interfaces;

import com.pharmacopee.ladafura.Models.Utilisateur;
import com.pharmacopee.ladafura.dto.population.auth.PopulationAuthResponse;
import com.pharmacopee.ladafura.dto.population.auth.PopulationRegisterRequest;
import com.pharmacopee.ladafura.dto.population.auth.PopulationSyncRequest;

public interface IPopulationAuthService {

    /**
     * Inscrit un nouvel utilisateur de la population dans Firebase Auth et en base MySQL.
     * Attribue automatiquement le rôle POPULATION.
     *
     * @param request Formulaire d'inscription (nom, prénom, email, mot de passe, téléphone)
     * @return Les informations de l'utilisateur inscrit
     */
    PopulationAuthResponse register(PopulationRegisterRequest request);

    /**
     * Récupère l'entité Utilisateur connectée et vérifie qu'elle possède le rôle POPULATION.
     *
     * @return L'entité Utilisateur connectée
     */
    Utilisateur getCurrentPopulationUser();

    /**
     * Récupère les données d'identité et de session de l'utilisateur Population connecté.
     *
     * @return Les informations de session de l'utilisateur connecté
     */
    PopulationAuthResponse getMe();

    /**
     * Synchronise le profil d'un utilisateur s'étant inscrit ou connecté directement via Firebase (ex: client SDK).
     *
     * @param request Informations complémentaires éventuelles (nom, prénom, téléphone)
     * @return Les informations de l'utilisateur synchronisé
     */
    PopulationAuthResponse syncFirebaseUser(PopulationSyncRequest request);
}
