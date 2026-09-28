package com.pharmacopee.ladafura.controllers.population;

import java.util.List;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pharmacopee.ladafura.dto.population.produit.PopulationOffrePharmacopeeDto;
import com.pharmacopee.ladafura.dto.population.produit.PopulationProduitDetailResponse;
import com.pharmacopee.ladafura.dto.population.produit.PopulationProduitSummaryResponse;
import com.pharmacopee.ladafura.services.interfaces.IPopulationProduitService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/population/produits")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Population - Consultation des Produits", description = "Endpoints de consultation du catalogue des produits traditionnels validés, compositions botaniques, offres des officines et disponibilités")
public class PopulationProduitController {

    private final IPopulationProduitService produitService;

    @GetMapping
    @Operation(summary = "Consulter le catalogue des produits validés",
               description = "Retourne la liste paginée des produits validés avec filtres optionnels par mot-clé, catégorie et prix plafond.")
    @ApiResponse(responseCode = "200", description = "Page de produits correspondants")
    public ResponseEntity<Page<PopulationProduitSummaryResponse>> listerProduits(
            @Parameter(description = "Mot-clé sur le nom ou la description du produit", example = "tisane")
            @RequestParam(required = false) String query,
            @Parameter(description = "Identifiant de la catégorie de produit", example = "1")
            @RequestParam(required = false) Long categorieId,
            @Parameter(description = "Prix maximum en FCFA", example = "5000")
            @RequestParam(required = false) Double prixMax,
            @ParameterObject Pageable pageable) {

        log.info("Requête GET /api/v1/population/produits reçue (query='{}', categorieId={}, prixMax={})",
                query, categorieId, prixMax);
        return ResponseEntity.ok(produitService.listerProduits(query, categorieId, prixMax, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulter la fiche détaillée d'un produit traditionnel",
               description = "Fournit la fiche complète du produit validé avec sa description, forme, composition, plantes et maladies associées, et l'ensemble des offres des pharmacopées agréées (prix, disponibilité, modes de retrait).")
    @ApiResponse(responseCode = "200", description = "Fiche détaillée du produit")
    @ApiResponse(responseCode = "404", description = "Produit introuvable ou non validé")
    public ResponseEntity<PopulationProduitDetailResponse> getProduitDetail(
            @Parameter(description = "Identifiant unique du produit", example = "5")
            @PathVariable Long id) {

        log.info("Requête GET /api/v1/population/produits/{} reçue", id);
        return ResponseEntity.ok(produitService.getProduitDetail(id));
    }

    @GetMapping("/{id}/offres")
    @Operation(summary = "Consulter les offres et disponibilités des pharmacopées pour un produit",
               description = "Retourne la liste des officines agréées qui proposent ce produit avec leur prix affiché, quantité en stock, géolocalisation et modes de retrait (Livraison / Pickup).")
    @ApiResponse(responseCode = "200", description = "Liste des offres des officines")
    @ApiResponse(responseCode = "404", description = "Produit introuvable ou non validé")
    public ResponseEntity<List<PopulationOffrePharmacopeeDto>> getOffresByProduit(
            @Parameter(description = "Identifiant du produit", example = "5")
            @PathVariable Long id) {

        log.info("Requête GET /api/v1/population/produits/{}/offres reçue", id);
        return ResponseEntity.ok(produitService.getOffresByProduit(id));
    }
}
