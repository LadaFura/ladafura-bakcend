package com.pharmacopee.ladafura.services.impl;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.auth.UserRecord;
import com.pharmacopee.ladafura.services.interfaces.IFirebaseAuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    private void ensureFirebaseAuthAvailable() {
        if (firebaseAuth == null) {
            throw new IllegalStateException("Firebase Admin SDK n'est pas initialisé. " +
                    "Assurez-vous d'avoir placé le fichier 'firebase-service-account.json' dans 'src/main/resources/' " +
                    "ou configuré la variable 'FIREBASE_CONFIG_PATH'.");
        }
    }
}
