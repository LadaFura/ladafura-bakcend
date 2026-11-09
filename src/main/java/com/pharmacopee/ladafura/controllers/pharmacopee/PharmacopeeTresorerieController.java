package com.pharmacopee.ladafura.controllers.pharmacopee;

import java.time.LocalDate;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pharmacopee.ladafura.dto.pharmacopee.tresorerie.SyntheseTresorerieResponse;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeTresorerieService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/pharmacopee/tresorerie")
@RequiredArgsConstructor
@Tag(name = "Pharmacopée - Trésorerie", description = "Synthèse par période ; ce n'est pas une comptabilité générale")
@SecurityRequirement(name = "bearerAuth")
public class PharmacopeeTresorerieController {

    private final IPharmacopeeTresorerieService tresorerieService;

    @GetMapping("/synthese")
    @Operation(summary = "Synthèse de trésorerie", description = "Permission COMPTABILITE_CONSULTER. Chiffre d'affaires et encaissements ne s'additionnent pas.")
    @ApiResponse(responseCode = "200", description = "Synthèse")
    @ApiResponse(responseCode = "400", description = "Période invalide")
    public ResponseEntity<SyntheseTresorerieResponse> getSynthese(@RequestParam LocalDate debut, @RequestParam LocalDate fin) {
        return ResponseEntity.ok(tresorerieService.getSynthese(debut, fin));
    }
}
