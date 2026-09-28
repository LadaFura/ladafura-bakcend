package com.pharmacopee.ladafura.controllers.population;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pharmacopee.ladafura.dto.population.recherche.PopulationGlobalSearchResponse;
import com.pharmacopee.ladafura.dto.population.recherche.PopulationMaladieSearchItem;
import com.pharmacopee.ladafura.dto.population.recherche.PopulationPharmacopeeSearchItem;
import com.pharmacopee.ladafura.dto.population.recherche.PopulationPlanteSearchItem;
import com.pharmacopee.ladafura.dto.population.recherche.PopulationProduitSearchItem;
import com.pharmacopee.ladafura.dto.population.recherche.PopulationVernaculaireSearchItem;
import com.pharmacopee.ladafura.services.interfaces.IPopulationRechercheService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/population/recherche")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Population - Moteur de Recherche", description = "Endpoints de recherche universelle et multicritères sur les plantes, noms vernaculaires, maladies, produits et pharmacopées")
public class PopulationRechercheController {

    private final IPopulationRechercheService rechercheService;

    @GetMapping
    @Operation(summary = "Recherche simple et unifiée globale (barre de recherche universelle)",
               description = "Permet de rechercher un mot-clé (ex: 'kinkeliba', 'paludisme', 'sirop', 'Bamako') et de recevoir un résultat consolidé contenant plantes, noms vernaculaires, maladies, produits et pharmacopées.")
    @ApiResponse(responseCode = "200", description = "Résultats de recherche consolidés")
    public ResponseEntity<PopulationGlobalSearchResponse> rechercheGlobale(
            @Parameter(description = "Terme ou expression recherchée", example = "kinkeliba")
            @RequestParam(required = false) String query) {
        log.info("Requête GET /api/v1/population/recherche reçue (query='{}')", query);
        return ResponseEntity.ok(rechercheService.rechercheGlobale(query));
    }

    @GetMapping("/plantes")
    @Operation(summary = "Recherche paginée de plantes médicinales validées",
               description = "Recherche multicritère par mot-clé (nom scientifique ou description) avec pagination.")
    @ApiResponse(responseCode = "200", description = "Page de plantes médicinales correspondantes")
    public ResponseEntity<Page<PopulationPlanteSearchItem>> rechercherPlantes(
            @Parameter(description = "Mot-clé sur le nom scientifique ou la description", example = "Combretum")
            @RequestParam(required = false) String query,
            @ParameterObject Pageable pageable) {
        log.info("Requête GET /api/v1/population/recherche/plantes reçue (query='{}')", query);
        return ResponseEntity.ok(rechercheService.rechercherPlantes(query, pageable));
    }

    @GetMapping("/vernaculaires")
    @Operation(summary = "Recherche paginée multicritère de noms vernaculaires",
               description = "Recherche par nom vernaculaire local et/ou par langue malienne (Bambara, Peul, etc.).")
    @ApiResponse(responseCode = "200", description = "Page de noms vernaculaires correspondants")
    public ResponseEntity<Page<PopulationVernaculaireSearchItem>> rechercherNomsVernaculaires(
            @Parameter(description = "Nom vernaculaire recherché", example = "Kinkéliba")
            @RequestParam(required = false) String nom,
            @Parameter(description = "Langue locale malienne", example = "Bambara")
            @RequestParam(required = false) String langue,
            @ParameterObject Pageable pageable) {
        log.info("Requête GET /api/v1/population/recherche/vernaculaires reçue (nom='{}', langue='{}')", nom, langue);
        return ResponseEntity.ok(rechercheService.rechercherNomsVernaculaires(nom, langue, pageable));
    }

    @GetMapping("/maladies")
    @Operation(summary = "Recherche paginée de maladies documentées",
               description = "Recherche par nom ou symptôme de maladie avec pagination.")
    @ApiResponse(responseCode = "200", description = "Page de maladies correspondantes")
    public ResponseEntity<Page<PopulationMaladieSearchItem>> rechercherMaladies(
            @Parameter(description = "Nom ou mot-clé de la maladie", example = "Paludisme")
            @RequestParam(required = false) String query,
            @ParameterObject Pageable pageable) {
        log.info("Requête GET /api/v1/population/recherche/maladies reçue (query='{}')", query);
        return ResponseEntity.ok(rechercheService.rechercherMaladies(query, pageable));
    }

    @GetMapping("/produits")
    @Operation(summary = "Recherche paginée multicritère de produits de pharmacopée",
               description = "Recherche multicritère par mot-clé, catégorie et/ou budget plafond (prixMax).")
    @ApiResponse(responseCode = "200", description = "Page de produits correspondants")
    public ResponseEntity<Page<PopulationProduitSearchItem>> rechercherProduits(
            @Parameter(description = "Mot-clé sur le nom ou description du produit", example = "Tisane")
            @RequestParam(required = false) String query,
            @Parameter(description = "Identifiant de la catégorie de produit")
            @RequestParam(required = false) Long categorieId,
            @Parameter(description = "Prix maximum en FCFA", example = "5000")
            @RequestParam(required = false) Double prixMax,
            @ParameterObject Pageable pageable) {
        log.info("Requête GET /api/v1/population/recherche/produits reçue (query='{}', categorieId={}, prixMax={})",
                query, categorieId, prixMax);
        return ResponseEntity.ok(rechercheService.rechercherProduits(query, categorieId, prixMax, pageable));
    }

    @GetMapping("/pharmacopees")
    @Operation(summary = "Recherche paginée multicritère d'officines de pharmacopée",
               description = "Recherche multicritère par nom d'officine et filtres géographiques (région, cercle, commune).")
    @ApiResponse(responseCode = "200", description = "Page d'officines correspondantes")
    public ResponseEntity<Page<PopulationPharmacopeeSearchItem>> rechercherPharmacopees(
            @Parameter(description = "Nom de la pharmacopée ou mot-clé", example = "Mandé")
            @RequestParam(required = false) String query,
            @Parameter(description = "Région administrative", example = "Koulikoro")
            @RequestParam(required = false) String region,
            @Parameter(description = "Cercle administratif", example = "Kati")
            @RequestParam(required = false) String cercle,
            @Parameter(description = "Commune", example = "Siby")
            @RequestParam(required = false) String commune,
            @ParameterObject Pageable pageable) {
        log.info("Requête GET /api/v1/population/recherche/pharmacopees reçue (query='{}', region='{}', cercle='{}', commune='{}')",
                query, region, cercle, commune);
        return ResponseEntity.ok(rechercheService.rechercherPharmacopees(query, region, cercle, commune, pageable));
    }
}
