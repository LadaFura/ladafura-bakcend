package com.pharmacopee.ladafura.controllers.population;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pharmacopee.ladafura.dto.population.profil.PopulationProfileResponse;
import com.pharmacopee.ladafura.dto.population.profil.PopulationUpdateProfileRequest;
import com.pharmacopee.ladafura.services.interfaces.IPopulationProfileService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/population/profile")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasAuthority('ROLE_POPULATION')")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Population - Profil", description = "Endpoints de consultation et de mise à jour des coordonnées personnelles pour l'utilisateur Population")
public class PopulationProfileController {

    private final IPopulationProfileService populationProfileService;

    @GetMapping
    @Operation(summary = "Consulter son profil et son statut",
               description = "Permet à l'utilisateur Population connecté de consulter ses coordonnées, son statut et ses compteurs d'activité (commandes, favoris).")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Profil récupéré avec succès"),
        @ApiResponse(responseCode = "401", description = "Token JWT Firebase absent ou invalide"),
        @ApiResponse(responseCode = "403", description = "Accès refusé : rôle POPULATION requis ou compte inactif")
    })
    public ResponseEntity<PopulationProfileResponse> getProfile() {
        log.info("Requête GET /api/v1/population/profile reçue");
        return ResponseEntity.ok(populationProfileService.getProfile());
    }

    @PutMapping
    @Operation(summary = "Mettre à jour ses coordonnées autorisées",
               description = "Permet à l'utilisateur de modifier son nom, prénom et téléphone. Le rôle et le statut restent rigoureusement protégés et inaltérables.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Profil mis à jour avec succès"),
        @ApiResponse(responseCode = "400", description = "Données d'entrée invalides"),
        @ApiResponse(responseCode = "401", description = "Token JWT Firebase absent ou invalide"),
        @ApiResponse(responseCode = "403", description = "Accès refusé")
    })
    public ResponseEntity<PopulationProfileResponse> updateProfile(@Valid @RequestBody PopulationUpdateProfileRequest request) {
        log.info("Requête PUT /api/v1/population/profile reçue");
        return ResponseEntity.ok(populationProfileService.updateProfile(request));
    }
}
