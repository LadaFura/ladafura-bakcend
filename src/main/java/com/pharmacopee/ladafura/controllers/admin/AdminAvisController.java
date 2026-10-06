package com.pharmacopee.ladafura.controllers.admin;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pharmacopee.ladafura.dto.admin.avis.AdminAvisResponse;
import com.pharmacopee.ladafura.dto.admin.avis.AdminModerateAvisRequest;
import com.pharmacopee.ladafura.enums.StatutAvis;
import com.pharmacopee.ladafura.services.interfaces.IAdminAvisService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/admin/avis")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMINISTRATEUR')")
@Tag(name = "Admin - Modération des Avis", description = "Endpoints de modération des retours d'expérience et notes des consommateurs (US-31)")
public class AdminAvisController {

    private final IAdminAvisService avisService;

    @GetMapping
    @Operation(summary = "Lister les avis clients avec pagination et filtres", description = "Filtrage par statut d'avis (EN_ATTENTE, PUBLIE, REJETE, MASQUE) et par pharmacopée.")
    @ApiResponse(responseCode = "200", description = "Liste paginée des avis")
    public ResponseEntity<Page<AdminAvisResponse>> getAllAvis(
            @RequestParam(required = false) StatutAvis statut,
            @RequestParam(required = false) Long pharmacopeeId,
            @ParameterObject Pageable pageable) {
        return ResponseEntity.ok(avisService.getAllAvis(statut, pharmacopeeId, pageable));
    }

    @PatchMapping("/{id}/moderate")
    @Operation(summary = "Modérer un avis client", description = "Permet de publier, masquer ou rejeter un avis client.")
    @ApiResponse(responseCode = "200", description = "Avis modéré avec succès")
    @ApiResponse(responseCode = "400", description = "Données invalides")
    @ApiResponse(responseCode = "404", description = "Avis introuvable")
    public ResponseEntity<AdminAvisResponse> moderateAvis(
            @PathVariable Long id,
            @Valid @RequestBody AdminModerateAvisRequest request) {
        return ResponseEntity.ok(avisService.moderateAvis(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer définitivement un avis", description = "Supprime un avis inapproprié de la base de données.")
    @ApiResponse(responseCode = "204", description = "Avis supprimé avec succès")
    @ApiResponse(responseCode = "404", description = "Avis introuvable")
    public ResponseEntity<Void> deleteAvis(@PathVariable Long id) {
        avisService.deleteAvis(id);
        return ResponseEntity.noContent().build();
    }
}
