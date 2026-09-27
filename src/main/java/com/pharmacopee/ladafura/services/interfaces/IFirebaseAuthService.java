package com.pharmacopee.ladafura.services.interfaces;

import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.auth.UserRecord;

public interface IFirebaseAuthService {

    /**
     * Vérifie la signature et la validité d'un Firebase ID Token.
     *
     * @param idToken Le token JWT envoyé par Flutter ou Angular
     * @return L'objet FirebaseToken décodé
     * @throws FirebaseAuthException Si le token est invalide ou expiré
     */
    FirebaseToken verifyIdToken(String idToken) throws FirebaseAuthException;

    /**
     * Extrait l'identifiant unique Firebase (UID) à partir d'un token valide.
     *
     * @param idToken Le token JWT
     * @return Le firebaseUid
     * @throws FirebaseAuthException Si le token est invalide
     */
    String extractFirebaseUid(String idToken) throws FirebaseAuthException;

    /**
     * Extrait l'adresse email de l'utilisateur à partir d'un token valide.
     *
     * @param idToken Le token JWT
     * @return L'email de l'utilisateur
     * @throws FirebaseAuthException Si le token est invalide
     */
    String extractEmail(String idToken) throws FirebaseAuthException;

    /**
     * Récupère les métadonnées complètes de l'utilisateur depuis Firebase Authentication.
     *
     * @param firebaseUid L'identifiant unique Firebase
     * @return L'enregistrement UserRecord Firebase
     * @throws FirebaseAuthException Si l'utilisateur n'existe pas dans Firebase
     */
    UserRecord getFirebaseUser(String firebaseUid) throws FirebaseAuthException;
}
