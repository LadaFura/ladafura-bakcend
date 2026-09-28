package com.pharmacopee.ladafura.controllers.agent;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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

import com.pharmacopee.ladafura.dto.agent.localisation.AgentLocalisationRequest;
import com.pharmacopee.ladafura.dto.agent.localisation.AgentLocalisationResponse;
import com.pharmacopee.ladafura.services.interfaces.IAgentLocalisationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/agent")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasAuthority('ROLE_AGENT_COLLECTE')")
@Tag(name = "Agent - Localisation Géographique", description = "Endpoints réservés aux agents de collecte pour renseigner, consulter et modifier la localisation géographique des fiches de collecte (région, cercle, commune, localité/village, coordonnées GPS latitude/longitude).")
public class AgentLocalisationController {

    private final IAgentLocalisationService agentLocalisationService;

    @PostMapping("/collectes/{collecteId}/localisation")
    @Operation(summary = "Enregistrer la localisation géographique d'une collecte",
               description = "Associe ou met à jour la localisation d'une fiche de collecte sur le terrain (relation 1 ── 1). Uniquement autorisée si la collecte est en statut BROUILLON ou REJETEE.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Localisation enregistrée avec succès"),
        @ApiResponse(responseCode = "400", description = "Données invalides ou collecte non modifiable"),
        @ApiResponse(responseCode = "403", description = "Collecte n'appartenant pas à l'agent"),
        @ApiResponse(responseCode = "404", description = "Collecte introuvable")
    })
    public ResponseEntity<AgentLocalisationResponse> enregistrerLocalisation(
            @PathVariable Long collecteId,
            @Valid @RequestBody AgentLocalisationRequest request) {
        log.info("Requête d'enregistrement de localisation pour la collecte ID {} : {}, {}",
                collecteId, request.getLocalite(), request.getRegion());
        AgentLocalisationResponse response = agentLocalisationService.saveOrUpdateLocalisation(collecteId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/collectes/{collecteId}/localisation")
    @Operation(summary = "Modifier la localisation géographique d'une collecte",
               description = "Met à jour les informations de localisation (commune, village, coordonnées GPS) pour une collecte modifiable.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Localisation mise à jour avec succès"),
        @ApiResponse(responseCode = "400", description = "Collecte non modifiable"),
        @ApiResponse(responseCode = "403", description = "Collecte n'appartenant pas à l'agent"),
        @ApiResponse(responseCode = "404", description = "Collecte introuvable")
    })
    public ResponseEntity<AgentLocalisationResponse> modifierLocalisation(
            @PathVariable Long collecteId,
            @Valid @RequestBody AgentLocalisationRequest request) {
        log.info("Mise à jour de la localisation pour la collecte ID {}", collecteId);
        AgentLocalisationResponse response = agentLocalisationService.saveOrUpdateLocalisation(collecteId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/collectes/{collecteId}/localisation")
    @Operation(summary = "Consulter la localisation d'une fiche de collecte",
               description = "Retourne la localisation géographique complète où les informations de terrain ont été recueillies.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Localisation récupérée"),
        @ApiResponse(responseCode = "403", description = "Collecte n'appartenant pas à l'agent"),
        @ApiResponse(responseCode = "404", description = "Collecte ou localisation introuvable")
    })
    public ResponseEntity<AgentLocalisationResponse> getLocalisationByCollecte(@PathVariable Long collecteId) {
        AgentLocalisationResponse response = agentLocalisationService.getLocalisationByCollecte(collecteId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/collectes/{collecteId}/localisation")
    @Operation(summary = "Dissocier la localisation d'une fiche de collecte",
               description = "Retire la localisation rattachée à une fiche de collecte modifiable.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Localisation dissociée avec succès"),
        @ApiResponse(responseCode = "400", description = "Collecte verrouillée"),
        @ApiResponse(responseCode = "403", description = "Collecte n'appartenant pas à l'agent"),
        @ApiResponse(responseCode = "404", description = "Collecte introuvable")
    })
    public ResponseEntity<Void> supprimerLocalisation(@PathVariable Long collecteId) {
        log.info("Dissociation de la localisation pour la collecte ID {}", collecteId);
        agentLocalisationService.supprimerLocalisation(collecteId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/localisations")
    @Operation(summary = "Rechercher et lister des localisations de terrain",
               description = "Recherche paginée de localisations géographiques recensées (région, cercle, commune, localité).")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Page de localisations récupérée avec succès")
    })
    public ResponseEntity<Page<AgentLocalisationResponse>> rechercherLocalisations(
            @Parameter(description = "Mot-clé de recherche géographique (ex: Finkolo, Sikasso)")
            @RequestParam(required = false) String query,
            @PageableDefault(page = 0, size = 20, sort = "region", direction = Sort.Direction.ASC) Pageable pageable) {
        log.info("Recherche de localisations (query='{}')", query);
        Page<AgentLocalisationResponse> page = agentLocalisationService.rechercherLocalisations(query, pageable);
        return ResponseEntity.ok(page);
    }
}
