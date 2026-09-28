package com.pharmacopee.ladafura.controllers.admin;

import java.util.List;

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
import org.springframework.web.bind.annotation.RestController;

import com.pharmacopee.ladafura.dto.admin.etude.AdminEtudeRequest;
import com.pharmacopee.ladafura.dto.admin.etude.AdminEtudeResponse;
import com.pharmacopee.ladafura.services.interfaces.IAdminEtudeScientifiqueService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/admin/etudes")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMINISTRATEUR')")
@Tag(name = "Admin - Études Scientifiques", description = "Endpoints de gestion et d'indexation des publications scientifiques validant la pharmacopée (US-32)")
public class AdminEtudeScientifiqueController {

    private final IAdminEtudeScientifiqueService etudeService;

    @GetMapping
    @Operation(summary = "Lister toutes les études scientifiques avec pagination", description = "Permet de parcourir l'ensemble des études répertoriées dans la plateforme.")
    @ApiResponse(responseCode = "200", description = "Liste paginée des études")
    public ResponseEntity<Page<AdminEtudeResponse>> getAllEtudes(@ParameterObject Pageable pageable) {
        return ResponseEntity.ok(etudeService.getAllEtudes(pageable));
    }

    @GetMapping("/plante/{planteId}")
    @Operation(summary = "Lister les études d'une plante spécifique", description = "Retourne l'ensemble des études scientifiques associées à une plante médicinale.")
    @ApiResponse(responseCode = "200", description = "Liste des études de la plante")
    @ApiResponse(responseCode = "404", description = "Plante introuvable")
    public ResponseEntity<List<AdminEtudeResponse>> getEtudesByPlanteId(@PathVariable Long planteId) {
        return ResponseEntity.ok(etudeService.getEtudesByPlanteId(planteId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtenir les détails d'une étude scientifique", description = "Fournit le titre, auteurs, référence bibliographique, résumé et lien du document.")
    @ApiResponse(responseCode = "200", description = "Détails de l'étude")
    @ApiResponse(responseCode = "404", description = "Étude introuvable")
    public ResponseEntity<AdminEtudeResponse> getEtudeById(@PathVariable Long id) {
        return ResponseEntity.ok(etudeService.getEtudeById(id));
    }

    @PostMapping
    @Operation(summary = "Créer et associer une nouvelle étude scientifique", description = "Enregistre une publication scientifique rattachée à une plante médicinale.")
    @ApiResponse(responseCode = "201", description = "Étude scientifique créée avec succès")
    @ApiResponse(responseCode = "400", description = "Données invalides")
    @ApiResponse(responseCode = "404", description = "Plante introuvable")
    public ResponseEntity<AdminEtudeResponse> createEtude(@Valid @RequestBody AdminEtudeRequest request) {
        AdminEtudeResponse created = etudeService.createEtude(request);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Mettre à jour une étude scientifique", description = "Modifie les références, le résumé ou réassigne la publication à une autre plante.")
    @ApiResponse(responseCode = "200", description = "Étude scientifique mise à jour avec succès")
    @ApiResponse(responseCode = "404", description = "Étude introuvable")
    public ResponseEntity<AdminEtudeResponse> updateEtude(
            @PathVariable Long id,
            @Valid @RequestBody AdminEtudeRequest request) {
        return ResponseEntity.ok(etudeService.updateEtude(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer une étude scientifique", description = "Supprime la référence de l'étude scientifique du système.")
    @ApiResponse(responseCode = "204", description = "Étude supprimée avec succès")
    @ApiResponse(responseCode = "404", description = "Étude introuvable")
    public ResponseEntity<Void> deleteEtude(@PathVariable Long id) {
        etudeService.deleteEtude(id);
        return ResponseEntity.noContent().build();
    }
}
