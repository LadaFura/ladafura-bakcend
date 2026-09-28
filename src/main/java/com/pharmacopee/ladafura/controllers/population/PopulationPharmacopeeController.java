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

import com.pharmacopee.ladafura.dto.population.pharmacopee.PopulationPharmacopeeDetailResponse;
import com.pharmacopee.ladafura.dto.population.pharmacopee.PopulationPharmacopeeModeRetraitDto;
import com.pharmacopee.ladafura.dto.population.pharmacopee.PopulationPharmacopeeProduitItemResponse;
import com.pharmacopee.ladafura.dto.population.pharmacopee.PopulationPharmacopeeSummaryResponse;
import com.pharmacopee.ladafura.services.interfaces.IPopulationPharmacopeeService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/population/pharmacopees")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Population - Consultation des Pharmacopées", description = "Endpoints de découverte des officines de pharmacopée agréées, de leurs profils, géolocalisations, produits et modes de retrait")
public class PopulationPharmacopeeController {

    private final IPopulationPharmacopeeService pharmacopeeService;

    @GetMapping
    @Operation(summary = "Rechercher et lister les officines de pharmacopée agréées",
               description = "Retourne la liste paginée des officines agréées avec filtres optionnels sur le nom et la géographie (région, cercle, commune).")
    @ApiResponse(responseCode = "200", description = "Page d'officines correspondantes")
    public ResponseEntity<Page<PopulationPharmacopeeSummaryResponse>> listerPharmacopees(
            @Parameter(description = "Nom de la pharmacopée ou mot-clé", example = "Mandé")
            @RequestParam(required = false) String query,
            @Parameter(description = "Région administrative", example = "Koulikoro")
            @RequestParam(required = false) String region,
            @Parameter(description = "Cercle administratif", example = "Kati")
            @RequestParam(required = false) String cercle,
            @Parameter(description = "Commune", example = "Siby")
            @RequestParam(required = false) String commune,
            @ParameterObject Pageable pageable) {

        log.info("Requête GET /api/v1/population/pharmacopees reçue (query='{}', region='{}', cercle='{}', commune='{}')",
                query, region, cercle, commune);
        return ResponseEntity.ok(pharmacopeeService.listerPharmacopees(query, region, cercle, commune, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulter le profil complet d'une officine de pharmacopée",
               description = "Fournit le profil détaillé de l'officine incluant ses coordonnées, sa localisation géographique, ses modes de retrait actifs et sa note moyenne.")
    @ApiResponse(responseCode = "200", description = "Fiche détaillée de la pharmacopée")
    @ApiResponse(responseCode = "404", description = "Pharmacopée introuvable ou non agréée")
    public ResponseEntity<PopulationPharmacopeeDetailResponse> getPharmacopeeDetail(
            @Parameter(description = "Identifiant de la pharmacopée", example = "3")
            @PathVariable Long id) {

        log.info("Requête GET /api/v1/population/pharmacopees/{} reçue", id);
        return ResponseEntity.ok(pharmacopeeService.getPharmacopeeDetail(id));
    }

    @GetMapping("/{id}/produits")
    @Operation(summary = "Consulter les produits proposés par une pharmacopée",
               description = "Retourne la liste paginée des produits validés disponibles dans cette officine avec prix et état des stocks.")
    @ApiResponse(responseCode = "200", description = "Page de produits proposés")
    @ApiResponse(responseCode = "404", description = "Pharmacopée introuvable ou non agréée")
    public ResponseEntity<Page<PopulationPharmacopeeProduitItemResponse>> getProduitsByPharmacopee(
            @Parameter(description = "Identifiant de la pharmacopée", example = "3")
            @PathVariable Long id,
            @Parameter(description = "Filtrer uniquement les produits en stock disponible", example = "true")
            @RequestParam(required = false) Boolean disponibleOnly,
            @ParameterObject Pageable pageable) {

        log.info("Requête GET /api/v1/population/pharmacopees/{}/produits reçue (disponibleOnly={})", id, disponibleOnly);
        return ResponseEntity.ok(pharmacopeeService.getProduitsByPharmacopee(id, disponibleOnly, pageable));
    }

    @GetMapping("/{id}/modes-retrait")
    @Operation(summary = "Consulter les modes de retrait proposés par une pharmacopée",
               description = "Retourne la liste des options de retrait (Livraison à domicile et/ou Retrait en officine / Pickup) avec frais associés.")
    @ApiResponse(responseCode = "200", description = "Liste des modes de retrait")
    @ApiResponse(responseCode = "404", description = "Pharmacopée introuvable ou non agréée")
    public ResponseEntity<List<PopulationPharmacopeeModeRetraitDto>> getModesRetraitByPharmacopee(
            @Parameter(description = "Identifiant de la pharmacopée", example = "3")
            @PathVariable Long id) {

        log.info("Requête GET /api/v1/population/pharmacopees/{}/modes-retrait reçue", id);
        return ResponseEntity.ok(pharmacopeeService.getModesRetraitByPharmacopee(id));
    }
}
