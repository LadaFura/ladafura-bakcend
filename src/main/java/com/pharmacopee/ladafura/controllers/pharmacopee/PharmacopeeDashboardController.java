package com.pharmacopee.ladafura.controllers.pharmacopee;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pharmacopee.ladafura.dto.pharmacopee.dashboard.PharmacopeeDashboardResponse;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeDashboardService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/pharmacopee/dashboard")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Pharmacopée - Tableau de bord", description = "Endpoints de consultation des statistiques et vue globale de l'officine")
@SecurityRequirement(name = "bearerAuth")
public class PharmacopeeDashboardController {

    private final IPharmacopeeDashboardService dashboardService;

    @Operation(summary = "Consulter le tableau de bord consolidé",
               description = "Renvoie les statistiques clés : commandes, stock, référencement, avis, etc.")
    @GetMapping({"", "/stats"})
    public ResponseEntity<PharmacopeeDashboardResponse> getStats() {
        log.info("Requête GET /api/v1/pharmacopee/dashboard/stats reçue");
        return ResponseEntity.ok(dashboardService.getDashboard());
    }
}
