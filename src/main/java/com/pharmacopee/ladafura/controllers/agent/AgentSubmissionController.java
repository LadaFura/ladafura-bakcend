package com.pharmacopee.ladafura.controllers.agent;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pharmacopee.ladafura.dto.agent.submission.AgentCollecteRecapitulatifResponse;
import com.pharmacopee.ladafura.dto.agent.submission.AgentSubmissionResultResponse;
import com.pharmacopee.ladafura.services.interfaces.IAgentSubmissionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/agent")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasAuthority('ROLE_AGENT_COLLECTE')")
@Tag(name = "Agent - Récapitulatif et Soumission", description = "Endpoints réservés aux agents de collecte pour vérifier le dossier complet avant soumission et soumettre formellement la collecte pour validation.")
public class AgentSubmissionController {

    private final IAgentSubmissionService agentSubmissionService;

    @GetMapping("/collectes/{collecteId}/recapitulatif")
    @Operation(summary = "Consulter le récapitulatif pré-soumission d'une collecte",
               description = "Fournit la synthèse exhaustive de la fiche de collecte (plante, noms vernaculaires, connaissances traditionnelles, source, médias, localisation) ainsi que le bilan de conformité avec la liste des erreurs bloquantes et points de vigilance.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Récapitulatif généré avec succès"),
        @ApiResponse(responseCode = "403", description = "Collecte n'appartenant pas à l'agent connecté"),
        @ApiResponse(responseCode = "404", description = "Collecte introuvable")
    })
    public ResponseEntity<AgentCollecteRecapitulatifResponse> getRecapitulatif(@PathVariable Long collecteId) {
        log.info("Consultation du récapitulatif pré-soumission pour la collecte ID {}", collecteId);
        AgentCollecteRecapitulatifResponse response = agentSubmissionService.getRecapitulatif(collecteId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/collectes/{collecteId}/soumettre")
    @Operation(summary = "Soumettre formellement la collecte pour validation",
               description = "Vérifie la conformité intégrale du dossier. Si le dossier est valide, la collecte passe au statut SOUMISE, les connaissances traditionnelles passent en EN_ATTENTE et la fiche est verrouillée contre toute modification ultérieure.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Collecte soumise avec succès"),
        @ApiResponse(responseCode = "400", description = "Dossier incomplet ou statut non éligible à la soumission"),
        @ApiResponse(responseCode = "403", description = "Collecte n'appartenant pas à l'agent connecté"),
        @ApiResponse(responseCode = "404", description = "Collecte introuvable")
    })
    public ResponseEntity<AgentSubmissionResultResponse> soumettreCollecte(@PathVariable Long collecteId) {
        log.info("Demande de soumission pour la collecte ID {}", collecteId);
        AgentSubmissionResultResponse response = agentSubmissionService.soumettreCollecteVerifiee(collecteId);
        return ResponseEntity.ok(response);
    }
}
