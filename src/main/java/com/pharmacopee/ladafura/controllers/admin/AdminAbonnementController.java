package com.pharmacopee.ladafura.controllers.admin;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pharmacopee.ladafura.dto.pharmacopee.abonnement.AbonnementResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.abonnement.EvenementAbonnementResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.abonnement.FactureAbonnementResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.abonnement.MotifRequest;
import com.pharmacopee.ladafura.dto.pharmacopee.abonnement.PaiementAbonnementResponse;
import com.pharmacopee.ladafura.enums.StatutAbonnement;
import com.pharmacopee.ladafura.enums.StatutPaiement;
import com.pharmacopee.ladafura.services.interfaces.IAdminAbonnementService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Tag(name = "Admin - Abonnements", description = "Supervision des abonnements et confirmation des paiements")
@SecurityRequirement(name = "bearerAuth")
public class AdminAbonnementController {

    private final IAdminAbonnementService abonnementService;

    @GetMapping("/abonnements")
    @Operation(summary = "Lister les abonnements", description = "Filtres facultatifs par statut et par pharmacopée.")
    @ApiResponse(responseCode = "200", description = "Liste paginée")
    public ResponseEntity<Page<AbonnementResponse>> getAbonnements(@RequestParam(required = false) StatutAbonnement statut,
                                                                   @RequestParam(required = false) Long pharmacopeeId,
                                                                   @ParameterObject Pageable pageable) {
        return ResponseEntity.ok(abonnementService.getAbonnements(statut, pharmacopeeId, pageable));
    }

    @GetMapping("/abonnements/{id}")
    @Operation(summary = "Consulter un abonnement")
    @ApiResponse(responseCode = "200", description = "Abonnement et ses limites")
    @ApiResponse(responseCode = "404", description = "Abonnement introuvable")
    public ResponseEntity<AbonnementResponse> getAbonnement(@PathVariable Long id) {
        return ResponseEntity.ok(abonnementService.getAbonnement(id));
    }

    @GetMapping("/abonnements/{id}/historique")
    @Operation(summary = "Historique d'un abonnement")
    @ApiResponse(responseCode = "200", description = "Événements")
    public ResponseEntity<Page<EvenementAbonnementResponse>> getHistorique(@PathVariable Long id, @ParameterObject Pageable pageable) {
        return ResponseEntity.ok(abonnementService.getHistorique(id, pageable));
    }

    @PatchMapping("/abonnements/{id}/suspendre")
    @Operation(summary = "Suspendre un abonnement", description = "La pharmacopée passe en lecture seule.")
    @ApiResponse(responseCode = "200", description = "Abonnement suspendu")
    @ApiResponse(responseCode = "409", description = "Abonnement non en vigueur")
    public ResponseEntity<AbonnementResponse> suspendre(@PathVariable Long id, @Valid @RequestBody MotifRequest request) {
        return ResponseEntity.ok(abonnementService.suspendre(id, request.getMotif()));
    }

    @PatchMapping("/abonnements/{id}/reactiver")
    @Operation(summary = "Réactiver un abonnement suspendu")
    @ApiResponse(responseCode = "200", description = "Abonnement réactivé")
    @ApiResponse(responseCode = "409", description = "Abonnement non suspendu")
    public ResponseEntity<AbonnementResponse> reactiver(@PathVariable Long id, @Valid @RequestBody MotifRequest request) {
        return ResponseEntity.ok(abonnementService.reactiver(id, request.getMotif()));
    }

    @PatchMapping("/abonnements/{id}/annuler")
    @Operation(summary = "Annuler un abonnement", description = "S'il était en vigueur, la pharmacopée revient à l'offre gratuite.")
    @ApiResponse(responseCode = "200", description = "Abonnement annulé")
    @ApiResponse(responseCode = "409", description = "Abonnement terminé ou offre gratuite")
    public ResponseEntity<AbonnementResponse> annuler(@PathVariable Long id, @Valid @RequestBody MotifRequest request) {
        return ResponseEntity.ok(abonnementService.annuler(id, request.getMotif()));
    }

    @GetMapping("/paiements-abonnement")
    @Operation(summary = "Lister les paiements d'abonnement", description = "Filtre facultatif par statut.")
    @ApiResponse(responseCode = "200", description = "Liste paginée")
    public ResponseEntity<Page<PaiementAbonnementResponse>> getPaiements(@RequestParam(required = false) StatutPaiement statut,
                                                                         @ParameterObject Pageable pageable) {
        return ResponseEntity.ok(abonnementService.getPaiements(statut, pageable));
    }

    @PatchMapping("/paiements-abonnement/{id}/confirmer")
    @Operation(summary = "Confirmer un paiement", description = "Active l'abonnement, ou programme le renouvellement, et émet la facture.")
    @ApiResponse(responseCode = "200", description = "Facture émise")
    @ApiResponse(responseCode = "400", description = "Montant incorrect")
    @ApiResponse(responseCode = "409", description = "Paiement déjà traité ou référence déjà utilisée")
    public ResponseEntity<FactureAbonnementResponse> confirmerPaiement(@PathVariable Long id) {
        return ResponseEntity.ok(abonnementService.confirmerPaiement(id));
    }

    @PatchMapping("/paiements-abonnement/{id}/rejeter")
    @Operation(summary = "Rejeter un paiement")
    @ApiResponse(responseCode = "200", description = "Paiement rejeté")
    @ApiResponse(responseCode = "409", description = "Paiement déjà traité")
    public ResponseEntity<PaiementAbonnementResponse> rejeterPaiement(@PathVariable Long id, @Valid @RequestBody MotifRequest request) {
        return ResponseEntity.ok(abonnementService.rejeterPaiement(id, request.getMotif()));
    }
}
