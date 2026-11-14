package com.pharmacopee.ladafura.controllers.admin;

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

import com.pharmacopee.ladafura.dto.admin.abonnement.CreerAbonnementContratRequest;
import com.pharmacopee.ladafura.dto.admin.abonnement.TraiterDemandeContratRequest;
import com.pharmacopee.ladafura.dto.pharmacopee.abonnement.AbonnementResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.abonnement.DemandeContratResponse;
import com.pharmacopee.ladafura.enums.StatutDemandeContrat;
import com.pharmacopee.ladafura.services.interfaces.IAdminDemandeContratService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/admin/demandes-contrat")
@RequiredArgsConstructor
@Tag(name = "Admin - Demandes de contrat", description = "Traitement des demandes de contrat personnalisé")
@SecurityRequirement(name = "bearerAuth")
public class AdminDemandeContratController {

    private final IAdminDemandeContratService demandeService;

    @GetMapping
    @Operation(summary = "Lister les demandes", description = "Filtre facultatif par statut.")
    @ApiResponse(responseCode = "200", description = "Liste paginée")
    public ResponseEntity<Page<DemandeContratResponse>> getDemandes(@RequestParam(required = false) StatutDemandeContrat statut,
                                                                    @ParameterObject Pageable pageable) {
        return ResponseEntity.ok(demandeService.getDemandes(statut, pageable));
    }

    @PatchMapping("/{id}/traiter")
    @Operation(summary = "Changer le statut d'une demande")
    @ApiResponse(responseCode = "200", description = "Demande mise à jour")
    @ApiResponse(responseCode = "409", description = "Transition impossible")
    public ResponseEntity<DemandeContratResponse> traiter(@PathVariable Long id, @Valid @RequestBody TraiterDemandeContratRequest request) {
        return ResponseEntity.ok(demandeService.traiter(id, request));
    }

    @PostMapping("/{id}/abonnement")
    @Operation(summary = "Créer l'abonnement négocié", description = "Demande acceptée uniquement. L'abonnement attend ensuite son paiement.")
    @ApiResponse(responseCode = "201", description = "Abonnement créé")
    @ApiResponse(responseCode = "409", description = "Demande non acceptée ou abonnement déjà créé")
    public ResponseEntity<AbonnementResponse> creerAbonnement(@PathVariable Long id, @Valid @RequestBody CreerAbonnementContratRequest request) {
        return new ResponseEntity<>(demandeService.creerAbonnement(id, request), HttpStatus.CREATED);
    }
}
