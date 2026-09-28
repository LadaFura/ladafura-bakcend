package com.pharmacopee.ladafura.controllers.agent;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pharmacopee.ladafura.dto.agent.vernaculaire.AgentNomVernaculaireRequest;
import com.pharmacopee.ladafura.dto.agent.vernaculaire.AgentNomVernaculaireResponse;
import com.pharmacopee.ladafura.services.interfaces.IAgentNomVernaculaireService;

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
@Tag(name = "Agent de Collecte - Noms Vernaculaires", description = "Endpoints de recueil, consultation et enrichissement des noms vernaculaires des plantes")
@SecurityRequirement(name = "bearerAuth")
public class AgentNomVernaculaireController {

    private final IAgentNomVernaculaireService nomVernaculaireService;

    @GetMapping("/plantes/{planteId}/noms-vernaculaires")
    @Operation(summary = "Lister les noms vernaculaires d'une plante",
               description = "Renvoie l'ensemble des appellations locales et dialectales associées à une plante médicinale.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Liste des noms vernaculaires récupérée avec succès"),
        @ApiResponse(responseCode = "404", description = "Plante introuvable")
    })
    public ResponseEntity<List<AgentNomVernaculaireResponse>> getNomsByPlanteId(@PathVariable Long planteId) {
        log.info("Requête de consultation des noms vernaculaires pour la plante ID {}", planteId);
        return ResponseEntity.ok(nomVernaculaireService.getNomsByPlanteId(planteId));
    }

    @PostMapping("/plantes/{planteId}/noms-vernaculaires")
    @Operation(summary = "Ajouter un nom vernaculaire à une plante",
               description = "Enregistre un nom vernaculaire recueilli sur le terrain avec sa langue locale (Bambara, Peul, etc.) et le pays.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Nom vernaculaire enregistré avec succès"),
        @ApiResponse(responseCode = "400", description = "Données d'entrée invalides"),
        @ApiResponse(responseCode = "404", description = "Plante introuvable"),
        @ApiResponse(responseCode = "409", description = "Conflit : ce nom vernaculaire existe déjà dans cette langue pour cette plante")
    })
    public ResponseEntity<AgentNomVernaculaireResponse> addNomVernaculaire(
            @PathVariable Long planteId,
            @Valid @RequestBody AgentNomVernaculaireRequest request) {
        log.info("Requête d'ajout d'un nom vernaculaire pour la plante ID {} : '{}' ({})", planteId, request.getNom(), request.getLangue());
        AgentNomVernaculaireResponse response = nomVernaculaireService.addNomVernaculaire(planteId, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/plantes/{planteId}/noms-vernaculaires/{nomId}")
    @Operation(summary = "Modifier un nom vernaculaire",
               description = "Permet de rectifier l'orthographe, la langue ou le pays d'un nom vernaculaire existant.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Nom vernaculaire mis à jour avec succès"),
        @ApiResponse(responseCode = "400", description = "Données d'entrée invalides"),
        @ApiResponse(responseCode = "404", description = "Plante ou nom vernaculaire introuvable"),
        @ApiResponse(responseCode = "409", description = "Conflit : un doublon identique existe déjà")
    })
    public ResponseEntity<AgentNomVernaculaireResponse> updateNomVernaculaire(
            @PathVariable Long planteId,
            @PathVariable Long nomId,
            @Valid @RequestBody AgentNomVernaculaireRequest request) {
        log.info("Requête de mise à jour du nom vernaculaire ID {} pour la plante ID {}", nomId, planteId);
        return ResponseEntity.ok(nomVernaculaireService.updateNomVernaculaire(planteId, nomId, request));
    }

    @DeleteMapping("/plantes/{planteId}/noms-vernaculaires/{nomId}")
    @Operation(summary = "Supprimer un nom vernaculaire",
               description = "Retire un nom vernaculaire erroné associé à une plante.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Nom vernaculaire supprimé avec succès"),
        @ApiResponse(responseCode = "404", description = "Plante ou nom vernaculaire introuvable")
    })
    public ResponseEntity<Void> deleteNomVernaculaire(
            @PathVariable Long planteId,
            @PathVariable Long nomId) {
        log.info("Requête de suppression du nom vernaculaire ID {} pour la plante ID {}", nomId, planteId);
        nomVernaculaireService.deleteNomVernaculaire(planteId, nomId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/noms-vernaculaires/search")
    @Operation(summary = "Rechercher des noms vernaculaires",
               description = "Recherche les noms vernaculaires par terme et/ou par langue locale (ex: Bambara, Peul, etc.).")
    @ApiResponse(responseCode = "200", description = "Résultats de recherche")
    public ResponseEntity<List<AgentNomVernaculaireResponse>> searchNomsVernaculaires(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String langue) {
        log.info("Requête de recherche de noms vernaculaires (query='{}', langue='{}')", query, langue);
        return ResponseEntity.ok(nomVernaculaireService.searchNomsVernaculaires(query, langue));
    }
}
