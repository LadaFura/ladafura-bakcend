package com.pharmacopee.ladafura.controllers.admin;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pharmacopee.ladafura.dto.admin.plante.AdminModerateVertuRequest;
import com.pharmacopee.ladafura.dto.admin.plante.AdminPlanteRequest;
import com.pharmacopee.ladafura.dto.admin.plante.AdminPlanteResponse;
import com.pharmacopee.ladafura.dto.admin.plante.AdminVertuResponse;
import com.pharmacopee.ladafura.enums.StatutPlante;
import com.pharmacopee.ladafura.services.interfaces.IAdminPlanteService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/admin/plantes")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMINISTRATEUR')")
@Tag(name = "Admin - Modération des Plantes & Vertus", description = "Endpoints de gestion et modération du patrimoine botanique et thérapeutique (US-28)")
public class AdminPlanteController {

    private final IAdminPlanteService planteService;

    @GetMapping
    @Operation(summary = "Lister les plantes médicinales avec pagination", description = "Filtrage optionnel par statut (BROUILLON, EN_ATTENTE, VALIDE, REJETE, ARCHIVE).")
    @ApiResponse(responseCode = "200", description = "Liste paginée des plantes médicinales")
    public ResponseEntity<Page<AdminPlanteResponse>> getAllPlantes(
            @RequestParam(required = false) StatutPlante statut,
            @ParameterObject Pageable pageable) {
        return ResponseEntity.ok(planteService.getAllPlantes(statut, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtenir le détail d'une plante", description = "Retourne la fiche complète de la plante avec ses noms vernaculaires, maladies traitées et compteurs.")
    @ApiResponse(responseCode = "200", description = "Détails de la plante")
    @ApiResponse(responseCode = "404", description = "Plante introuvable")
    public ResponseEntity<AdminPlanteResponse> getPlanteById(@PathVariable Long id) {
        return ResponseEntity.ok(planteService.getPlanteById(id));
    }

    @PostMapping
    @Operation(summary = "Créer une nouvelle fiche de plante", description = "Enregistre une nouvelle plante médicinale dans l'herbier national.")
    @ApiResponse(responseCode = "201", description = "Plante créée avec succès")
    @ApiResponse(responseCode = "400", description = "Données invalides")
    @ApiResponse(responseCode = "409", description = "Conflit - Le nom scientifique existe déjà")
    public ResponseEntity<AdminPlanteResponse> createPlante(@Valid @RequestBody AdminPlanteRequest request) {
        AdminPlanteResponse created = planteService.createPlante(request);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Mettre à jour une fiche de plante", description = "Modifie la description, les photos, noms vernaculaires et pathologies associées.")
    @ApiResponse(responseCode = "200", description = "Plante mise à jour avec succès")
    @ApiResponse(responseCode = "404", description = "Plante introuvable")
    public ResponseEntity<AdminPlanteResponse> updatePlante(
            @PathVariable Long id,
            @Valid @RequestBody AdminPlanteRequest request) {
        return ResponseEntity.ok(planteService.updatePlante(id, request));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Valider ou modérer le statut d'une fiche plante", description = "Permet de passer une fiche en VALIDE, REJETE, ARCHIVE.")
    @ApiResponse(responseCode = "200", description = "Statut de la plante mis à jour avec succès")
    @ApiResponse(responseCode = "404", description = "Plante introuvable")
    public ResponseEntity<AdminPlanteResponse> moderatePlante(
            @PathVariable Long id,
            @RequestParam StatutPlante statut) {
        return ResponseEntity.ok(planteService.moderatePlante(id, statut));
    }

    @PatchMapping("/vertus/{vertuId}/moderate")
    @Operation(summary = "Modérer une vertu thérapeutique", description = "Permet d'approuver ou de rejeter une vertu spécifique rattachée à une plante.")
    @ApiResponse(responseCode = "200", description = "Vertu mise à jour avec succès")
    @ApiResponse(responseCode = "404", description = "Vertu introuvable")
    public ResponseEntity<AdminVertuResponse> moderateVertu(
            @PathVariable Long vertuId,
            @Valid @RequestBody AdminModerateVertuRequest request) {
        return ResponseEntity.ok(planteService.moderateVertu(vertuId, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer une plante médicinale", description = "Supprime la plante et ses déclinaisons vernaculaires associées.")
    @ApiResponse(responseCode = "204", description = "Plante supprimée avec succès")
    @ApiResponse(responseCode = "404", description = "Plante introuvable")
    public ResponseEntity<Void> deletePlante(@PathVariable Long id) {
        planteService.deletePlante(id);
        return ResponseEntity.noContent().build();
    }
}
