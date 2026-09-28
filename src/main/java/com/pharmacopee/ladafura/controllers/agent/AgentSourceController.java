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

import com.pharmacopee.ladafura.dto.agent.source.AgentCreateSourceRequest;
import com.pharmacopee.ladafura.dto.agent.source.AgentSourceResponse;
import com.pharmacopee.ladafura.dto.agent.source.AgentUpdateSourceRequest;
import com.pharmacopee.ladafura.enums.Role;
import com.pharmacopee.ladafura.services.interfaces.IAgentSourceService;

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
@Tag(name = "Agent - Sources de Données", description = "Endpoints réservés aux agents de collecte pour recenser et gérer les sources d'information de terrain (thérapeutes traditionnels, herboristes). Les sources enregistrées sont des référents de traçabilité et ne constituent pas des comptes utilisateurs.")
public class AgentSourceController {

    private final IAgentSourceService agentSourceService;

    @PostMapping("/sources")
    @Operation(summary = "Enregistrer une source de terrain",
               description = "Permet à l'agent d'enregistrer un thérapeute traditionnel ou un herboriste rencontré sur le terrain. Ne crée aucun compte utilisateur dans Firebase.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Source enregistrée avec succès"),
        @ApiResponse(responseCode = "400", description = "Données invalides"),
        @ApiResponse(responseCode = "409", description = "Email déjà utilisé par un autre enregistrement")
    })
    public ResponseEntity<AgentSourceResponse> creerSource(@Valid @RequestBody AgentCreateSourceRequest request) {
        log.info("Requête d'enregistrement d'une source : '{} {}' ({})",
                request.getPrenom(), request.getNom(), request.getRole());
        AgentSourceResponse response = agentSourceService.creerSource(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/sources/{id}")
    @Operation(summary = "Mettre à jour les informations d'une source",
               description = "Permet de mettre à jour le téléphone, l'adresse, la spécialité ou le rôle d'une source de terrain.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Source mise à jour avec succès"),
        @ApiResponse(responseCode = "404", description = "Source introuvable"),
        @ApiResponse(responseCode = "409", description = "Conflit sur l'adresse email")
    })
    public ResponseEntity<AgentSourceResponse> modifierSource(
            @PathVariable Long id,
            @Valid @RequestBody AgentUpdateSourceRequest request) {
        log.info("Requête de mise à jour de la source ID {}", id);
        AgentSourceResponse response = agentSourceService.modifierSource(id, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/sources/{id}")
    @Operation(summary = "Consulter le détail d'une source",
               description = "Retourne la fiche complète d'une source de terrain avec ses coordonnées de traçabilité.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Détail de la source récupéré"),
        @ApiResponse(responseCode = "404", description = "Source introuvable")
    })
    public ResponseEntity<AgentSourceResponse> getSourceById(@PathVariable Long id) {
        AgentSourceResponse response = agentSourceService.getSourceById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/sources")
    @Operation(summary = "Rechercher et lister les sources de terrain",
               description = "Recherche paginée de sources de données par mot-clé (nom, prénom, spécialité, adresse, téléphone) et par profil (THERAPEUTE ou HERBORISTE).")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Page de sources récupérée avec succès")
    })
    public ResponseEntity<Page<AgentSourceResponse>> rechercherSources(
            @Parameter(description = "Mot-clé de recherche (nom, prénom, spécialité, adresse, téléphone)")
            @RequestParam(required = false) String query,
            @Parameter(description = "Profil de la source (THERAPEUTE ou HERBORISTE)")
            @RequestParam(required = false) Role role,
            @PageableDefault(page = 0, size = 20, sort = "nom", direction = Sort.Direction.ASC) Pageable pageable) {
        log.info("Recherche de sources (query='{}', role='{}')", query, role);
        Page<AgentSourceResponse> page = agentSourceService.rechercherSources(query, role, pageable);
        return ResponseEntity.ok(page);
    }

    @PostMapping("/collectes/{collecteId}/sources/{sourceId}")
    @Operation(summary = "Associer une source à une fiche de collecte",
               description = "Rattache une source de données existante à une collecte. Uniquement autorisé si la collecte appartient à l'agent et est en statut BROUILLON ou REJETEE.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Source associée à la collecte"),
        @ApiResponse(responseCode = "400", description = "Collecte non modifiable"),
        @ApiResponse(responseCode = "403", description = "Collecte n'appartenant pas à l'agent"),
        @ApiResponse(responseCode = "404", description = "Collecte ou source introuvable")
    })
    public ResponseEntity<Void> associerSourceACollecte(
            @PathVariable Long collecteId,
            @PathVariable Long sourceId) {
        log.info("Association de la source ID {} à la collecte ID {}", sourceId, collecteId);
        agentSourceService.associerSourceACollecte(collecteId, sourceId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/collectes/{collecteId}/source")
    @Operation(summary = "Dissocier la source d'une fiche de collecte",
               description = "Retire l'association de source sur une fiche de collecte modifiable.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Source dissociée avec succès"),
        @ApiResponse(responseCode = "400", description = "Collecte non modifiable"),
        @ApiResponse(responseCode = "403", description = "Collecte n'appartenant pas à l'agent"),
        @ApiResponse(responseCode = "404", description = "Collecte introuvable")
    })
    public ResponseEntity<Void> dissocierSourceDeCollecte(@PathVariable Long collecteId) {
        log.info("Dissociation de la source sur la collecte ID {}", collecteId);
        agentSourceService.dissocierSourceDeCollecte(collecteId);
        return ResponseEntity.noContent().build();
    }
}
