package com.pharmacopee.ladafura.controllers.agent;

import org.springdoc.core.annotations.ParameterObject;
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

import com.pharmacopee.ladafura.dto.agent.collecte.AgentCollecteDetailResponse;
import com.pharmacopee.ladafura.dto.agent.collecte.AgentCollecteSummaryResponse;
import com.pharmacopee.ladafura.dto.agent.collecte.AgentCreateCollecteRequest;
import com.pharmacopee.ladafura.dto.agent.collecte.AgentUpdateCollecteRequest;
import com.pharmacopee.ladafura.enums.StatutCollecte;
import com.pharmacopee.ladafura.services.interfaces.IAgentCollecteService;

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
@RequestMapping("/api/v1/agent/collectes")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasAuthority('ROLE_AGENT_COLLECTE')")
@Tag(name = "Agent - Gestion des Collectes", description = "Endpoints de gestion du cycle de vie des collectes terrain (création, brouillon, modification, consultation, soumission)")
@SecurityRequirement(name = "bearerAuth")
public class AgentCollecteController {

    private final IAgentCollecteService collecteService;

    @GetMapping
    @Operation(summary = "Lister ses propres collectes avec pagination et filtre par statut",
               description = "Permet à l'agent connecté de consulter l'ensemble de ses fiches de collecte. Filtre possible par statut (BROUILLON, SOUMISE, EN_EXAMEN, VALIDEE, REJETEE).")
    @ApiResponse(responseCode = "200", description = "Liste paginée des collectes de l'agent récupérée avec succès")
    public ResponseEntity<Page<AgentCollecteSummaryResponse>> getMyCollectes(
            @Parameter(description = "Filtre optionnel par statut de la collecte (BROUILLON, SOUMISE, EN_EXAMEN, VALIDEE, REJETEE)")
            @RequestParam(required = false) StatutCollecte statut,
            @ParameterObject @PageableDefault(sort = "dateCollecte", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("Requête de consultation des collectes de l'agent (statut={})", statut);
        return ResponseEntity.ok(collecteService.getMyCollectes(statut, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulter le détail d'une de ses collectes",
               description = "Renvoie les informations complètes d'une collecte appartenant à l'agent connecté (statut, médias, géolocalisation, source).")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Détail de la collecte récupéré avec succès"),
        @ApiResponse(responseCode = "403", description = "Accès refusé : la collecte appartient à un autre agent"),
        @ApiResponse(responseCode = "404", description = "Collecte introuvable")
    })
    public ResponseEntity<AgentCollecteDetailResponse> getMyCollecteById(@PathVariable Long id) {
        log.info("Requête de consultation du détail de la collecte ID {}", id);
        return ResponseEntity.ok(collecteService.getMyCollecteById(id));
    }

    @PostMapping
    @Operation(summary = "Créer une collecte terrain",
               description = "Crée une nouvelle collecte rattachée à l'agent. Par défaut enregistrée en BROUILLON, ou SOUMISE si 'soumettre' vaut true.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Collecte créée avec succès"),
        @ApiResponse(responseCode = "400", description = "Données d'entrée invalides")
    })
    public ResponseEntity<AgentCollecteDetailResponse> createCollecte(@Valid @RequestBody AgentCreateCollecteRequest request) {
        log.info("Requête de création d'une collecte reçue");
        AgentCollecteDetailResponse response = collecteService.createCollecte(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/draft")
    @Operation(summary = "Enregistrer une collecte comme brouillon",
               description = "Point d'accès explicite pour sauvegarder un travail de collecte en cours avec le statut BROUILLON.")
    @ApiResponse(responseCode = "201", description = "Brouillon sauvegardé avec succès")
    public ResponseEntity<AgentCollecteDetailResponse> saveDraft(@Valid @RequestBody AgentCreateCollecteRequest request) {
        log.info("Requête d'enregistrement d'un brouillon de collecte reçue");
        AgentCollecteDetailResponse response = collecteService.saveDraft(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier une collecte (brouillon ou après rejet)",
               description = "Permet de modifier une collecte tant qu'elle est en statut BROUILLON ou REJETEE (demande de correction de l'administrateur).")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Collecte modifiée avec succès"),
        @ApiResponse(responseCode = "400", description = "Modification impossible (collecte déjà soumise ou validée)"),
        @ApiResponse(responseCode = "403", description = "Accès refusé"),
        @ApiResponse(responseCode = "404", description = "Collecte introuvable")
    })
    public ResponseEntity<AgentCollecteDetailResponse> updateCollecte(
            @PathVariable Long id,
            @Valid @RequestBody AgentUpdateCollecteRequest request) {
        log.info("Requête de modification de la collecte ID {}", id);
        return ResponseEntity.ok(collecteService.updateCollecte(id, request));
    }

    @PostMapping("/{id}/submit")
    @Operation(summary = "Soumettre une collecte pour vérification",
               description = "Passe la collecte du statut BROUILLON ou REJETEE au statut SOUMISE, déclenchant le workflow d'examen administratif.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Collecte soumise avec succès"),
        @ApiResponse(responseCode = "400", description = "Soumission impossible (statut incompatible)"),
        @ApiResponse(responseCode = "403", description = "Accès refusé"),
        @ApiResponse(responseCode = "404", description = "Collecte introuvable")
    })
    public ResponseEntity<AgentCollecteDetailResponse> submitCollecte(@PathVariable Long id) {
        log.info("Requête de soumission de la collecte ID {}", id);
        return ResponseEntity.ok(collecteService.submitCollecte(id));
    }

    @DeleteMapping("/{id}/draft")
    @Operation(summary = "Supprimer un brouillon de collecte",
               description = "Permet de purger définitivement une collecte au statut BROUILLON.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Brouillon supprimé avec succès"),
        @ApiResponse(responseCode = "400", description = "Suppression impossible (seuls les brouillons peuvent être supprimés)"),
        @ApiResponse(responseCode = "403", description = "Accès refusé"),
        @ApiResponse(responseCode = "404", description = "Collecte introuvable")
    })
    public ResponseEntity<Void> deleteDraft(@PathVariable Long id) {
        log.info("Requête de suppression du brouillon de collecte ID {}", id);
        collecteService.deleteDraft(id);
        return ResponseEntity.noContent().build();
    }
}
