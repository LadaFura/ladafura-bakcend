package com.pharmacopee.ladafura.controllers.admin;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pharmacopee.ladafura.dto.admin.abonnement.DefinirQuotasOffreRequest;
import com.pharmacopee.ladafura.dto.admin.abonnement.EnregistrerOffreRequest;
import com.pharmacopee.ladafura.dto.admin.abonnement.LimiteOffreResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.abonnement.OffreResponse;
import com.pharmacopee.ladafura.services.interfaces.IAdminOffreService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Tag(name = "Admin - Offres", description = "Catalogue des offres d'abonnement et de leurs quotas")
@SecurityRequirement(name = "bearerAuth")
public class AdminOffreController {

    private final IAdminOffreService offreService;

    @GetMapping("/offres")
    @Operation(summary = "Lister les offres")
    @ApiResponse(responseCode = "200", description = "Toutes les offres")
    public ResponseEntity<List<OffreResponse>> getOffres() {
        return ResponseEntity.ok(offreService.getOffres());
    }

    @PostMapping("/offres")
    @Operation(summary = "Créer une offre", description = "Créée inactive. La durée découle du type.")
    @ApiResponse(responseCode = "201", description = "Offre créée")
    @ApiResponse(responseCode = "409", description = "Code déjà utilisé")
    public ResponseEntity<OffreResponse> creer(@Valid @RequestBody EnregistrerOffreRequest request) {
        return new ResponseEntity<>(offreService.creer(request), HttpStatus.CREATED);
    }

    @PutMapping("/offres/{id}")
    @Operation(summary = "Modifier une offre", description = "Sans effet sur les abonnements déjà souscrits.")
    @ApiResponse(responseCode = "200", description = "Offre modifiée")
    public ResponseEntity<OffreResponse> modifier(@PathVariable Long id, @Valid @RequestBody EnregistrerOffreRequest request) {
        return ResponseEntity.ok(offreService.modifier(id, request));
    }

    @PatchMapping("/offres/{id}/activer")
    @Operation(summary = "Activer une offre", description = "Une offre payante exige un prix positif et tous ses quotas.")
    @ApiResponse(responseCode = "200", description = "Offre activée")
    @ApiResponse(responseCode = "400", description = "Prix ou quotas manquants")
    public ResponseEntity<OffreResponse> activer(@PathVariable Long id) {
        return ResponseEntity.ok(offreService.activer(id));
    }

    @PatchMapping("/offres/{id}/desactiver")
    @Operation(summary = "Désactiver une offre", description = "L'offre gratuite reste toujours active.")
    @ApiResponse(responseCode = "200", description = "Offre désactivée")
    @ApiResponse(responseCode = "409", description = "Offre gratuite")
    public ResponseEntity<OffreResponse> desactiver(@PathVariable Long id) {
        return ResponseEntity.ok(offreService.desactiver(id));
    }

    @GetMapping("/limites")
    @Operation(summary = "Lister les limites du catalogue")
    @ApiResponse(responseCode = "200", description = "Limites")
    public ResponseEntity<List<LimiteOffreResponse>> getLimites() {
        return ResponseEntity.ok(offreService.getLimites());
    }

    @PutMapping("/offres/{id}/limites")
    @Operation(summary = "Définir les quotas d'une offre", description = "Remplace la liste complète.")
    @ApiResponse(responseCode = "200", description = "Quotas enregistrés")
    public ResponseEntity<OffreResponse> definirQuotas(@PathVariable Long id, @Valid @RequestBody DefinirQuotasOffreRequest request) {
        return ResponseEntity.ok(offreService.definirQuotas(id, request));
    }
}
