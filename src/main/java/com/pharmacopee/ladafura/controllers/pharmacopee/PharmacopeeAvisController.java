package com.pharmacopee.ladafura.controllers.pharmacopee;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pharmacopee.ladafura.dto.pharmacopee.avis.PharmacopeeAvisItemResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.avis.PharmacopeeAvisSummaryResponse;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeAvisService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/pharmacopee/avis")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Pharmacopée - Avis Clients", description = "Endpoints de consultation des avis, notes et commentaires clients modérés et validés sur les produits de l'officine")
@SecurityRequirement(name = "bearerAuth")
public class PharmacopeeAvisController {

    private final IPharmacopeeAvisService avisService;

    @Operation(summary = "Consulter la liste paginée des avis publiés sur ses produits",
               description = "Renvoie l'ensemble des retours d'expérience et notes vérifiés par la modération sur les remèdes vendus par l'officine.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Liste des avis récupérée avec succès"),
            @ApiResponse(responseCode = "401", description = "Non authentifié"),
            @ApiResponse(responseCode = "403", description = "Accès refusé ou établissement non validé")
    })
    @GetMapping
    public ResponseEntity<Page<PharmacopeeAvisItemResponse>> getAvis(@ParameterObject Pageable pageable) {
        log.info("Requête GET /api/v1/pharmacopee/avis reçue");
        return ResponseEntity.ok(avisService.getAvis(pageable));
    }

    @Operation(summary = "Synthèse et statistiques de satisfaction globale",
               description = "Renvoie la note moyenne générale, le nombre total d'avis et la répartition par étoile (1 à 5 étoiles).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Statistiques de satisfaction calculées avec succès"),
            @ApiResponse(responseCode = "401", description = "Non authentifié"),
            @ApiResponse(responseCode = "403", description = "Accès refusé ou établissement non validé")
    })
    @GetMapping("/summary")
    public ResponseEntity<PharmacopeeAvisSummaryResponse> getSummary() {
        log.info("Requête GET /api/v1/pharmacopee/avis/summary reçue");
        return ResponseEntity.ok(avisService.getSummary());
    }

    // Endpoint produit/{produitId} supprimé car les avis sont désormais rattachés à la pharmacopée directement.

    @Operation(summary = "Consulter le détail d'un avis client",
               description = "Renvoie les détails complets d'une évaluation spécifique.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Détail de l'avis récupéré avec succès"),
            @ApiResponse(responseCode = "401", description = "Non authentifié"),
            @ApiResponse(responseCode = "403", description = "Accès refusé"),
            @ApiResponse(responseCode = "404", description = "Avis non trouvé ou ne concernant pas votre officine")
    })
    @GetMapping("/{id}")
    public ResponseEntity<PharmacopeeAvisItemResponse> getAvisDetail(
            @Parameter(description = "Identifiant de l'avis", example = "1")
            @PathVariable Long id) {
        log.info("Requête GET /api/v1/pharmacopee/avis/{} reçue", id);
        return ResponseEntity.ok(avisService.getAvisDetail(id));
    }

    @org.springframework.web.bind.annotation.PostMapping("/{id}/repondre")
    public ResponseEntity<PharmacopeeAvisItemResponse> repondreAvis(
            @PathVariable Long id,
            @org.springframework.web.bind.annotation.RequestBody ReponseAvisRequest request) {
        return ResponseEntity.ok(avisService.repondreAvis(id, request.getReponse()));
    }
}
class ReponseAvisRequest {
    private String reponse;
    public String getReponse() { return reponse; }
    public void setReponse(String reponse) { this.reponse = reponse; }
}
