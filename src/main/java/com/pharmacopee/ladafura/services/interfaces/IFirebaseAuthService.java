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

    /**
     * Crée un compte utilisateur dans Firebase Authentication.
     *
     * @param email       Email de l'utilisateur
     * @param password    Mot de passe temporaire ou initial
     * @param displayName Nom complet de l'utilisateur
     * @return Le firebaseUid généré par Firebase
     */
    String createUser(String email, String password, String displayName);

    /**
     * Attribue un rôle sous forme de Custom Claim dans le token Firebase.
     *
     * @param uid  L'identifiant Firebase UID
     * @param role Le rôle (ex: ADMINISTRATEUR, AGENT_COLLECTE, PHARMACOPEE, POPULATION)
     */
    void setRole(String uid, String role);

    /**
     * Désactive un compte utilisateur dans Firebase Authentication.
     *
     * @param uid L'identifiant Firebase UID
     */
    void disableUser(String uid);

    /**
     * Réactive un compte utilisateur dans Firebase Authentication.
     *
     * @param uid L'identifiant Firebase UID
     */
    void enableUser(String uid);

    /**
     * Supprime un compte utilisateur dans Firebase Authentication.
     *
     * @param uid L'identifiant Firebase UID
     */
    void deleteUser(String uid);
}
