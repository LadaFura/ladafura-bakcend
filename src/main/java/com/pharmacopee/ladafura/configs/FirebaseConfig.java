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
            Resource resource = resourceLoader.getResource(firebaseConfigPath);
            if (!resource.exists()) {
                log.warn("ATTENTION : Le fichier de configuration Firebase [{}] n'existe pas. " +
                        "Téléchargez votre clé 'service-account.json' depuis la console Firebase pour activer la validation des tokens.", firebaseConfigPath);
                return null;
            }

            try (InputStream serviceAccountStream = resource.getInputStream()) {
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccountStream))
                        .setHttpTransport(new NetHttpTransport())
                        .build();

                FirebaseApp app = FirebaseApp.initializeApp(options);
                log.info("Firebase Admin SDK initialisé avec succès depuis [{}]", firebaseConfigPath);
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
