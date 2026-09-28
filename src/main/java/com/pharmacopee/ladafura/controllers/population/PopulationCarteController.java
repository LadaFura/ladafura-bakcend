package com.pharmacopee.ladafura.controllers.population;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pharmacopee.ladafura.dto.population.cartographie.PopulationCarteDetailPharmacopeeResponse;
import com.pharmacopee.ladafura.dto.population.cartographie.PopulationCartePharmacopeeItem;
import com.pharmacopee.ladafura.dto.population.cartographie.PopulationCarteProduitItem;
import com.pharmacopee.ladafura.services.interfaces.IPopulationCarteService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/population/carte")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Population - Cartographie et Géolocalisation", description = "Endpoints cartographiques pour afficher les officines agréées, localiser les stocks de produits sur carte et calculer les distances (sans suivi GPS livreur)")
public class PopulationCarteController {

    private final IPopulationCarteService carteService;

    @GetMapping("/pharmacopees")
    @Operation(summary = "Afficher les officines de pharmacopée sur la carte",
               description = "Retourne la liste des marqueurs géographiques des officines agréées disposant de coordonnées GPS. Permet un filtrage administratif ou par proximité (rayon kilométrique autour d'une position GPS).")
    @ApiResponse(responseCode = "200", description = "Liste des marqueurs cartographiques")
    public ResponseEntity<List<PopulationCartePharmacopeeItem>> getPharmacopeesSurCarte(
            @Parameter(description = "Nom ou mot-clé de recherche", example = "Mandé")
            @RequestParam(required = false) String query,
            @Parameter(description = "Région administrative", example = "Koulikoro")
            @RequestParam(required = false) String region,
            @Parameter(description = "Cercle administratif", example = "Kati")
            @RequestParam(required = false) String cercle,
            @Parameter(description = "Commune", example = "Siby")
            @RequestParam(required = false) String commune,
            @Parameter(description = "Latitude GPS de l'utilisateur", example = "12.6392")
            @RequestParam(required = false) Double lat,
            @Parameter(description = "Longitude GPS de l'utilisateur", example = "-8.0029")
            @RequestParam(required = false) Double lng,
            @Parameter(description = "Rayon de recherche maximal en kilomètres", example = "25.0")
            @RequestParam(required = false) Double rayonKm) {

        log.info("Requête GET /api/v1/population/carte/pharmacopees reçue (query='{}', region='{}', lat={}, lng={}, rayonKm={})",
                query, region, lat, lng, rayonKm);
        return ResponseEntity.ok(carteService.getPharmacopeesSurCarte(query, region, cercle, commune, lat, lng, rayonKm));
    }

    @GetMapping("/produit")
    @Operation(summary = "Localiser les officines proposant un produit sur la carte",
               description = "Affiche sur la carte toutes les officines proposant un produit donné en stock avec leur prix et coordonnées GPS, triées par proximité.")
    @ApiResponse(responseCode = "200", description = "Liste des officines localisées avec le produit")
    public ResponseEntity<List<PopulationCarteProduitItem>> localiserProduitSurCarte(
            @Parameter(description = "Identifiant du produit", example = "5")
            @RequestParam(required = false) Long produitId,
            @Parameter(description = "Nom ou mot-clé sur le produit", example = "tisane")
            @RequestParam(required = false) String query,
            @Parameter(description = "Latitude GPS de l'utilisateur", example = "12.6392")
            @RequestParam(required = false) Double lat,
            @Parameter(description = "Longitude GPS de l'utilisateur", example = "-8.0029")
            @RequestParam(required = false) Double lng,
            @Parameter(description = "Rayon de recherche en kilomètres", example = "30.0")
            @RequestParam(required = false) Double rayonKm,
            @Parameter(description = "Filtrer uniquement si en stock disponible", example = "true")
            @RequestParam(required = false, defaultValue = "true") Boolean disponibleOnly) {

        log.info("Requête GET /api/v1/population/carte/produit reçue (produitId={}, query='{}', lat={}, lng={}, rayonKm={})",
                produitId, query, lat, lng, rayonKm);
        return ResponseEntity.ok(carteService.localiserProduitSurCarte(produitId, query, lat, lng, rayonKm, disponibleOnly));
    }

    @GetMapping("/pharmacopees/{id}")
    @Operation(summary = "Consulter le détail cartographique d'une pharmacopée",
               description = "Fournit la fiche cartographique détaillée d'une officine avec calcul de distance estimée par rapport à l'utilisateur.")
    @ApiResponse(responseCode = "200", description = "Détail cartographique de la pharmacopée")
    @ApiResponse(responseCode = "404", description = "Pharmacopée introuvable ou non agréée")
    public ResponseEntity<PopulationCarteDetailPharmacopeeResponse> getPharmacopeeCarteDetail(
            @Parameter(description = "Identifiant de la pharmacopée", example = "3")
            @PathVariable Long id,
            @Parameter(description = "Latitude GPS de l'utilisateur", example = "12.6392")
            @RequestParam(required = false) Double lat,
            @Parameter(description = "Longitude GPS de l'utilisateur", example = "-8.0029")
            @RequestParam(required = false) Double lng) {

        log.info("Requête GET /api/v1/population/carte/pharmacopees/{} reçue (lat={}, lng={})", id, lat, lng);
        return ResponseEntity.ok(carteService.getPharmacopeeCarteDetail(id, lat, lng));
    }
}
