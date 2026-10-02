package com.pharmacopee.ladafura.controllers.admin;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pharmacopee.ladafura.dto.admin.maladie.AdminMaladieRequest;
import com.pharmacopee.ladafura.dto.admin.maladie.AdminMaladieResponse;
import com.pharmacopee.ladafura.services.interfaces.IAdminMaladieService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/admin/maladies")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMINISTRATEUR')")
@Tag(name = "Admin - Gestion des Maladies & Pathologies", description = "Endpoints de gestion des fiches diagnostiques et pathologies ciblées")
public class AdminMaladieController {

    private final IAdminMaladieService maladieService;

    @GetMapping
    @Operation(summary = "Lister les pathologies avec pagination", description = "Recherche textuelle et pagination des maladies répertoriées.")
    @ApiResponse(responseCode = "200", description = "Liste paginée des maladies")
    public ResponseEntity<Page<AdminMaladieResponse>> getAllMaladies(
            @RequestParam(required = false) String search,
            @ParameterObject Pageable pageable) {
        return ResponseEntity.ok(maladieService.getAllMaladies(search, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtenir les détails d'une pathologie", description = "Retourne la fiche complète de la maladie avec ses plantes médicinales associées.")
    @ApiResponse(responseCode = "200", description = "Détails de la maladie")
    @ApiResponse(responseCode = "404", description = "Maladie introuvable")
    public ResponseEntity<AdminMaladieResponse> getMaladieById(@PathVariable Long id) {
        return ResponseEntity.ok(maladieService.getMaladieById(id));
    }

    @PostMapping
    @Operation(summary = "Créer une nouvelle pathologie", description = "Enregistre une nouvelle maladie dans le référentiel médical.")
    @ApiResponse(responseCode = "201", description = "Maladie créée avec succès")
    @ApiResponse(responseCode = "409", description = "Une maladie portant ce nom existe déjà")
    public ResponseEntity<AdminMaladieResponse> createMaladie(@Valid @RequestBody AdminMaladieRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(maladieService.createMaladie(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Mettre à jour une pathologie", description = "Modifie les informations d'une maladie existante.")
    @ApiResponse(responseCode = "200", description = "Maladie mise à jour")
    @ApiResponse(responseCode = "404", description = "Maladie introuvable")
    public ResponseEntity<AdminMaladieResponse> updateMaladie(
            @PathVariable Long id,
            @Valid @RequestBody AdminMaladieRequest request) {
        return ResponseEntity.ok(maladieService.updateMaladie(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer une pathologie", description = "Supprime la maladie du référentiel médical.")
    @ApiResponse(responseCode = "204", description = "Maladie supprimée")
    @ApiResponse(responseCode = "404", description = "Maladie introuvable")
    public ResponseEntity<Void> deleteMaladie(@PathVariable Long id) {
        maladieService.deleteMaladie(id);
        return ResponseEntity.noContent().build();
    }
}
