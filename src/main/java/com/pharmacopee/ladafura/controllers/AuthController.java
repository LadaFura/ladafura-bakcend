package com.pharmacopee.ladafura.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pharmacopee.ladafura.dto.auth.AuthMeResponse;
import com.pharmacopee.ladafura.services.interfaces.IAuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentification Générale", description = "Endpoints neutres d'authentification et de résolution de session pour l'application mobile")
public class AuthController {

    private final IAuthService authService;

    @GetMapping("/me")
    @Operation(summary = "Résolution neutre du profil et rôle utilisateur connecté",
               description = "Permet à l'application mobile Flutter de récupérer les informations de l'utilisateur authentifié (nom, prénom, email, rôle POPULATION ou AGENT_COLLECTE) via le jeton Firebase Bearer, sans supposer le rôle à l'avance.",
               security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Profil et rôle récupérés avec succès"),
        @ApiResponse(responseCode = "401", description = "Jeton d'authentification absent, expiré ou invalide"),
        @ApiResponse(responseCode = "403", description = "Compte inactif ou suspendu"),
        @ApiResponse(responseCode = "404", description = "Utilisateur non trouvé en base de données")
    })
    public ResponseEntity<AuthMeResponse> getMe() {
        log.info("Requête GET /api/v1/auth/me reçue");
        AuthMeResponse response = authService.getCurrentUser();
        return ResponseEntity.ok(response);
    }
}
