package com.pharmacopee.ladafura.controllers.population;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pharmacopee.ladafura.dto.population.auth.PopulationAuthResponse;
import com.pharmacopee.ladafura.dto.population.auth.PopulationRegisterRequest;
import com.pharmacopee.ladafura.dto.population.auth.PopulationSyncRequest;
import com.pharmacopee.ladafura.services.interfaces.IPopulationAuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/population/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Population - Authentification & Accès", description = "Endpoints d'inscription autonome, de synchronisation Firebase et de contrôle de session pour les citoyens (Population)")
public class PopulationAuthController {

    private final IPopulationAuthService populationAuthService;

    @PostMapping("/register")
    @Operation(summary = "Inscription autonome d'un citoyen",
               description = "Permet à un nouvel utilisateur de la Population de créer son compte. Provisionne simultanément le compte dans Firebase Authentication et dans la base de données avec le rôle POPULATION.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Utilisateur Population inscrit avec succès"),
        @ApiResponse(responseCode = "400", description = "Données d'inscription invalides ou mot de passe trop court"),
        @ApiResponse(responseCode = "409", description = "Un compte existe déjà avec cette adresse email")
    })
    public ResponseEntity<PopulationAuthResponse> register(@Valid @RequestBody PopulationRegisterRequest request) {
        log.info("Requête POST /api/v1/population/auth/register reçue pour email: {}", request.getEmail());
        PopulationAuthResponse response = populationAuthService.register(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/sync")
    @Operation(summary = "Synchroniser un compte créé via Firebase Auth direct",
               description = "Permet d'initialiser ou de synchroniser le profil en base de données pour un utilisateur authentifié directement via le SDK Firebase (ex: Google Sign-In ou Flutter Firebase Auth).")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Profil synchronisé avec succès"),
        @ApiResponse(responseCode = "401", description = "Token JWT Firebase absent ou invalide")
    })
    public ResponseEntity<PopulationAuthResponse> syncFirebaseUser(@RequestBody(required = false) PopulationSyncRequest request) {
        log.info("Requête POST /api/v1/population/auth/sync reçue");
        return ResponseEntity.ok(populationAuthService.syncFirebaseUser(request));
    }

    @GetMapping("/me")
    @PreAuthorize("hasAuthority('ROLE_POPULATION')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Consulter son identité et sa session Population",
               description = "Renvoie les informations de l'utilisateur de la Population actuellement connecté via son Firebase ID Token.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Session active et identité récupérée avec succès"),
        @ApiResponse(responseCode = "401", description = "Token JWT Firebase absent, invalide ou expiré"),
        @ApiResponse(responseCode = "403", description = "Accès refusé : compte non actif ou rôle non POPULATION")
    })
    public ResponseEntity<PopulationAuthResponse> getMe() {
        log.info("Requête GET /api/v1/population/auth/me reçue");
        return ResponseEntity.ok(populationAuthService.getMe());
    }
}
