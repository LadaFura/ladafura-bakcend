package com.pharmacopee.ladafura.controllers.agent;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pharmacopee.ladafura.dto.agent.dashboard.AgentDashboardResponse;
import com.pharmacopee.ladafura.services.interfaces.IAgentDashboardService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/agent/dashboard")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasAuthority('ROLE_AGENT_COLLECTE')")
@Tag(name = "Agent - Tableau de Bord", description = "Endpoint de synthèse générale, métriques opérationnelles et alertes consolidées en temps réel pour l'Agent de Collecte")
@SecurityRequirement(name = "bearerAuth")
public class AgentDashboardController {

    private final IAgentDashboardService dashboardService;

    @Operation(summary = "Consulter le tableau de bord consolidé de l'agent",
               description = "Fournit en un seul appel l'ensemble des indicateurs réels de l'agent connecté : profil synthétique, statistiques des collectes (total, brouillons, en attente, validées, rejetées, taux de validation), volet des notifications et les 5 dernières fiches de collectes.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tableau de bord consolidé généré avec succès"),
            @ApiResponse(responseCode = "401", description = "Non authentifié"),
            @ApiResponse(responseCode = "403", description = "Accès refusé - Rôle AGENT_COLLECTE requis")
    })
    @GetMapping
    public ResponseEntity<AgentDashboardResponse> getDashboard() {
        log.info("Requête GET /api/v1/agent/dashboard reçue");
        return ResponseEntity.ok(dashboardService.getDashboard());
    }
}
