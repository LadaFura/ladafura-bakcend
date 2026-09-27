package com.pharmacopee.ladafura.controllers.pharmacopee;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pharmacopee.ladafura.dto.pharmacopee.dashboard.PharmacopeeDashboardResponse;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeDashboardService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/pharmacopee/dashboard")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Pharmacopée - Tableau de Bord", description = "Endpoint de synthèse générale et indicateurs consolidés en temps réel pour l'officine de pharmacopée")
@SecurityRequirement(name = "bearerAuth")
public class PharmacopeeDashboardController {

    private final IPharmacopeeDashboardService dashboardService;

    @Operation(summary = "Consulter le tableau de bord consolidé de la pharmacopée",
               description = "Renvoie l'ensemble des indicateurs réels calculés en temps réel : statut de référencement, inventaire & valorisation des stocks, suivi des commandes, chiffre d'affaires, réputation/avis clients et alertes.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tableau de bord généré avec succès"),
            @ApiResponse(responseCode = "401", description = "Non authentifié"),
            @ApiResponse(responseCode = "403", description = "Accès refusé ou établissement non associé")
    })
    @GetMapping
    public ResponseEntity<PharmacopeeDashboardResponse> getDashboard() {
        log.info("Requête GET /api/v1/pharmacopee/dashboard reçue");
        return ResponseEntity.ok(dashboardService.getDashboard());
    }
}
