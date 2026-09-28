package com.pharmacopee.ladafura.controllers.population;

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

import com.pharmacopee.ladafura.dto.population.avis.PopulationAvisResponse;
import com.pharmacopee.ladafura.dto.population.avis.PopulationCreateAvisRequest;
import com.pharmacopee.ladafura.dto.population.avis.PopulationEligibiliteAvisResponse;
import com.pharmacopee.ladafura.dto.population.avis.PopulationUpdateAvisRequest;
import com.pharmacopee.ladafura.enums.StatutAvis;
import com.pharmacopee.ladafura.services.interfaces.IPopulationAvisService;

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
@RequestMapping("/api/v1/population/avis")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasAuthority('ROLE_POPULATION')")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Population - Avis", description = "Endpoints de dépôt, modification et consultation des avis clients vérifiés")
public class PopulationAvisController {

    private final IPopulationAvisService populationAvisService;

    @GetMapping("/eligibilite/{produitId}")
    @Operation(summary = "Vérifier son éligibilité pour noter un produit",
               description = "Vérifie si le client a effectivement commandé et réceptionné le produit (commande livrée ou retirée), et s'il a déjà laissé une évaluation.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Éligibilité vérifiée avec succès"),
        @ApiResponse(responseCode = "401", description = "Non authentifié"),
        @ApiResponse(responseCode = "403", description = "Accès refusé"),
        @ApiResponse(responseCode = "404", description = "Produit introuvable")
    })
    public ResponseEntity<PopulationEligibiliteAvisResponse> verifierEligibiliteAvis(@PathVariable Long produitId) {
        log.info("Requête GET /api/v1/population/avis/eligibilite/{} reçue", produitId);
        return ResponseEntity.ok(populationAvisService.verifierEligibiliteAvis(produitId));
    }

    @PostMapping
    @Operation(summary = "Déposer un avis et une note sur un produit acheté",
               description = "Permet au client de noter (1 à 5 étoiles) et commenter un produit reçu. L'avis est créé au statut EN_ATTENTE pour modération administrative.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Avis créé avec succès"),
        @ApiResponse(responseCode = "400", description = "Produit non acheté/reçu ou avis déjà existant"),
        @ApiResponse(responseCode = "401", description = "Non authentifié"),
        @ApiResponse(responseCode = "403", description = "Accès refusé"),
        @ApiResponse(responseCode = "404", description = "Produit introuvable")
    })
    public ResponseEntity<PopulationAvisResponse> creerAvis(@Valid @RequestBody PopulationCreateAvisRequest request) {
        log.info("Requête POST /api/v1/population/avis reçue pour le produit ID: {}", request.getProduitId());
        PopulationAvisResponse response = populationAvisService.creerAvis(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{avisId}")
    @Operation(summary = "Modifier son avis ou sa note",
               description = "Permet de corriger ou mettre à jour la note ou le commentaire d'un avis existant.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Avis modifié avec succès"),
        @ApiResponse(responseCode = "400", description = "Données d'évaluation invalides"),
        @ApiResponse(responseCode = "401", description = "Non authentifié"),
        @ApiResponse(responseCode = "403", description = "Accès refusé"),
        @ApiResponse(responseCode = "404", description = "Avis introuvable")
    })
    public ResponseEntity<PopulationAvisResponse> modifierAvis(
            @PathVariable Long avisId,
            @Valid @RequestBody PopulationUpdateAvisRequest request) {
        log.info("Requête PUT /api/v1/population/avis/{} reçue", avisId);
        return ResponseEntity.ok(populationAvisService.modifierAvis(avisId, request));
    }

    @DeleteMapping("/{avisId}")
    @Operation(summary = "Supprimer son avis",
               description = "Supprime définitivement un avis déposé par l'utilisateur.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Avis supprimé avec succès"),
        @ApiResponse(responseCode = "401", description = "Non authentifié"),
        @ApiResponse(responseCode = "403", description = "Accès refusé"),
        @ApiResponse(responseCode = "404", description = "Avis introuvable")
    })
    public ResponseEntity<Void> supprimerAvis(@PathVariable Long avisId) {
        log.info("Requête DELETE /api/v1/population/avis/{} reçue", avisId);
        populationAvisService.supprimerAvis(avisId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @Operation(summary = "Consulter la liste de ses avis déposés",
               description = "Retourne l'historique complet et paginé de tous les avis rédigés par l'utilisateur connecté.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Historique des avis récupéré avec succès"),
        @ApiResponse(responseCode = "401", description = "Non authentifié"),
        @ApiResponse(responseCode = "403", description = "Accès refusé")
    })
    public ResponseEntity<Page<PopulationAvisResponse>> getMesAvis(
            @Parameter(description = "Filtrer par statut de modération (EN_ATTENTE, PUBLIE, REJETE, MASQUE)")
            @RequestParam(required = false) StatutAvis statut,
            @PageableDefault(size = 10, sort = "dateAvis", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("Requête GET /api/v1/population/avis reçue (statut={})", statut);
        return ResponseEntity.ok(populationAvisService.getMesAvis(statut, pageable));
    }

    @GetMapping("/{avisId}")
    @Operation(summary = "Consulter le détail d'un de ses avis",
               description = "Récupère les informations complètes d'un avis client spécifique.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Détail de l'avis récupéré avec succès"),
        @ApiResponse(responseCode = "401", description = "Non authentifié"),
        @ApiResponse(responseCode = "403", description = "Accès refusé"),
        @ApiResponse(responseCode = "404", description = "Avis introuvable")
    })
    public ResponseEntity<PopulationAvisResponse> getAvisDetail(@PathVariable Long avisId) {
        log.info("Requête GET /api/v1/population/avis/{} reçue", avisId);
        return ResponseEntity.ok(populationAvisService.getAvisDetail(avisId));
    }
}
