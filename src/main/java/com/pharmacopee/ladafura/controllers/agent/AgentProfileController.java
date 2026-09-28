package com.pharmacopee.ladafura.controllers.agent;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pharmacopee.ladafura.dto.agent.profil.AgentProfileResponse;
import com.pharmacopee.ladafura.dto.agent.profil.AgentUpdateProfileRequest;
import com.pharmacopee.ladafura.services.interfaces.IAgentProfileService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/agent/profile")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Agent de Collecte - Profil", description = "Endpoints de consultation et mise à jour du profil de l'Agent de Collecte")
@SecurityRequirement(name = "bearerAuth")
public class AgentProfileController {

    private final IAgentProfileService agentProfileService;

    @GetMapping
    @Operation(summary = "Consulter le profil et le statut de l'agent",
               description = "Permet à l'agent de collecte connecté de consulter ses données personnelles, professionnelles, son statut et son volume de collectes.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Profil de l'agent récupéré avec succès"),
        @ApiResponse(responseCode = "401", description = "Token JWT Firebase absent ou invalide"),
        @ApiResponse(responseCode = "403", description = "Accès refusé : compte inactif ou rôle non autorisé")
    })
    public ResponseEntity<AgentProfileResponse> getProfile() {
        log.info("Requête de consultation du profil de l'agent reçue");
        return ResponseEntity.ok(agentProfileService.getProfile());
    }

    @PutMapping
    @Operation(summary = "Mettre à jour ses coordonnées autorisées",
               description = "Permet à l'agent de modifier son nom, prénom, téléphone et zone de couverture sans altérer son rôle ou son statut.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Profil mis à jour avec succès"),
        @ApiResponse(responseCode = "400", description = "Données d'entrée invalides"),
        @ApiResponse(responseCode = "401", description = "Token JWT Firebase absent ou invalide"),
        @ApiResponse(responseCode = "403", description = "Accès refusé")
    })
    public ResponseEntity<AgentProfileResponse> updateProfile(@Valid @RequestBody AgentUpdateProfileRequest request) {
        log.info("Requête de mise à jour du profil reçue");
        return ResponseEntity.ok(agentProfileService.updateProfile(request));
    }
}
