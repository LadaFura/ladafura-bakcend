package com.pharmacopee.ladafura.controllers.agent;

import java.util.List;

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
import org.springframework.web.bind.annotation.RestController;

import com.pharmacopee.ladafura.dto.agent.connaissance.AgentConnaissanceRequest;
import com.pharmacopee.ladafura.dto.agent.connaissance.AgentConnaissanceResponse;
import com.pharmacopee.ladafura.dto.agent.connaissance.AgentConnaissanceUpdateRequest;
import com.pharmacopee.ladafura.services.interfaces.IAgentConnaissanceService;

import io.swagger.v3.oas.annotations.Operation;
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
@Tag(name = "Agent - Connaissances Traditionnelles", description = "Endpoints réservés aux agents de collecte pour recueillir et documenter le savoir traditionnel de terrain (usages rapportés, organes utilisés, modes de préparation, précautions, sources et maladies). Note : les connaissances traditionnelles recueillies ne constituent pas des preuves scientifiques ou des allégations médicales.")
public class AgentConnaissanceController {

    private final IAgentConnaissanceService agentConnaissanceService;

    @PostMapping("/connaissances")
    @Operation(summary = "Enregistrer une connaissance traditionnelle recueillie",
               description = "Permet à l'agent d'enregistrer un usage traditionnel rapporté par un praticien/herboriste, les organes de la plante utilisés, le mode de préparation, les précautions d'emploi et les maladies ciblées. Crée ou met à jour la liaison VertuDeLaPlante en statut BROUILLON.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Connaissance traditionnelle enregistrée avec succès"),
        @ApiResponse(responseCode = "400", description = "Données invalides ou collecte non modifiable"),
        @ApiResponse(responseCode = "403", description = "Collecte n'appartenant pas à l'agent connecté"),
        @ApiResponse(responseCode = "404", description = "Collecte, Plante ou Source introuvable")
    })
    public ResponseEntity<AgentConnaissanceResponse> enregistrerConnaissance(@Valid @RequestBody AgentConnaissanceRequest request) {
        log.info("Requête de recueil de connaissance traditionnelle pour la collecte ID {} et plante ID {}",
                request.getCollecteId(), request.getPlanteId());
        AgentConnaissanceResponse response = agentConnaissanceService.enregistrerConnaissance(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/connaissances/{id}")
    @Operation(summary = "Modifier une connaissance traditionnelle",
               description = "Met à jour les informations d'une connaissance traditionnelle recueillie. Uniquement possible si la fiche de collecte parente est en statut BROUILLON ou REJETEE.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Connaissance traditionnelle mise à jour"),
        @ApiResponse(responseCode = "400", description = "Collecte parente verrouillée ou statut incompatible"),
        @ApiResponse(responseCode = "403", description = "Non propriétaire de la collecte"),
        @ApiResponse(responseCode = "404", description = "Connaissance introuvable")
    })
    public ResponseEntity<AgentConnaissanceResponse> modifierConnaissance(
            @PathVariable Long id,
            @Valid @RequestBody AgentConnaissanceUpdateRequest request) {
        log.info("Requête de mise à jour de la connaissance traditionnelle ID {}", id);
        AgentConnaissanceResponse response = agentConnaissanceService.modifierConnaissance(id, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/connaissances/{id}")
    @Operation(summary = "Consulter le détail d'une connaissance traditionnelle",
               description = "Retourne la fiche complète de la connaissance traditionnelle avec les détails botaniques, la source et la mention de non-validation scientifique.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Détail de la connaissance récupéré"),
        @ApiResponse(responseCode = "404", description = "Connaissance introuvable")
    })
    public ResponseEntity<AgentConnaissanceResponse> getConnaissanceById(@PathVariable Long id) {
        AgentConnaissanceResponse response = agentConnaissanceService.getConnaissanceById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/collectes/{collecteId}/connaissances")
    @Operation(summary = "Lister les connaissances traditionnelles d'une collecte",
               description = "Retourne toutes les connaissances et usages traditionnels documentés au sein d'une fiche de collecte spécifique appartenant à l'agent.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Liste des connaissances de la collecte"),
        @ApiResponse(responseCode = "403", description = "Collecte n'appartenant pas à l'agent"),
        @ApiResponse(responseCode = "404", description = "Collecte introuvable")
    })
    public ResponseEntity<List<AgentConnaissanceResponse>> getConnaissancesByCollecte(@PathVariable Long collecteId) {
        List<AgentConnaissanceResponse> list = agentConnaissanceService.getConnaissancesByCollecte(collecteId);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/plantes/{planteId}/connaissances")
    @Operation(summary = "Lister les connaissances traditionnelles recensées pour une plante",
               description = "Permet à l'agent de consulter les différents usages traditionnels de terrain recensés pour une plante botanique donnée.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Liste des connaissances pour la plante"),
        @ApiResponse(responseCode = "404", description = "Plante introuvable")
    })
    public ResponseEntity<List<AgentConnaissanceResponse>> getConnaissancesByPlante(@PathVariable Long planteId) {
        List<AgentConnaissanceResponse> list = agentConnaissanceService.getConnaissancesByPlante(planteId);
        return ResponseEntity.ok(list);
    }

    @DeleteMapping("/connaissances/{id}")
    @Operation(summary = "Supprimer une connaissance traditionnelle",
               description = "Supprime une connaissance d'une fiche de collecte. Uniquement autorisé si la collecte est au statut BROUILLON.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Connaissance supprimée avec succès"),
        @ApiResponse(responseCode = "400", description = "Collecte déjà soumise ou validée"),
        @ApiResponse(responseCode = "403", description = "Non propriétaire de la collecte"),
        @ApiResponse(responseCode = "404", description = "Connaissance introuvable")
    })
    public ResponseEntity<Void> supprimerConnaissance(@PathVariable Long id) {
        log.info("Requête de suppression de la connaissance traditionnelle ID {}", id);
        agentConnaissanceService.supprimerConnaissance(id);
        return ResponseEntity.noContent().build();
    }
}
