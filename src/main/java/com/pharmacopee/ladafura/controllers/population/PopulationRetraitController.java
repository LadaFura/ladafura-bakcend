package com.pharmacopee.ladafura.controllers.population;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pharmacopee.ladafura.dto.population.retrait.PopulationEstimationRetraitRequest;
import com.pharmacopee.ladafura.dto.population.retrait.PopulationEstimationRetraitResponse;
import com.pharmacopee.ladafura.dto.population.retrait.PopulationPharmacopeeRetraitOptionsResponse;
import com.pharmacopee.ladafura.services.interfaces.IPopulationRetraitService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/population/retrait")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Population - Livraison & Pickup", description = "Endpoints de consultation des options de mise à disposition, tarification et éligibilité")
public class PopulationRetraitController {

    private final IPopulationRetraitService populationRetraitService;

    @GetMapping("/pharmacopees/{pharmacopeeId}")
    @Operation(summary = "Consulter les modes de mise à disposition d'une pharmacopée",
               description = "Fournit la liste des options configurées par l'officine (Livraison à domicile et/ou Retrait en officine Pickup) avec les tarifs et adresses.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Options de retrait récupérées avec succès"),
        @ApiResponse(responseCode = "400", description = "Pharmacopée non agréée"),
        @ApiResponse(responseCode = "404", description = "Pharmacopée introuvable")
    })
    public ResponseEntity<PopulationPharmacopeeRetraitOptionsResponse> getOptionsRetraitPharmacopee(
            @PathVariable Long pharmacopeeId) {
        log.info("Requête GET /api/v1/population/retrait/pharmacopees/{} reçue", pharmacopeeId);
        return ResponseEntity.ok(populationRetraitService.getOptionsRetraitPharmacopee(pharmacopeeId));
    }

    @PostMapping("/estimer")
    @Operation(summary = "Vérifier l'éligibilité et estimer les frais de retrait/livraison",
               description = "Permet de vérifier avant la commande si le mode souhaité (LIVRAISON ou PICKUP) est actif auprès de l'officine et d'en connaître les frais applicables.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Estimation calculée avec succès"),
        @ApiResponse(responseCode = "400", description = "Requête invalide ou pharmacopée non agréée"),
        @ApiResponse(responseCode = "404", description = "Pharmacopée introuvable")
    })
    public ResponseEntity<PopulationEstimationRetraitResponse> estimerOptionRetrait(
            @Valid @RequestBody PopulationEstimationRetraitRequest request) {
        log.info("Requête POST /api/v1/population/retrait/estimer reçue pour pharmacopée ID: {} (type: {})",
                request.getPharmacopeeId(), request.getType());
        return ResponseEntity.ok(populationRetraitService.estimerOptionRetrait(request));
    }
}
