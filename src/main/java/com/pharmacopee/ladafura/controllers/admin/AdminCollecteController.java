package com.pharmacopee.ladafura.controllers.admin;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pharmacopee.ladafura.dto.admin.collecte.AdminCollecteDetailResponse;
import com.pharmacopee.ladafura.dto.admin.collecte.AdminCollecteSummaryResponse;
import com.pharmacopee.ladafura.dto.admin.collecte.AdminModerateCollecteRequest;
import com.pharmacopee.ladafura.enums.StatutCollecte;
import com.pharmacopee.ladafura.services.interfaces.IAdminCollecteService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/admin/collectes")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMINISTRATEUR')")
@Tag(name = "Admin - Modération des Collectes", description = "Endpoints d'examen et de modération des collectes terrain remontées par les agents (US-29)")
public class AdminCollecteController {

    private final IAdminCollecteService collecteService;

    @GetMapping
    @Operation(summary = "Lister les fiches de collecte terrain", description = "Filtrage optionnel par statut (BROUILLON, SOUMISE, EN_EXAMEN, VALIDEE, REJETEE).")
    @ApiResponse(responseCode = "200", description = "Liste paginée des fiches de collecte")
    public ResponseEntity<Page<AdminCollecteSummaryResponse>> getAllCollectes(
            @RequestParam(required = false) StatutCollecte statut,
            @ParameterObject Pageable pageable) {
        return ResponseEntity.ok(collecteService.getAllCollectes(statut, pageable));
    }

    @GetMapping("/stats")
    @Operation(summary = "Statistiques des collectes", description = "Retourne le nombre total de collectes réparties par statut d'examen.")
    @ApiResponse(responseCode = "200", description = "Statistiques calculées avec succès")
    public ResponseEntity<com.pharmacopee.ladafura.dto.admin.collecte.AdminCollecteStatsResponse> getStats() {
        return ResponseEntity.ok(collecteService.getStats());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtenir le détail d'une collecte", description = "Fournit l'ensemble des données géographiques, les audios, photos, l'agent et la source interrogée.")
    @ApiResponse(responseCode = "200", description = "Détails de la collecte")
    @ApiResponse(responseCode = "404", description = "Collecte introuvable")
    public ResponseEntity<AdminCollecteDetailResponse> getCollecteById(@PathVariable Long id) {
        return ResponseEntity.ok(collecteService.getCollecteById(id));
    }

    @PatchMapping("/{id}/moderate")
    @Operation(summary = "Valider ou rejeter une collecte", description = "Permet d'approuver ou de refuser une collecte terrain en précisant un motif en cas de rejet.")
    @ApiResponse(responseCode = "200", description = "Modération enregistrée avec succès")
    @ApiResponse(responseCode = "400", description = "Action de modération invalide")
    @ApiResponse(responseCode = "404", description = "Collecte introuvable")
    public ResponseEntity<AdminCollecteDetailResponse> moderateCollecte(
            @PathVariable Long id,
            @Valid @RequestBody AdminModerateCollecteRequest request) {
        return ResponseEntity.ok(collecteService.moderateCollecte(id, request));
    }

    @PatchMapping("/{id}/photo")
    @Operation(summary = "Mettre à jour la photo du spécimen", description = "Permet de modifier l'URL de la photo du spécimen d'une collecte.")
    @ApiResponse(responseCode = "200", description = "Photo mise à jour avec succès")
    @ApiResponse(responseCode = "404", description = "Collecte introuvable")
    public ResponseEntity<AdminCollecteDetailResponse> updatePhoto(
            @PathVariable Long id,
            @RequestBody java.util.Map<String, String> body) {
        return ResponseEntity.ok(collecteService.updatePhoto(id, body.get("photoUrl")));
    }
}
