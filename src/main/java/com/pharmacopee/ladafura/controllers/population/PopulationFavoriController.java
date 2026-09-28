package com.pharmacopee.ladafura.controllers.population;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pharmacopee.ladafura.dto.population.favori.PopulationFavoriCheckResponse;
import com.pharmacopee.ladafura.dto.population.favori.PopulationFavoriCountResponse;
import com.pharmacopee.ladafura.dto.population.favori.PopulationFavoriItemResponse;
import com.pharmacopee.ladafura.dto.population.favori.PopulationToggleFavoriRequest;
import com.pharmacopee.ladafura.dto.population.favori.PopulationToggleFavoriResponse;
import com.pharmacopee.ladafura.dto.population.pharmacopee.PopulationPharmacopeeSummaryResponse;
import com.pharmacopee.ladafura.dto.population.plante.PopulationPlanteSummaryResponse;
import com.pharmacopee.ladafura.dto.population.produit.PopulationProduitSummaryResponse;
import com.pharmacopee.ladafura.enums.TypeFavori;
import com.pharmacopee.ladafura.services.interfaces.IPopulationFavoriService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/population/favoris")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasAuthority('ROLE_POPULATION')")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Population - Favoris", description = "Endpoints de gestion des favoris (plantes médicinales, produits et officines de pharmacopée)")
public class PopulationFavoriController {

    private final IPopulationFavoriService populationFavoriService;

    @PostMapping("/toggle")
    @Operation(summary = "Basculer l'état favori (ajouter ou retirer)",
               description = "Bouton d'action rapide : ajoute l'élément en favori s'il n'y est pas, ou le retire s'il est déjà présent.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "État du favori basculé avec succès"),
        @ApiResponse(responseCode = "400", description = "Données invalides"),
        @ApiResponse(responseCode = "401", description = "Non authentifié"),
        @ApiResponse(responseCode = "404", description = "Élément cible introuvable")
    })
    public ResponseEntity<PopulationToggleFavoriResponse> toggleFavori(@Valid @RequestBody PopulationToggleFavoriRequest request) {
        log.info("Requête POST /api/v1/population/favoris/toggle reçue (type={}, cibleId={})", request.getType(), request.getCibleId());
        return ResponseEntity.ok(populationFavoriService.toggleFavori(request));
    }

    @PostMapping("/plantes/{planteId}")
    @Operation(summary = "Ajouter une plante aux favoris", description = "Ajoute explicitement une plante médicinale aux favoris.")
    public ResponseEntity<PopulationToggleFavoriResponse> ajouterPlanteFavori(@PathVariable Long planteId) {
        log.info("Requête POST /api/v1/population/favoris/plantes/{}", planteId);
        return ResponseEntity.ok(populationFavoriService.ajouterPlanteFavori(planteId));
    }

    @DeleteMapping("/plantes/{planteId}")
    @Operation(summary = "Retirer une plante des favoris", description = "Retire explicitement une plante médicinale des favoris.")
    public ResponseEntity<PopulationToggleFavoriResponse> supprimerPlanteFavori(@PathVariable Long planteId) {
        log.info("Requête DELETE /api/v1/population/favoris/plantes/{}", planteId);
        return ResponseEntity.ok(populationFavoriService.supprimerPlanteFavori(planteId));
    }

    @PostMapping("/produits/{produitId}")
    @Operation(summary = "Ajouter un produit aux favoris", description = "Ajoute explicitement un produit de pharmacopée aux favoris.")
    public ResponseEntity<PopulationToggleFavoriResponse> ajouterProduitFavori(@PathVariable Long produitId) {
        log.info("Requête POST /api/v1/population/favoris/produits/{}", produitId);
        return ResponseEntity.ok(populationFavoriService.ajouterProduitFavori(produitId));
    }

    @DeleteMapping("/produits/{produitId}")
    @Operation(summary = "Retirer un produit des favoris", description = "Retire explicitement un produit de pharmacopée des favoris.")
    public ResponseEntity<PopulationToggleFavoriResponse> supprimerProduitFavori(@PathVariable Long produitId) {
        log.info("Requête DELETE /api/v1/population/favoris/produits/{}", produitId);
        return ResponseEntity.ok(populationFavoriService.supprimerProduitFavori(produitId));
    }

    @PostMapping("/pharmacopees/{pharmacopeeId}")
    @Operation(summary = "Ajouter une pharmacopée aux favoris", description = "Ajoute explicitement une officine de pharmacopée aux favoris.")
    public ResponseEntity<PopulationToggleFavoriResponse> ajouterPharmacopeeFavori(@PathVariable Long pharmacopeeId) {
        log.info("Requête POST /api/v1/population/favoris/pharmacopees/{}", pharmacopeeId);
        return ResponseEntity.ok(populationFavoriService.ajouterPharmacopeeFavori(pharmacopeeId));
    }

    @DeleteMapping("/pharmacopees/{pharmacopeeId}")
    @Operation(summary = "Retirer une pharmacopée des favoris", description = "Retire explicitement une officine de pharmacopée des favoris.")
    public ResponseEntity<PopulationToggleFavoriResponse> supprimerPharmacopeeFavori(@PathVariable Long pharmacopeeId) {
        log.info("Requête DELETE /api/v1/population/favoris/pharmacopees/{}", pharmacopeeId);
        return ResponseEntity.ok(populationFavoriService.supprimerPharmacopeeFavori(pharmacopeeId));
    }

    @DeleteMapping("/{favoriId}")
    @Operation(summary = "Supprimer un favori par son ID", description = "Supprime un enregistrement favori précis appartenant au client connecté.")
    public ResponseEntity<Void> supprimerFavoriById(@PathVariable Long favoriId) {
        log.info("Requête DELETE /api/v1/population/favoris/{}", favoriId);
        populationFavoriService.supprimerFavoriById(favoriId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/check")
    @Operation(summary = "Vérifier si un élément est en favori",
               description = "Permet à l'application mobile/web d'afficher l'état de l'icône coeur (activée ou désactivée).")
    public ResponseEntity<PopulationFavoriCheckResponse> checkFavori(
            @RequestParam TypeFavori type,
            @RequestParam Long cibleId) {
        log.info("Requête GET /api/v1/population/favoris/check (type={}, cibleId={})", type, cibleId);
        return ResponseEntity.ok(populationFavoriService.checkFavori(type, cibleId));
    }

    @GetMapping("/count")
    @Operation(summary = "Obtenir les compteurs de favoris",
               description = "Retourne le nombre total de favoris ainsi que la répartition par plante, produit et pharmacopée.")
    public ResponseEntity<PopulationFavoriCountResponse> getFavoriCounts() {
        log.info("Requête GET /api/v1/population/favoris/count");
        return ResponseEntity.ok(populationFavoriService.getFavoriCounts());
    }

    @GetMapping
    @Operation(summary = "Lister ses favoris avec pagination et filtre par type",
               description = "Retourne la liste complète ou filtrée (PLANTE, PRODUIT, PHARMACOPEE) des favoris avec pagination.")
    public ResponseEntity<Page<PopulationFavoriItemResponse>> getMesFavoris(
            @RequestParam(required = false) TypeFavori type,
            @ParameterObject Pageable pageable) {
        log.info("Requête GET /api/v1/population/favoris (type={})", type);
        return ResponseEntity.ok(populationFavoriService.getMesFavoris(type, pageable));
    }

    @GetMapping("/plantes")
    @Operation(summary = "Lister ses plantes favorites (résumé paginé)",
               description = "Retourne directement les fiches résumées des plantes médicinales favorites.")
    public ResponseEntity<Page<PopulationPlanteSummaryResponse>> getMesPlantesFavorites(@ParameterObject Pageable pageable) {
        log.info("Requête GET /api/v1/population/favoris/plantes");
        return ResponseEntity.ok(populationFavoriService.getMesPlantesFavorites(pageable));
    }

    @GetMapping("/produits")
    @Operation(summary = "Lister ses produits favoris (résumé paginé)",
               description = "Retourne directement les fiches résumées des produits favoris avec disponibilité et note.")
    public ResponseEntity<Page<PopulationProduitSummaryResponse>> getMesProduitsFavoris(@ParameterObject Pageable pageable) {
        log.info("Requête GET /api/v1/population/favoris/produits");
        return ResponseEntity.ok(populationFavoriService.getMesProduitsFavoris(pageable));
    }

    @GetMapping("/pharmacopees")
    @Operation(summary = "Lister ses pharmacopées favorites (résumé paginé)",
               description = "Retourne directement les fiches résumées des officines de pharmacopée favorites.")
    public ResponseEntity<Page<PopulationPharmacopeeSummaryResponse>> getMesPharmacopeesFavorites(@ParameterObject Pageable pageable) {
        log.info("Requête GET /api/v1/population/favoris/pharmacopees");
        return ResponseEntity.ok(populationFavoriService.getMesPharmacopeesFavorites(pageable));
    }
}
