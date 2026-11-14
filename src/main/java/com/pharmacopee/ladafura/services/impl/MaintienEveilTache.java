package com.pharmacopee.ladafura.services.impl;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * Anti-veille de l'hébergement gratuit Render.
 * <p>
 * Render met en veille un service gratuit après 15 minutes sans requête entrante ; le réveil
 * d'une application Spring Boot prend ensuite plusieurs minutes. Cette tâche appelle
 * périodiquement l'URL <b>publique</b> du service : la requête repasse par le routeur Render et
 * compte donc comme du trafic entrant (un appel sur {@code localhost} ne compterait pas).
 * <p>
 * Limite : la tâche ne peut pas réveiller un service déjà endormi ou arrêté. Un moniteur
 * externe (UptimeRobot, cron-job.org) sur la même URL couvre ce cas.
 */
@Component
@Slf4j
@ConditionalOnProperty(name = "ladafura.maintien-eveil.enabled", havingValue = "true")
public class MaintienEveilTache {

    static final String CHEMIN_SONDE = "/actuator/health";
    private static final Duration DELAI_MAXIMAL = Duration.ofSeconds(20);

    private final String urlSonde;
    private final HttpClient client = HttpClient.newBuilder().connectTimeout(DELAI_MAXIMAL).build();

    public MaintienEveilTache(@Value("${ladafura.maintien-eveil.url:}") String urlPublique) {
        this.urlSonde = construireUrlSonde(urlPublique);
        if (urlSonde == null) {
            log.warn("Anti-veille activé sans URL publique : définissez MAINTIEN_EVEIL_URL "
                    + "(sur Render, RENDER_EXTERNAL_URL est fournie automatiquement).");
        } else {
            log.info("Anti-veille activé : appel périodique de {}", urlSonde);
        }
    }

    /** L'intervalle par défaut (10 minutes) reste sous le seuil de mise en veille de 15 minutes. */
    @Scheduled(initialDelayString = "${ladafura.maintien-eveil.intervalle-ms:600000}",
            fixedDelayString = "${ladafura.maintien-eveil.intervalle-ms:600000}")
    public void maintenirEveil() {
        if (urlSonde == null) {
            return;
        }
        HttpRequest requete = HttpRequest.newBuilder(URI.create(urlSonde))
                .timeout(DELAI_MAXIMAL)
                .GET()
                .build();
        try {
            HttpResponse<Void> reponse = client.send(requete, HttpResponse.BodyHandlers.discarding());
            // Même une réponse en erreur compte comme trafic entrant ; on la signale seulement.
            if (reponse.statusCode() >= 400) {
                log.warn("Anti-veille : la sonde a répondu HTTP {}", reponse.statusCode());
            } else {
                log.debug("Anti-veille : sonde HTTP {}", reponse.statusCode());
            }
        } catch (IOException e) {
            log.warn("Anti-veille : sonde injoignable ({})", e.toString());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Anti-veille : appel interrompu");
        }
    }

    private static String construireUrlSonde(String urlPublique) {
        if (urlPublique == null || urlPublique.isBlank()) {
            return null;
        }
        String base = urlPublique.trim();
        while (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        return base + CHEMIN_SONDE;
    }
}
