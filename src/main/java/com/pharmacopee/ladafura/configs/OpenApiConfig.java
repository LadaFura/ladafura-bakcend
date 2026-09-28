package com.pharmacopee.ladafura.configs;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "BearerAuth";

    @Bean
    public OpenAPI ladafuraOpenAPI() {
        return new OpenAPI()
                // Métadonnées générales de l'API
                .info(new Info()
                        .title("LADAFURA API - Pharmacopée Nationale Malienne")
                        .description("Plateforme numérique nationale de documentation, valorisation et distribution de la pharmacopée traditionnelle malienne. " +
                                "Cette API REST dessert les applications Flutter (mobile) et Angular (web).")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Support Technique LADAFURA")
                                .email("contact@ladafura.ml")
                                .url("https://ladafura.ml"))
                        .license(new License()
                                .name("Usage National Réglementé - INRMPT Mali")
                                .url("https://inrmpt.sante.gov.ml")))

                // Serveurs cibles
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Environnement de Développement Local")
                ))

                // Déclaration du schéma d'authentification Bearer JWT (Firebase)
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME, new SecurityScheme()
                                .name(SECURITY_SCHEME_NAME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Collez votre Firebase ID Token obtenu depuis l'application Flutter ou Angular (sans le préfixe 'Bearer ').")
                        )
                )

                // Applique l'authentification Bearer par défaut sur les endpoints sécurisés
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME));
    }
}
