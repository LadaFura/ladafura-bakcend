package com.pharmacopee.ladafura.controllers.agent;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pharmacopee.ladafura.dto.agent.collecte.AgentCollecteSummaryResponse;
import com.pharmacopee.ladafura.dto.agent.suivi.AgentCollecteRejetDetailResponse;
import com.pharmacopee.ladafura.dto.agent.suivi.AgentSuiviStatsResponse;
import com.pharmacopee.ladafura.enums.StatutCollecte;
import com.pharmacopee.ladafura.services.interfaces.IAgentSuiviCollecteService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/agent/suivi")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasAuthority('ROLE_AGENT_COLLECTE')")
@Tag(name = "Agent - Suivi des Collectes", description = "Endpoints de suivi et de pilotage du workflow des fiches de collecte : compteurs par statut, listes filtrées (brouillons, soumises, en examen, validées, rejetées) et détail explicatif des motifs de rejet avec guide de correction.")
public class AgentSuiviController {

    private final IAgentSuiviCollecteService suiviCollecteService;

    @GetMapping("/stats")
    @Operation(summary = "Obtenir les statistiques synthétiques de suivi",
               description = "Renvoie les compteurs réels des collectes de l'agent connecté : total, brouillons, soumises, en examen, validées, rejetées/à corriger et le taux de validation.")
    @ApiResponse(responseCode = "200", description = "Statistiques de suivi récupérées avec succès")
    public ResponseEntity<AgentSuiviStatsResponse> getStatsSuivi() {
        log.info("Consultation des statistiques de suivi de collecte pour l'agent connecté");
        return ResponseEntity.ok(suiviCollecteService.getStatsSuivi());
    }

    @GetMapping("/brouillons")
    @Operation(summary = "Consulter la liste de ses brouillons",
               description = "Renvoie la liste paginée de toutes les collectes actuellement au statut BROUILLON.")
    @ApiResponse(responseCode = "200", description = "Liste paginée des brouillons")
    public ResponseEntity<Page<AgentCollecteSummaryResponse>> getBrouillons(
            @ParameterObject @PageableDefault(sort = "dateCollecte", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("Consultation des brouillons de l'agent");
        return ResponseEntity.ok(suiviCollecteService.getBrouillons(pageable));
    }

    @GetMapping("/soumises")
    @Operation(summary = "Consulter la liste de ses collectes soumises",
               description = "Renvoie la liste paginée des collectes soumises en attente d'attribution ou d'examen.")
    @ApiResponse(responseCode = "200", description = "Liste paginée des collectes soumises")
    public ResponseEntity<Page<AgentCollecteSummaryResponse>> getSoumises(
            @ParameterObject @PageableDefault(sort = "dateSoumission", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("Consultation des collectes soumises de l'agent");
        return ResponseEntity.ok(suiviCollecteService.getSoumises(pageable));
    }

    @GetMapping("/en-examen")
    @Operation(summary = "Consulter la liste de ses collectes en attente d'examen",
               description = "Renvoie la liste paginée des collectes actuellement en cours d'examen par les modérateurs/administrateurs.")
    @ApiResponse(responseCode = "200", description = "Liste paginée des collectes en examen")
    public ResponseEntity<Page<AgentCollecteSummaryResponse>> getEnExamen(
            @ParameterObject @PageableDefault(sort = "dateSoumission", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("Consultation des collectes en cours d'examen de l'agent");
        return ResponseEntity.ok(suiviCollecteService.getEnExamen(pageable));
    }

    @GetMapping("/validees")
    @Operation(summary = "Consulter la liste de ses collectes validées",
               description = "Renvoie la liste paginée des collectes définitivement validées et valorisées.")
    @ApiResponse(responseCode = "200", description = "Liste paginée des collectes validées")
    public ResponseEntity<Page<AgentCollecteSummaryResponse>> getValidees(
            @ParameterObject @PageableDefault(sort = "dateCollecte", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("Consultation des collectes validées de l'agent");
        return ResponseEntity.ok(suiviCollecteService.getValidees(pageable));
    }

    @GetMapping("/rejetees")
    @Operation(summary = "Consulter la liste de ses collectes rejetées nécessitant correction",
               description = "Renvoie la liste paginée des collectes ayant fait l'objet d'un refus ou d'une demande de révision par l'administrateur.")
    @ApiResponse(responseCode = "200", description = "Liste paginée des collectes rejetées")
    public ResponseEntity<Page<AgentCollecteSummaryResponse>> getRejetees(
            @ParameterObject @PageableDefault(sort = "dateCollecte", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("Consultation des collectes rejetées / à corriger de l'agent");
        return ResponseEntity.ok(suiviCollecteService.getRejetees(pageable));
    }

    @GetMapping("/{collecteId}/motif-rejet")
    @Operation(summary = "Consulter le motif détaillé de rejet et le guide de correction",
               description = "Permet à l'agent de comprendre pourquoi sa collecte a été rejetée et détaille la démarche à suivre pour rectifier la fiche avant nouvelle soumission.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Motif de rejet et guide de correction récupérés"),
        @ApiResponse(responseCode = "400", description = "La collecte n'est pas au statut REJETEE"),
        @ApiResponse(responseCode = "403", description = "Collecte n'appartenant pas à l'agent connecté"),
        @ApiResponse(responseCode = "404", description = "Collecte introuvable")
    })
    public ResponseEntity<AgentCollecteRejetDetailResponse> getDetailRejet(@PathVariable Long collecteId) {
        log.info("Consultation du détail explicatif du rejet pour la collecte ID {}", collecteId);
        return ResponseEntity.ok(suiviCollecteService.getDetailRejet(collecteId));
    }

    @GetMapping
    @Operation(summary = "Recherche multi-critères et consultation de l'ensemble de ses collectes",
               description = "Permet de rechercher parmi toutes les collectes de l'agent avec filtrage optionnel par statut et texte libre (nom de plante, localité, région, source, etc.).")
    @ApiResponse(responseCode = "200", description = "Résultats de recherche récupérés avec succès")
    public ResponseEntity<Page<AgentCollecteSummaryResponse>> searchSuivi(
            @Parameter(description = "Filtre optionnel par statut de collecte")
            @RequestParam(required = false) StatutCollecte statut,
            @Parameter(description = "Recherche textuelle libre")
            @RequestParam(required = false) String query,
            @ParameterObject @PageableDefault(sort = "dateCollecte", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("Recherche de suivi collectes (statut={}, query='{}')", statut, query);
        return ResponseEntity.ok(suiviCollecteService.searchSuiviCollectes(statut, query, pageable));
    }
}
