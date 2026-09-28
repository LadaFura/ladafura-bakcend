package com.pharmacopee.ladafura.controllers.admin;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pharmacopee.ladafura.dto.admin.dashboard.AdminDashboardStatsResponse;
import com.pharmacopee.ladafura.services.interfaces.IAdminDashboardService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/admin/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMINISTRATEUR')")
@Tag(name = "Admin - Dashboard & Métriques", description = "Endpoints d'analyse globale et statistiques pour le tableau de bord administrateur (US-25)")
public class AdminDashboardController {

    private final IAdminDashboardService dashboardService;

    @GetMapping("/stats")
    @Operation(summary = "Obtenir les statistiques globales", description = "Fournit les métriques consolidées : total utilisateurs, alertes de modération (collectes, officines, produits, avis) et base de connaissances.")
    @ApiResponse(responseCode = "200", description = "Statistiques consolidées calculées avec succès")
    @ApiResponse(responseCode = "403", description = "Accès refusé - Rôle ADMINISTRATEUR requis")
    public ResponseEntity<AdminDashboardStatsResponse> getStats() {
        return ResponseEntity.ok(dashboardService.getDashboardStats());
    }
}
