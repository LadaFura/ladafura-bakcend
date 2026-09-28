package com.pharmacopee.ladafura.controllers.population;

import java.util.List;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pharmacopee.ladafura.dto.population.plante.PopulationConnaissanceTraditionnelleDto;
import com.pharmacopee.ladafura.dto.population.plante.PopulationEtudeScientifiqueDto;
import com.pharmacopee.ladafura.dto.population.plante.PopulationPlanteDetailResponse;
import com.pharmacopee.ladafura.dto.population.plante.PopulationPlanteSummaryResponse;
import com.pharmacopee.ladafura.services.interfaces.IPopulationPlanteService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/population/plantes")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Population - Consultation des Plantes", description = "Endpoints de consultation du catalogue des plantes médicinales, de leurs savoirs traditionnels et de leurs études scientifiques")
public class PopulationPlanteController {

    private final IPopulationPlanteService planteService;

    @GetMapping
    @Operation(summary = "Lister les plantes médicinales validées",
               description = "Retourne la liste paginée des plantes médicinales validées de la flore malienne avec leurs noms vernaculaires et aperçus.")
    @ApiResponse(responseCode = "200", description = "Liste paginée des plantes")
    public ResponseEntity<Page<PopulationPlanteSummaryResponse>> listerPlantes(
            @ParameterObject Pageable pageable) {
        log.info("Requête GET /api/v1/population/plantes reçue (page={}, size={})",
                pageable.getPageNumber(), pageable.getPageSize());
        return ResponseEntity.ok(planteService.listerPlantes(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulter la fiche détaillée d'une plante médicinale",
               description = "Fournit la fiche complète d'une plante validée incluant nom scientifique, noms vernaculaires, savoirs traditionnels validés, études scientifiques, maladies, médias et localités répertoriées, accompagnée d'un avertissement médical strict.")
    @ApiResponse(responseCode = "200", description = "Fiche détaillée de la plante")
    @ApiResponse(responseCode = "404", description = "Plante introuvable ou non validée")
    public ResponseEntity<PopulationPlanteDetailResponse> getPlanteDetail(
            @Parameter(description = "Identifiant unique de la plante", example = "1")
            @PathVariable Long id) {
        log.info("Requête GET /api/v1/population/plantes/{} reçue", id);
        return ResponseEntity.ok(planteService.getPlanteDetail(id));
    }

    @GetMapping("/{id}/connaissances")
    @Operation(summary = "Consulter les connaissances traditionnelles validées d'une plante",
               description = "Retourne uniquement la liste des usages et connaissances traditionnelles validés associés à la plante (séparés des études scientifiques).")
    @ApiResponse(responseCode = "200", description = "Liste des connaissances traditionnelles validées")
    @ApiResponse(responseCode = "404", description = "Plante introuvable ou non validée")
    public ResponseEntity<List<PopulationConnaissanceTraditionnelleDto>> getConnaissancesByPlante(
            @Parameter(description = "Identifiant de la plante", example = "1")
            @PathVariable Long id) {
        log.info("Requête GET /api/v1/population/plantes/{}/connaissances reçue", id);
        return ResponseEntity.ok(planteService.getConnaissancesByPlante(id));
    }

    @GetMapping("/{id}/etudes")
    @Operation(summary = "Consulter les études scientifiques disponibles sur une plante",
               description = "Retourne la liste des études scientifiques universitaires ou cliniques documentées sur la plante.")
    @ApiResponse(responseCode = "200", description = "Liste des études scientifiques")
    @ApiResponse(responseCode = "404", description = "Plante introuvable ou non validée")
    public ResponseEntity<List<PopulationEtudeScientifiqueDto>> getEtudesByPlante(
            @Parameter(description = "Identifiant de la plante", example = "1")
            @PathVariable Long id) {
        log.info("Requête GET /api/v1/population/plantes/{}/etudes reçue", id);
        return ResponseEntity.ok(planteService.getEtudesByPlante(id));
    }
}
