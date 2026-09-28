package com.pharmacopee.ladafura.controllers.agent;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pharmacopee.ladafura.dto.agent.auth.AgentAuthResponse;
import com.pharmacopee.ladafura.services.interfaces.IAgentAuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/agent/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Agent de Collecte - Authentification & Accès", description = "Endpoints d'identification, de contrôle de session et de vérification des droits pour l'acteur Agent de Collecte")
@SecurityRequirement(name = "bearerAuth")
public class AgentAuthController {

    private final IAgentAuthService agentAuthService;

    @GetMapping("/me")
    @Operation(summary = "Consulter son identité et les informations de session",
               description = "Permet à l'agent de collecte connecté avec le rôle AGENT_COLLECTE de consulter ses informations de profil, son matricule, sa zone de couverture et son statut.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Identité récupérée avec succès"),
        @ApiResponse(responseCode = "401", description = "Token JWT Firebase absent, invalide ou expiré"),
        @ApiResponse(responseCode = "403", description = "Accès refusé : compte non actif ou rôle non AGENT_COLLECTE")
    })
    public ResponseEntity<AgentAuthResponse> getMe() {
        log.info("Requête de consultation d'identité reçue pour l'agent de collecte connecté");
        return ResponseEntity.ok(agentAuthService.getMe());
    }
}
