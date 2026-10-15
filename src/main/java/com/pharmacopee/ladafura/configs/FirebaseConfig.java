package com.pharmacopee.ladafura.configs;

import java.io.IOException;
import java.io.InputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class FirebaseConfig {

    private final ResourceLoader resourceLoader;

    @Value("${firebase.config.path:classpath:firebase-service-account.json}")
    private String firebaseConfigPath;

    @Bean
    public FirebaseApp firebaseApp() {
        if (!FirebaseApp.getApps().isEmpty()) {
            return FirebaseApp.getInstance();
        }

        try {
            InputStream serviceAccountStream = null;
            String envJson = System.getenv("FIREBASE_SERVICE_ACCOUNT_JSON");
            if (envJson == null || envJson.isBlank()) {
                envJson = System.getenv("FIREBASE_CREDENTIALS_JSON");
            }

            if (envJson != null && !envJson.isBlank()) {
                log.info("Chargement des identifiants Firebase depuis la variable d'environnement (FIREBASE_SERVICE_ACCOUNT_JSON)");
                serviceAccountStream = new java.io.ByteArrayInputStream(envJson.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            } else {
                Resource resource = resourceLoader.getResource(firebaseConfigPath);
                if (resource.exists()) {
                    serviceAccountStream = resource.getInputStream();
                } else {
                    log.warn("ATTENTION : Le fichier de configuration Firebase [{}] n'existe pas. " +
                            "Définissez FIREBASE_SERVICE_ACCOUNT_JSON ou fournissez le fichier service-account.json.", firebaseConfigPath);
                    return null;
                }
            }

            try (InputStream in = serviceAccountStream) {
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(in))
                        .setHttpTransport(new NetHttpTransport())
                        .build();

                FirebaseApp app = FirebaseApp.initializeApp(options);
                log.info("Firebase Admin SDK initialisé avec succès !");
                return app;
            }
        } catch (IOException e) {
            log.error("Erreur lors de l'initialisation de Firebase Admin SDK : {}", e.getMessage(), e);
            return null;
        }
    }

    @Bean
    public FirebaseAuth firebaseAuth(@Autowired(required = false) FirebaseApp firebaseApp) {
        if (firebaseApp == null) {
            log.warn("FirebaseAuth Bean indisponible car FirebaseApp n'a pas pu être initialisé.");
            return null;
        }
        return FirebaseAuth.getInstance(firebaseApp);
    }
}
