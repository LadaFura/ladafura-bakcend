package com.pharmacopee.ladafura.controllers.pharmacopee;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pharmacopee.ladafura.dto.pharmacopee.profil.PharmacopeeProfileResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.profil.PharmacopeeUpdateProfileRequest;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeProfileService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/pharmacopee/profil")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Pharmacopée - Profil & Établissement", description = "Endpoints de consultation et mise à jour de la fiche de l'officine et de son gérant")
@SecurityRequirement(name = "bearerAuth")
public class PharmacopeeProfileController {

    private final IPharmacopeeProfileService profileService;

    @GetMapping
    @Operation(summary = "Consulter le profil complet de sa pharmacopée",
               description = "Fournit la fiche complète de l'établissement : statut administratif, coordonnées du gérant, localisation, modes de retrait et statistiques de catalogue.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Profil récupéré avec succès"),
        @ApiResponse(responseCode = "401", description = "Token JWT Firebase absent ou invalide"),
        @ApiResponse(responseCode = "403", description = "Accès refusé : compte non actif ou rôle non PHARMACOPEE"),
        @ApiResponse(responseCode = "404", description = "Aucune pharmacopée associée à cet utilisateur")
    })
    public ResponseEntity<PharmacopeeProfileResponse> getProfile() {
        log.info("Consultation du profil complet de la pharmacopée connectée");
        return ResponseEntity.ok(profileService.getProfile());
    }

    @PutMapping
    @Operation(summary = "Mettre à jour les informations de son profil",
               description = "Permet de modifier le nom, la description, les coordonnées téléphoniques (officine et gérant) et la localisation géographique. Le statut de référencement ne peut pas être modifié par ce biais.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Profil mis à jour avec succès"),
        @ApiResponse(responseCode = "400", description = "Données du formulaire invalides"),
        @ApiResponse(responseCode = "401", description = "Token JWT Firebase absent ou invalide"),
        @ApiResponse(responseCode = "403", description = "Accès refusé : compte non actif ou rôle non PHARMACOPEE"),
        @ApiResponse(responseCode = "404", description = "Aucune pharmacopée associée à cet utilisateur")
    })
    public ResponseEntity<PharmacopeeProfileResponse> updateProfile(
            @Valid @RequestBody PharmacopeeUpdateProfileRequest request) {
        log.info("Mise à jour du profil de la pharmacopée connectée (nouveau nom: {})", request.getNom());
        return ResponseEntity.ok(profileService.updateProfile(request));
    }
}
