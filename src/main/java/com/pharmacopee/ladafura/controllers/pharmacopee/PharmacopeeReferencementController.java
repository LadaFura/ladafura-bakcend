package com.pharmacopee.ladafura.controllers.pharmacopee;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pharmacopee.ladafura.dto.pharmacopee.referencement.DemandeReferencementRequest;
import com.pharmacopee.ladafura.dto.pharmacopee.referencement.StatutReferencementResponse;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeReferencementService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/pharmacopee/referencement")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Pharmacopée - Référencement & Agrément", description = "Endpoints de soumission de dossier et consultation du statut d'agrément officiel")
@SecurityRequirement(name = "bearerAuth")
public class PharmacopeeReferencementController {

    private final IPharmacopeeReferencementService referencementService;

    @PostMapping("/demande")
    @Operation(summary = "Soumettre une demande de référencement",
               description = "Permet à un acteur PHARMACOPEE de soumettre son dossier officiel. La pharmacopée est créée avec le statut EN_ATTENTE.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Demande de référencement créée avec succès"),
        @ApiResponse(responseCode = "400", description = "Données du formulaire invalides"),
        @ApiResponse(responseCode = "401", description = "Token JWT Firebase absent ou invalide"),
        @ApiResponse(responseCode = "403", description = "Accès refusé : compte non actif ou rôle non PHARMACOPEE"),
        @ApiResponse(responseCode = "409", description = "Une demande ou une pharmacopée existe déjà pour ce compte")
    })
    public ResponseEntity<StatutReferencementResponse> soumettreDemande(@Valid @RequestBody DemandeReferencementRequest request) {
        log.info("Réception d'une demande de référencement pour : {}", request.getNom());
        StatutReferencementResponse response = referencementService.soumettreDemandeReferencement(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/statut")
    @Operation(summary = "Consulter le statut d'agrément de sa pharmacopée",
               description = "Permet à la pharmacopée de consulter en temps réel l'avancement de son dossier (EN_ATTENTE, VALIDEE, SUSPENDUE, REJETEE).")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Statut de référencement récupéré avec succès"),
        @ApiResponse(responseCode = "401", description = "Token JWT Firebase absent ou invalide"),
        @ApiResponse(responseCode = "403", description = "Accès refusé : compte inactif ou rôle non PHARMACOPEE")
    })
    public ResponseEntity<StatutReferencementResponse> consulterStatut() {
        log.info("Consultation du statut de référencement de la pharmacopée connectée");
        return ResponseEntity.ok(referencementService.consulterStatutReferencement());
    }
}
