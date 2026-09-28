package com.pharmacopee.ladafura.controllers.agent;

import java.util.List;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pharmacopee.ladafura.dto.agent.plante.AgentCreatePlanteRequest;
import com.pharmacopee.ladafura.dto.agent.plante.AgentPlanteAssociationResponse;
import com.pharmacopee.ladafura.dto.agent.plante.AgentPlanteResponse;
import com.pharmacopee.ladafura.services.interfaces.IAgentPlanteService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/agent")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Agent de Collecte - Identification des Plantes", description = "Endpoints de recherche, sélection, création de plantes et d'association aux collectes terrain")
@SecurityRequirement(name = "bearerAuth")
public class AgentPlanteController {

    private final IAgentPlanteService agentPlanteService;

    @GetMapping("/plantes")
    @Operation(summary = "Rechercher des plantes médicinales",
               description = "Permet de rechercher une plante existante par nom scientifique ou nom vernaculaire local, avec pagination.")
    @ApiResponse(responseCode = "200", description = "Liste paginée des plantes correspondant aux critères")
    public ResponseEntity<Page<AgentPlanteResponse>> searchPlantes(
            @RequestParam(required = false) String search,
            @ParameterObject Pageable pageable) {
        log.info("Requête de recherche de plantes reçue (search='{}')", search);
        return ResponseEntity.ok(agentPlanteService.searchPlantes(search, pageable));
    }

    @GetMapping("/plantes/{id}")
    @Operation(summary = "Obtenir le détail d'une plante",
               description = "Consulter la fiche descriptive d'une plante et l'ensemble de ses noms vernaculaires enregistrés.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Plante trouvée"),
        @ApiResponse(responseCode = "404", description = "Plante introuvable")
    })
    public ResponseEntity<AgentPlanteResponse> getPlanteById(@PathVariable Long id) {
        log.info("Requête de consultation de la plante ID {}", id);
        return ResponseEntity.ok(agentPlanteService.getPlanteById(id));
    }

    @PostMapping("/plantes")
    @Operation(summary = "Créer une nouvelle plante (si non existante)",
               description = "Enregistre une nouvelle plante au statut BROUILLON avec ses noms vernaculaires initiaux. Bloque la création si le nom scientifique existe déjà.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Plante créée avec succès"),
        @ApiResponse(responseCode = "400", description = "Données invalides"),
        @ApiResponse(responseCode = "409", description = "Conflit : une plante portant ce nom scientifique existe déjà")
    })
    public ResponseEntity<AgentPlanteResponse> createPlante(@Valid @RequestBody AgentCreatePlanteRequest request) {
        log.info("Requête de création d'une nouvelle plante reçue : '{}'", request.getNomScientifique());
        AgentPlanteResponse response = agentPlanteService.createPlante(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/collectes/{collecteId}/plantes/{planteId}")
    @Operation(summary = "Associer une plante à une collecte",
               description = "Rattache une plante identifiée à une collecte appartenant à l'agent connecté (collecte au statut BROUILLON ou REJETEE). Évite la création de doublons.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Plante associée avec succès à la collecte"),
        @ApiResponse(responseCode = "400", description = "Opération impossible (collecte déjà soumise ou validée)"),
        @ApiResponse(responseCode = "403", description = "Accès refusé : la collecte appartient à un autre agent"),
        @ApiResponse(responseCode = "404", description = "Collecte ou plante introuvable")
    })
    public ResponseEntity<AgentPlanteAssociationResponse> associatePlanteToCollecte(
            @PathVariable Long collecteId,
            @PathVariable Long planteId) {
        log.info("Requête d'association de la plante ID {} à la collecte ID {}", planteId, collecteId);
        return ResponseEntity.ok(agentPlanteService.associatePlanteToCollecte(collecteId, planteId));
    }

    @GetMapping("/collectes/{collecteId}/plantes")
    @Operation(summary = "Lister les plantes associées à une collecte",
               description = "Récupère toutes les plantes déjà rattachées à la fiche de collecte spécifiée.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Liste des plantes associées récupérée avec succès"),
        @ApiResponse(responseCode = "403", description = "Accès refusé : la collecte appartient à un autre agent"),
        @ApiResponse(responseCode = "404", description = "Collecte introuvable")
    })
    public ResponseEntity<List<AgentPlanteAssociationResponse>> getPlantesByCollecte(@PathVariable Long collecteId) {
        log.info("Requête de consultation des plantes associées à la collecte ID {}", collecteId);
        return ResponseEntity.ok(agentPlanteService.getPlantesByCollecte(collecteId));
    }

    @DeleteMapping("/collectes/{collecteId}/plantes/{planteId}")
    @Operation(summary = "Dissocier une plante d'une collecte",
               description = "Retire l'association d'une plante à une fiche de collecte (autorisé uniquement si statut BROUILLON ou REJETEE).")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Plante dissociée avec succès"),
        @ApiResponse(responseCode = "400", description = "Opération impossible (collecte déjà soumise)"),
        @ApiResponse(responseCode = "403", description = "Accès refusé"),
        @ApiResponse(responseCode = "404", description = "Association introuvable")
    })
    public ResponseEntity<Void> dissociatePlanteFromCollecte(
            @PathVariable Long collecteId,
            @PathVariable Long planteId) {
        log.info("Requête de dissociation de la plante ID {} de la collecte ID {}", planteId, collecteId);
        agentPlanteService.dissociatePlanteFromCollecte(collecteId, planteId);
        return ResponseEntity.noContent().build();
    }
}
