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

import com.pharmacopee.ladafura.dto.admin.pharmacopee.AdminCreatePharmacopeeRequest;
import com.pharmacopee.ladafura.dto.admin.pharmacopee.AdminModeratePharmacopeeRequest;
import com.pharmacopee.ladafura.dto.admin.pharmacopee.AdminPharmacopeeDetailResponse;
import com.pharmacopee.ladafura.dto.admin.pharmacopee.AdminPharmacopeeSummaryResponse;
import com.pharmacopee.ladafura.dto.admin.pharmacopee.AdminUpdatePharmacopeeRequest;
import com.pharmacopee.ladafura.enums.StatutPharmacopee;
import com.pharmacopee.ladafura.services.interfaces.IAdminPharmacopeeService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/admin/pharmacopees")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMINISTRATEUR')")
@Tag(name = "Admin - Modération des Pharmacopées", description = "Endpoints de gestion, création et agrément des structures de pharmacopée (US-27)")
public class AdminPharmacopeeController {

    private final IAdminPharmacopeeService pharmacopeeService;

    @GetMapping
    @Operation(summary = "Lister les pharmacopées avec pagination et filtre par statut", description = "Permet de lister les structures selon leur état (EN_ATTENTE, VALIDEE, SUSPENDUE, REJETEE).")
    @ApiResponse(responseCode = "200", description = "Liste paginée des pharmacopées")
    public ResponseEntity<Page<AdminPharmacopeeSummaryResponse>> getAllPharmacopees(
            @RequestParam(required = false) StatutPharmacopee statut,
            @ParameterObject Pageable pageable) {
        return ResponseEntity.ok(pharmacopeeService.getAllPharmacopees(statut, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtenir le détail d'une pharmacopée", description = "Fournit les informations complètes d'une structure (localisation, propriétaire, modes de retrait, volumes).")
    @ApiResponse(responseCode = "200", description = "Détails de la pharmacopée")
    @ApiResponse(responseCode = "404", description = "Pharmacopée introuvable")
    public ResponseEntity<AdminPharmacopeeDetailResponse> getPharmacopeeById(@PathVariable Long id) {
        return ResponseEntity.ok(pharmacopeeService.getPharmacopeeById(id));
    }

    @PostMapping
    @Operation(summary = "Créer une nouvelle structure de pharmacopée", description = "Enregistre une nouvelle pharmacopée avec ses coordonnées géographiques et son statut initial.")
    @ApiResponse(responseCode = "201", description = "Pharmacopée créée avec succès")
    public ResponseEntity<AdminPharmacopeeDetailResponse> createPharmacopee(
            @Valid @RequestBody AdminCreatePharmacopeeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pharmacopeeService.createPharmacopee(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Mettre à jour les informations d'une pharmacopée", description = "Modifie le nom, la description, les coordonnées et la localisation d'une pharmacopée existante.")
    @ApiResponse(responseCode = "200", description = "Pharmacopée mise à jour avec succès")
    @ApiResponse(responseCode = "404", description = "Pharmacopée introuvable")
    public ResponseEntity<AdminPharmacopeeDetailResponse> updatePharmacopee(
            @PathVariable Long id,
            @Valid @RequestBody AdminUpdatePharmacopeeRequest request) {
        return ResponseEntity.ok(pharmacopeeService.updatePharmacopee(id, request));
    }

    @PatchMapping("/{id}/moderate")
    @Operation(summary = "Valider, suspendre ou rejeter une pharmacopée", description = "Met à jour le statut d'agrément de la pharmacopée avec un motif éventuel.")
    @ApiResponse(responseCode = "200", description = "Statut de la pharmacopée mis à jour avec succès")
    @ApiResponse(responseCode = "400", description = "Action de modération invalide")
    @ApiResponse(responseCode = "404", description = "Pharmacopée introuvable")
    public ResponseEntity<AdminPharmacopeeDetailResponse> moderatePharmacopee(
            @PathVariable Long id,
            @Valid @RequestBody AdminModeratePharmacopeeRequest request) {
        return ResponseEntity.ok(pharmacopeeService.moderatePharmacopee(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer une pharmacopée", description = "Supprime définitivement une structure de pharmacopée du système national.")
    @ApiResponse(responseCode = "204", description = "Pharmacopée supprimée")
    @ApiResponse(responseCode = "404", description = "Pharmacopée introuvable")
    public ResponseEntity<Void> deletePharmacopee(@PathVariable Long id) {
        pharmacopeeService.deletePharmacopee(id);
        return ResponseEntity.noContent().build();
    }
}
