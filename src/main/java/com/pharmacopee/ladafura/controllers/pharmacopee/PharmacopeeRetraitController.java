package com.pharmacopee.ladafura.controllers.pharmacopee;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pharmacopee.ladafura.dto.pharmacopee.retrait.ConfigureModesRetraitGlobalRequest;
import com.pharmacopee.ladafura.dto.pharmacopee.retrait.ModeRetraitResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.retrait.PharmacopeeModesRetraitSummaryResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.retrait.UpdateModeRetraitRequest;
import com.pharmacopee.ladafura.enums.TypeModeRetrait;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeRetraitService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/pharmacopee/modes-retrait")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Pharmacopée - Livraison & Pickup", description = "Endpoints de gestion et configuration des modes de mise à disposition (Livraison à domicile et Retrait comptoir/Pickup)")
@SecurityRequirement(name = "bearerAuth")
public class PharmacopeeRetraitController {

    private final IPharmacopeeRetraitService retraitService;

    @Operation(summary = "Consulter la configuration des modes de retrait",
               description = "Renvoie la configuration détaillée des deux modes (Livraison et Pickup), indiquant si au moins un mode ou les deux sont activés.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Configuration récupérée avec succès"),
            @ApiResponse(responseCode = "401", description = "Non authentifié (token Firebase manquant ou invalide)"),
            @ApiResponse(responseCode = "403", description = "Accès refusé ou établissement non validé")
    })
    @GetMapping
    public ResponseEntity<PharmacopeeModesRetraitSummaryResponse> getModesRetrait() {
        log.info("Requête GET /api/v1/pharmacopee/modes-retrait reçue");
        return ResponseEntity.ok(retraitService.getModesRetrait());
    }

    @Operation(summary = "Configuration globale simultanée des modes (Livraison & Pickup)",
               description = "Permet d'activer/désactiver et définir les frais de Livraison et de Pickup simultanément. Les deux modes peuvent être actifs en même temps.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Configuration globale mise à jour avec succès"),
            @ApiResponse(responseCode = "400", description = "Données invalides"),
            @ApiResponse(responseCode = "401", description = "Non authentifié"),
            @ApiResponse(responseCode = "403", description = "Accès refusé ou établissement non validé")
    })
    @PutMapping
    public ResponseEntity<PharmacopeeModesRetraitSummaryResponse> configureModesRetraitGlobal(
            @Valid @RequestBody ConfigureModesRetraitGlobalRequest request) {
        log.info("Requête PUT /api/v1/pharmacopee/modes-retrait reçue");
        return ResponseEntity.ok(retraitService.configureModesRetraitGlobal(request));
    }

    @Operation(summary = "Consulter un mode de retrait spécifique",
               description = "Renvoie la configuration spécifique du mode demandé (LIVRAISON ou PICKUP).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Mode récupéré avec succès"),
            @ApiResponse(responseCode = "400", description = "Type de mode inconnu"),
            @ApiResponse(responseCode = "401", description = "Non authentifié"),
            @ApiResponse(responseCode = "403", description = "Accès refusé ou établissement non validé")
    })
    @GetMapping("/{type}")
    public ResponseEntity<ModeRetraitResponse> getModeRetrait(
            @Parameter(description = "Type de mode de mise à disposition", example = "LIVRAISON")
            @PathVariable TypeModeRetrait type) {
        log.info("Requête GET /api/v1/pharmacopee/modes-retrait/{} reçue", type);
        return ResponseEntity.ok(retraitService.getModeRetrait(type));
    }

    @Operation(summary = "Mettre à jour un mode de retrait spécifique",
               description = "Active/désactive le mode ciblé (LIVRAISON ou PICKUP) et met à jour ses frais applicables en FCFA.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Mode mis à jour avec succès"),
            @ApiResponse(responseCode = "400", description = "Données invalides"),
            @ApiResponse(responseCode = "401", description = "Non authentifié"),
            @ApiResponse(responseCode = "403", description = "Accès refusé ou établissement non validé")
    })
    @PutMapping("/{type}")
    public ResponseEntity<ModeRetraitResponse> updateModeRetrait(
            @Parameter(description = "Type de mode de mise à disposition", example = "LIVRAISON")
            @PathVariable TypeModeRetrait type,
            @Valid @RequestBody UpdateModeRetraitRequest request) {
        log.info("Requête PUT /api/v1/pharmacopee/modes-retrait/{} reçue", type);
        return ResponseEntity.ok(retraitService.updateModeRetrait(type, request));
    }

    @Operation(summary = "Basculer l'activation d'un mode (ON / OFF)",
               description = "Active ou désactive instantanément le mode ciblé sans altérer les frais déjà enregistrés.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Statut basculé avec succès"),
            @ApiResponse(responseCode = "401", description = "Non authentifié"),
            @ApiResponse(responseCode = "403", description = "Accès refusé ou établissement non validé")
    })
    @PatchMapping("/{type}/toggle")
    public ResponseEntity<ModeRetraitResponse> toggleModeRetrait(
            @Parameter(description = "Type de mode de mise à disposition", example = "PICKUP")
            @PathVariable TypeModeRetrait type) {
        log.info("Requête PATCH /api/v1/pharmacopee/modes-retrait/{}/toggle reçue", type);
        return ResponseEntity.ok(retraitService.toggleModeRetrait(type));
    }
}
