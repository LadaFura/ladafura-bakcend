package com.pharmacopee.ladafura.controllers.pharmacopee;

import java.time.LocalDate;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pharmacopee.ladafura.dto.pharmacopee.abonnement.MotifRequest;
import com.pharmacopee.ladafura.dto.pharmacopee.depense.DepenseResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.depense.EnregistrerDepenseRequest;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeDepenseService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/pharmacopee/depenses")
@RequiredArgsConstructor
@Tag(name = "Pharmacopée - Dépenses", description = "Dépenses d'exploitation, payées depuis une caisse ou un compte")
@SecurityRequirement(name = "bearerAuth")
public class PharmacopeeDepenseController {

    private final IPharmacopeeDepenseService depenseService;

    @GetMapping
    @Operation(summary = "Lister les dépenses", description = "Permission DEPENSES_CONSULTER. Période facultative.")
    @ApiResponse(responseCode = "200", description = "Liste paginée")
    public ResponseEntity<Page<DepenseResponse>> getDepenses(@RequestParam(required = false) LocalDate debut,
                                                             @RequestParam(required = false) LocalDate fin,
                                                             @ParameterObject Pageable pageable) {
        return ResponseEntity.ok(depenseService.getDepenses(debut, fin, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulter une dépense", description = "Permission DEPENSES_CONSULTER.")
    @ApiResponse(responseCode = "200", description = "Dépense")
    @ApiResponse(responseCode = "404", description = "Dépense introuvable dans cette pharmacopée")
    public ResponseEntity<DepenseResponse> getDepense(@PathVariable Long id) {
        return ResponseEntity.ok(depenseService.getDepense(id));
    }

    @PostMapping
    @Operation(summary = "Enregistrer une dépense", description = "Permission DEPENSES_GERER. Crée la sortie de caisse correspondante.")
    @ApiResponse(responseCode = "201", description = "Dépense enregistrée")
    @ApiResponse(responseCode = "409", description = "Session fermée ou solde insuffisant")
    public ResponseEntity<DepenseResponse> creer(@Valid @RequestBody EnregistrerDepenseRequest request) {
        return new ResponseEntity<>(depenseService.creer(request), HttpStatus.CREATED);
    }

    @PatchMapping("/{id}/annuler")
    @Operation(summary = "Annuler une dépense", description = "Permission DEPENSES_GERER. Contre-passation en caisse.")
    @ApiResponse(responseCode = "200", description = "Dépense annulée")
    @ApiResponse(responseCode = "409", description = "Dépense déjà annulée")
    public ResponseEntity<DepenseResponse> annuler(@PathVariable Long id, @Valid @RequestBody MotifRequest request) {
        return ResponseEntity.ok(depenseService.annuler(id, request.getMotif()));
    }
}
