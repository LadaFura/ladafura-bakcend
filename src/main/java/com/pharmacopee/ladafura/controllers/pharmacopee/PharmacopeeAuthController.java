package com.pharmacopee.ladafura.controllers.pharmacopee;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pharmacopee.ladafura.dto.pharmacopee.auth.PharmacopeeAuthResponse;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeAuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/pharmacopee/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Pharmacopée - Authentification & Accès", description = "Endpoints d'identification, de contrôle de session et de vérification des droits pour l'acteur Pharmacopée")
@SecurityRequirement(name = "bearerAuth")
public class PharmacopeeAuthController {

    private final IPharmacopeeAuthService pharmacopeeAuthService;

    @GetMapping("/me")
    @Operation(summary = "Consulter son identité et le statut de sa pharmacopée",
               description = "Permet à l'utilisateur connecté avec le rôle PHARMACOPEE de consulter son profil utilisateur, son UID Firebase et l'état de référencement de son établissement.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Identité récupérée avec succès"),
        @ApiResponse(responseCode = "401", description = "Token JWT Firebase absent, invalide ou expiré"),
        @ApiResponse(responseCode = "403", description = "Accès refusé : compte non actif ou rôle non PHARMACOPEE")
    })
    public ResponseEntity<PharmacopeeAuthResponse> getMe() {
        log.info("Requête de consultation d'identité reçue pour le compte pharmacopée connecté");
        return ResponseEntity.ok(pharmacopeeAuthService.getMe());
    }
}
