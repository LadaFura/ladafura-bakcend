package com.pharmacopee.ladafura.configs.security;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.pharmacopee.ladafura.repository.UtilisateurRepository;
import com.pharmacopee.ladafura.services.interfaces.IFirebaseAuthService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class FirebaseAuthenticationFilter extends OncePerRequestFilter {

    private final IFirebaseAuthService firebaseAuthService;
    private final ObjectProvider<UtilisateurRepository> utilisateurRepositoryProvider;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        // Si aucun jeton Bearer n'est transmis, continuer la chaîne (les routes publiques passeront, les privées seront bloquées)
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String idToken = authHeader.substring(7).trim();

        // Extraction automatique au cas où l'utilisateur a collé la réponse JSON entière de Firebase dans Postman ou Swagger
        if (idToken.startsWith("{")) {
            try {
                var jsonNode = objectMapper.readTree(idToken);
                if (jsonNode.has("idToken")) {
                    idToken = jsonNode.get("idToken").asText().trim();
                    log.info("Token JWT extrait automatiquement depuis l'objet JSON fourni dans Authorization");
                }
            } catch (Exception e) {
                log.debug("Impossible de parser le header Authorization comme JSON : {}", e.getMessage());
            }
        }
        if (idToken.startsWith("\"") && idToken.endsWith("\"") && idToken.length() > 1) {
            idToken = idToken.substring(1, idToken.length() - 1).trim();
        }

        // Pré-validation : un JWT valide doit comporter 3 parties séparées par un point (header.payload.signature)
        String[] jwtParts = idToken.split("\\.");
        if (jwtParts.length != 3) {
            log.warn("Jeton Bearer fourni invalide : format JWT attendu (3 parties) mais reçu : '{}'",
                    idToken.length() > 30 ? idToken.substring(0, 30) + "..." : idToken);
            SecurityContextHolder.clearContext();

            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

            Map<String, Object> errorDetails = new HashMap<>();
            errorDetails.put("timestamp", LocalDateTime.now().toString());
            errorDetails.put("status", HttpServletResponse.SC_UNAUTHORIZED);
            errorDetails.put("error", "Unauthorized");
            errorDetails.put("message", "Le jeton d'authentification fourni n'est pas un JWT Firebase valide.");
            errorDetails.put("path", request.getRequestURI());

            objectMapper.writeValue(response.getOutputStream(), errorDetails);
            return;
        }

        try {
            // Vérification de la signature cryptographique du jeton auprès de Firebase
            FirebaseToken decodedToken = firebaseAuthService.verifyIdToken(idToken);

            String uid = decodedToken.getUid();
            String email = decodedToken.getEmail();

            // Extraction des rôles depuis les Custom Claims de Firebase ou la base MySQL
            List<GrantedAuthority> authorities = extractAuthorities(decodedToken);

            // Création de l'objet d'authentification pour le contexte Spring Security
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(email != null ? email : uid, idToken, authorities);

            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            // Enregistrement dans le SecurityContext
            SecurityContextHolder.getContext().setAuthentication(authentication);

            log.debug("Utilisateur authentifié via Firebase : UID={}, Email={}, Authorities={}", uid, email, authorities);

            filterChain.doFilter(request, response);

        } catch (FirebaseAuthException e) {
            log.warn("Échec de validation du token Firebase : {}", e.getMessage());
            SecurityContextHolder.clearContext();

            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

            Map<String, Object> errorDetails = new HashMap<>();
            errorDetails.put("timestamp", LocalDateTime.now().toString());
            errorDetails.put("status", HttpServletResponse.SC_UNAUTHORIZED);
            errorDetails.put("error", "Unauthorized");
            errorDetails.put("message", "Le jeton d'authentification Firebase est invalide ou a expiré : " + e.getMessage());
            errorDetails.put("path", request.getRequestURI());

            objectMapper.writeValue(response.getOutputStream(), errorDetails);
        } catch (Exception e) {
            log.error("Erreur inattendue lors de la validation du token : {}", e.getMessage());
            SecurityContextHolder.clearContext();

            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

            Map<String, Object> errorDetails = new HashMap<>();
            errorDetails.put("timestamp", LocalDateTime.now().toString());
            errorDetails.put("status", HttpServletResponse.SC_UNAUTHORIZED);
            errorDetails.put("error", "Unauthorized");
            errorDetails.put("message", "Jeton d'authentification invalide.");
            errorDetails.put("path", request.getRequestURI());

            objectMapper.writeValue(response.getOutputStream(), errorDetails);
        }
    }

    private List<GrantedAuthority> extractAuthorities(FirebaseToken decodedToken) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        Map<String, Object> claims = decodedToken.getClaims();

        // 1. Vérification d'un claim spécifique "role"
        Object roleClaim = claims.get("role");
        if (roleClaim != null) {
            String roleName = roleClaim.toString().toUpperCase();
            if (!roleName.startsWith("ROLE_")) {
                roleName = "ROLE_" + roleName;
            }
            authorities.add(new SimpleGrantedAuthority(roleName));
        }

        // 2. Vérification d'un flag booléen "admin"
        if (Boolean.TRUE.equals(claims.get("admin"))) {
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMINISTRATEUR"));
        }

        // 3. Fallback : Vérification dans MySQL si le custom claim n'est pas encore dans le jeton actuel
        if (authorities.isEmpty() || (authorities.size() == 1 && "ROLE_USER".equals(authorities.get(0).getAuthority()))) {
            UtilisateurRepository repo = utilisateurRepositoryProvider.getIfAvailable();
            if (repo != null) {
                try {
                    repo.findByFirebaseUid(decodedToken.getUid())
                            .or(() -> decodedToken.getEmail() != null ? repo.findByEmail(decodedToken.getEmail()) : java.util.Optional.empty())
                            .ifPresent(u -> {
                                if (u.getRole() != null) {
                                    authorities.clear();
                                    String roleName = u.getRole().name();
                                    if (!roleName.startsWith("ROLE_")) {
                                        roleName = "ROLE_" + roleName;
                                    }
                                    authorities.add(new SimpleGrantedAuthority(roleName));
                                    log.debug("Rôle chargé depuis MySQL : {}", roleName);
                                }
                            });
                } catch (Exception ex) {
                    log.warn("Impossible de récupérer le rôle depuis la base de données : {}", ex.getMessage());
                }
            }
        }

        // 4. Rôle de base accordé par défaut à tout utilisateur Firebase authentifié valide
        if (authorities.isEmpty()) {
            authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        }

        return authorities;
    }
}
