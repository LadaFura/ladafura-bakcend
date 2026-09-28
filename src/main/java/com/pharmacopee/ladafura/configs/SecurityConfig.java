package com.pharmacopee.ladafura.configs;

import java.util.Arrays;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.pharmacopee.ladafura.configs.security.CustomAccessDeniedHandler;
import com.pharmacopee.ladafura.configs.security.CustomAuthenticationEntryPoint;
import com.pharmacopee.ladafura.configs.security.FirebaseAuthenticationFilter;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final FirebaseAuthenticationFilter firebaseAuthenticationFilter;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Désactivation du CSRF (API REST stateless avec jetons)
                .csrf(AbstractHttpConfigurer::disable)

                // Configuration CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // Gestion des sessions sans état (Stateless)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Gestion personnalisée des erreurs 401 et 403
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(customAuthenticationEntryPoint)
                        .accessDeniedHandler(customAccessDeniedHandler)
                )

                // Règles d'autorisation des requêtes HTTP
                .authorizeHttpRequests(auth -> auth
                        // 1. Documentation Swagger / OpenAPI (Accès public)
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()

                        // 2. Endpoints publics généraux et consultation
                        .requestMatchers(
                                "/api/v1/public/**",
                                "/api/v1/auth/**"
                        ).permitAll()

                        // 3. Autoriser les requêtes préliminaires CORS OPTIONS
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // 4. Endpoints d'administration (Rôle ADMINISTRATEUR requis)
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMINISTRATEUR")

                        // 5. Endpoints des agents de collecte
                        .requestMatchers("/api/v1/agent/**").hasRole("AGENT_COLLECTE")

                        // 6. Endpoints des pharmacopées
                        .requestMatchers("/api/v1/pharmacopee/**").hasRole("PHARMACOPEE")

                        // 7. Endpoints de la population
                        .requestMatchers(
                                "/api/v1/population/auth/register",
                                "/api/v1/population/auth/sync",
                                "/api/v1/population/recherche/**",
                                "/api/v1/population/plantes/**",
                                "/api/v1/population/produits/**",
                                "/api/v1/population/pharmacopees/**"
                        ).permitAll()
                        .requestMatchers("/api/v1/population/**").hasRole("POPULATION")

                        // 8. Tout autre endpoint nécessite d'être authentifié
                        .anyRequest().authenticated()
                )

                // Ajout du filtre Firebase avant le filtre standard d'authentification Spring
                .addFilterBefore(firebaseAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Origines autorisées (Flutter Web, Angular en dev, production)
        configuration.setAllowedOriginPatterns(List.of("*"));

        // Méthodes HTTP autorisées
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

        // En-têtes autorisés (notamment Authorization)
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With", "Accept", "Origin"));

        // Autoriser l'envoi d'identifiants
        configuration.setAllowCredentials(true);

        // Durée de mise en cache du pre-flight CORS (1 heure)
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
