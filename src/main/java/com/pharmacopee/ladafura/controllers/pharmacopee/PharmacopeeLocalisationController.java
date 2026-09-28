package com.pharmacopee.ladafura.controllers.pharmacopee;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pharmacopee.ladafura.dto.pharmacopee.localisation.PharmacopeeLocalisationRequest;
import com.pharmacopee.ladafura.dto.pharmacopee.localisation.PharmacopeeLocalisationResponse;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeLocalisationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/pharmacopee/localisation")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Pharmacopée - Localisation & Cartographie", description = "Endpoints de gestion de l'adresse géographique et des coordonnées GPS de l'officine")
@SecurityRequirement(name = "bearerAuth")
public class PharmacopeeLocalisationController {

    private final IPharmacopeeLocalisationService localisationService;

    @GetMapping
    @Operation(summary = "Consulter la localisation de sa pharmacopée",
               description = "Fournit les informations géographiques officielles (région, cercle, commune, quartier) et les coordonnées GPS de l'établissement.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Localisation récupérée avec succès"),
        @ApiResponse(responseCode = "401", description = "Token JWT Firebase absent ou invalide"),
        @ApiResponse(responseCode = "403", description = "Accès refusé : compte non actif ou rôle non PHARMACOPEE"),
        @ApiResponse(responseCode = "404", description = "Aucune localisation n'a encore été configurée pour cette pharmacopée")
    })
    public ResponseEntity<PharmacopeeLocalisationResponse> getLocalisation() {
        log.info("Consultation de la localisation de la pharmacopée connectée");
        return ResponseEntity.ok(localisationService.getLocalisation());
    }

    @PutMapping
    @Operation(summary = "Enregistrer ou modifier la localisation de sa pharmacopée",
               description = "Permet à la pharmacopée d'enregistrer ou mettre à jour son adresse administrative et ses coordonnées GPS pour la cartographie et la recherche de proximité.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Localisation enregistrée ou mise à jour avec succès"),
        @ApiResponse(responseCode = "400", description = "Données géographiques ou coordonnées GPS invalides"),
        @ApiResponse(responseCode = "401", description = "Token JWT Firebase absent ou invalide"),
        @ApiResponse(responseCode = "403", description = "Accès refusé : compte non actif ou rôle non PHARMACOPEE")
    })
    public ResponseEntity<PharmacopeeLocalisationResponse> saveOrUpdateLocalisation(
            @Valid @RequestBody PharmacopeeLocalisationRequest request) {
        log.info("Mise à jour des coordonnées géographiques de la pharmacopée connectée ({}, {})",
                request.getRegion(), request.getLocalite());
        return ResponseEntity.ok(localisationService.saveOrUpdateLocalisation(request));
    }
}
