package com.pharmacopee.ladafura.services.impl;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.auth.UserRecord;
import com.pharmacopee.ladafura.exceptions.FirebaseAuthenticationException;
import com.pharmacopee.ladafura.services.interfaces.IFirebaseAuthService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class FirebaseAuthServiceImpl implements IFirebaseAuthService {

    private final FirebaseAuth firebaseAuth;

    public FirebaseAuthServiceImpl(@Autowired(required = false) FirebaseAuth firebaseAuth) {
        this.firebaseAuth = firebaseAuth;
    }

    @Override
    public FirebaseToken verifyIdToken(String idToken) throws FirebaseAuthException {
        ensureFirebaseAuthAvailable();
        return firebaseAuth.verifyIdToken(idToken);
    }

    @Override
    public String extractFirebaseUid(String idToken) throws FirebaseAuthException {
        FirebaseToken token = verifyIdToken(idToken);
        return token.getUid();
    }

    @Override
    public String extractEmail(String idToken) throws FirebaseAuthException {
        FirebaseToken token = verifyIdToken(idToken);
        return token.getEmail();
    }

    @Override
    public UserRecord getFirebaseUser(String firebaseUid) throws FirebaseAuthException {
        ensureFirebaseAuthAvailable();
        return firebaseAuth.getUser(firebaseUid);
    }

    @Override
    public String createUser(String email, String password, String displayName) {
        ensureFirebaseAuthAvailable();
        try {
            UserRecord.CreateRequest request = new UserRecord.CreateRequest()
                    .setEmail(email)
                    .setPassword(password)
                    .setDisplayName(displayName)
                    .setEmailVerified(true);
            UserRecord userRecord = firebaseAuth.createUser(request);
            log.info("Compte Firebase créé avec succès pour {} (UID: {})", email, userRecord.getUid());
            return userRecord.getUid();
        } catch (FirebaseAuthException e) {
            log.error("Erreur lors de la création du compte Firebase pour {} : {}", email, e.getMessage());
            throw new FirebaseAuthenticationException("Impossible de créer l'utilisateur dans Firebase : " + e.getMessage(), e);
        }
    }

    @Override
    public void setRole(String uid, String role) {
        ensureFirebaseAuthAvailable();
        try {
            Map<String, Object> claims = new HashMap<>();
            claims.put("role", role);
            firebaseAuth.setCustomUserClaims(uid, claims);
            log.info("Custom claim 'role={}' attribué à l'UID {}", role, uid);
        } catch (FirebaseAuthException e) {
            log.error("Erreur lors de l'attribution du rôle Firebase à l'UID {} : {}", uid, e.getMessage());
            throw new FirebaseAuthenticationException("Impossible d'attribuer le rôle dans Firebase : " + e.getMessage(), e);
        }
    }

    @Override
    public void disableUser(String uid) {
        ensureFirebaseAuthAvailable();
        try {
            UserRecord.UpdateRequest request = new UserRecord.UpdateRequest(uid)
                    .setDisabled(true);
            firebaseAuth.updateUser(request);
            log.info("Compte Firebase désactivé pour l'UID {}", uid);
        } catch (FirebaseAuthException e) {
            log.error("Erreur lors de la désactivation du compte Firebase UID {} : {}", uid, e.getMessage());
            throw new FirebaseAuthenticationException("Impossible de désactiver le compte dans Firebase : " + e.getMessage(), e);
        }
    }

    @Override
    public void enableUser(String uid) {
        ensureFirebaseAuthAvailable();
        try {
            UserRecord.UpdateRequest request = new UserRecord.UpdateRequest(uid)
                    .setDisabled(false);
            firebaseAuth.updateUser(request);
            log.info("Compte Firebase réactivé pour l'UID {}", uid);
        } catch (FirebaseAuthException e) {
            log.error("Erreur lors de la réactivation du compte Firebase UID {} : {}", uid, e.getMessage());
            throw new FirebaseAuthenticationException("Impossible de réactiver le compte dans Firebase : " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteUser(String uid) {
        ensureFirebaseAuthAvailable();
        try {
            firebaseAuth.deleteUser(uid);
            log.info("Compte Firebase supprimé pour l'UID {}", uid);
        } catch (FirebaseAuthException e) {
            log.error("Erreur lors de la suppression du compte Firebase UID {} : {}", uid, e.getMessage());
            throw new FirebaseAuthenticationException("Impossible de supprimer le compte dans Firebase : " + e.getMessage(), e);
        }
    }

    private void ensureFirebaseAuthAvailable() {
        if (firebaseAuth == null) {
            throw new IllegalStateException("Firebase Admin SDK n'est pas initialisé. " +
                    "Assurez-vous d'avoir placé le fichier 'firebase-service-account.json' dans 'src/main/resources/' " +
                    "ou configuré la variable 'FIREBASE_CONFIG_PATH'.");
        }
    }
}
