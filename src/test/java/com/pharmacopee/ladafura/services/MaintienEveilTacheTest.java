package com.pharmacopee.ladafura.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.pharmacopee.ladafura.services.impl.MaintienEveilTache;
import com.sun.net.httpserver.HttpServer;

/** Anti-veille Render : la tâche appelle la sonde publique et ne propage jamais d'erreur. */
class MaintienEveilTacheTest {

    private HttpServer serveur;
    private final List<String> appelsRecus = new CopyOnWriteArrayList<>();
    private int statutReponse = 200;

    @BeforeEach
    void demarrerServeur() throws IOException {
        serveur = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
        serveur.createContext("/", echange -> {
            appelsRecus.add(echange.getRequestMethod() + " " + echange.getRequestURI().getPath());
            echange.sendResponseHeaders(statutReponse, -1);
            echange.close();
        });
        serveur.start();
    }

    @AfterEach
    void arreterServeur() {
        serveur.stop(0);
    }

    private String urlServeur() {
        return "http://127.0.0.1:" + serveur.getAddress().getPort();
    }

    @Test
    @DisplayName("Anti-veille - appelle GET /actuator/health sur l'URL publique")
    void appelleLaSondeDeSante() {
        new MaintienEveilTache(urlServeur()).maintenirEveil();

        assertThat(appelsRecus).containsExactly("GET /actuator/health");
    }

    @Test
    @DisplayName("Anti-veille - tolère une barre oblique finale dans l'URL publique")
    void normaliseLaBarreObliqueFinale() {
        new MaintienEveilTache(urlServeur() + "/").maintenirEveil();

        assertThat(appelsRecus).containsExactly("GET /actuator/health");
    }

    @Test
    @DisplayName("Anti-veille - sans URL publique, aucun appel et aucune erreur")
    void sansUrlNeFaitRien() {
        assertThatCode(() -> new MaintienEveilTache("  ").maintenirEveil()).doesNotThrowAnyException();

        assertThat(appelsRecus).isEmpty();
    }

    @Test
    @DisplayName("Anti-veille - une réponse 503 de la sonde n'interrompt pas la planification")
    void reponseEnErreurNePropageRien() {
        statutReponse = 503;

        assertThatCode(() -> new MaintienEveilTache(urlServeur()).maintenirEveil()).doesNotThrowAnyException();

        assertThat(appelsRecus).hasSize(1);
    }

    @Test
    @DisplayName("Anti-veille - un service injoignable n'interrompt pas la planification")
    void serviceInjoignableNePropageRien() {
        String urlFermee = urlServeur();
        serveur.stop(0);

        assertThatCode(() -> new MaintienEveilTache(urlFermee).maintenirEveil()).doesNotThrowAnyException();
    }
}
