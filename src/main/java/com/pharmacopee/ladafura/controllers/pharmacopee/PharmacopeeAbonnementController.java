package com.pharmacopee.ladafura.controllers.pharmacopee;

import java.util.List;

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
import org.springframework.web.bind.annotation.RestController;

import com.pharmacopee.ladafura.dto.pharmacopee.abonnement.AbonnementResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.abonnement.DeclarerPaiementRequest;
import com.pharmacopee.ladafura.dto.pharmacopee.abonnement.DemandeContratRequest;
import com.pharmacopee.ladafura.dto.pharmacopee.abonnement.DemandeContratResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.abonnement.FactureAbonnementResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.abonnement.MotifRequest;
import com.pharmacopee.ladafura.dto.pharmacopee.abonnement.OffreResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.abonnement.PaiementAbonnementResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.abonnement.SouscrireOffreRequest;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeAbonnementService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/pharmacopee/abonnement")
@RequiredArgsConstructor
@Tag(name = "Pharmacopée - Abonnement", description = "Abonnement SaaS de la pharmacopée : offres, souscription, paiements, factures")
@SecurityRequirement(name = "bearerAuth")
public class PharmacopeeAbonnementController {

    private final IPharmacopeeAbonnementService abonnementService;

    @GetMapping
    @Operation(summary = "Abonnement en vigueur", description = "Permission ABONNEMENT_CONSULTER. Avec les limites et leur consommation.")
    @ApiResponse(responseCode = "200", description = "Abonnement en vigueur")
    @ApiResponse(responseCode = "404", description = "Aucun abonnement en vigueur")
    public ResponseEntity<AbonnementResponse> getAbonnementEnVigueur() {
        return ResponseEntity.ok(abonnementService.getAbonnementEnVigueur());
    }

    @GetMapping("/historique")
    @Operation(summary = "Abonnements successifs", description = "Permission ABONNEMENT_CONSULTER.")
    @ApiResponse(responseCode = "200", description = "Liste paginée")
    public ResponseEntity<Page<AbonnementResponse>> getHistorique(@ParameterObject Pageable pageable) {
        return ResponseEntity.ok(abonnementService.getHistorique(pageable));
    }

    @GetMapping("/factures")
    @Operation(summary = "Factures d'abonnement", description = "Permission ABONNEMENT_CONSULTER.")
    @ApiResponse(responseCode = "200", description = "Liste paginée")
    public ResponseEntity<Page<FactureAbonnementResponse>> getFactures(@ParameterObject Pageable pageable) {
        return ResponseEntity.ok(abonnementService.getFactures(pageable));
    }

    @GetMapping("/offres")
    @Operation(summary = "Offres disponibles", description = "Permission ABONNEMENT_CONSULTER.")
    @ApiResponse(responseCode = "200", description = "Offres actives")
    public ResponseEntity<List<OffreResponse>> getOffres() {
        return ResponseEntity.ok(abonnementService.getOffres());
    }

    @PostMapping("/souscriptions")
    @Operation(summary = "Souscrire à une offre", description = "Permission ABONNEMENT_GERER. L'abonnement est activé après confirmation du paiement.")
    @ApiResponse(responseCode = "201", description = "Souscription en attente de paiement")
    @ApiResponse(responseCode = "400", description = "Offre non souscriptible")
    @ApiResponse(responseCode = "409", description = "Une souscription est déjà en attente")
    public ResponseEntity<AbonnementResponse> souscrire(@Valid @RequestBody SouscrireOffreRequest request) {
        return new ResponseEntity<>(abonnementService.souscrire(request), HttpStatus.CREATED);
    }

    @PostMapping("/souscriptions/{id}/paiements")
    @Operation(summary = "Déclarer un paiement", description = "Permission ABONNEMENT_GERER. À confirmer par l'administrateur LADAFURA.")
    @ApiResponse(responseCode = "201", description = "Paiement déclaré")
    @ApiResponse(responseCode = "400", description = "Montant différent du prix")
    @ApiResponse(responseCode = "409", description = "Abonnement non en attente, ou paiement déjà déclaré")
    public ResponseEntity<PaiementAbonnementResponse> declarerPaiement(@PathVariable Long id, @Valid @RequestBody DeclarerPaiementRequest request) {
        return new ResponseEntity<>(abonnementService.declarerPaiement(id, request), HttpStatus.CREATED);
    }

    @PatchMapping("/souscriptions/{id}/annuler")
    @Operation(summary = "Annuler une souscription", description = "Permission ABONNEMENT_GERER. Uniquement en attente de paiement ou programmée.")
    @ApiResponse(responseCode = "200", description = "Souscription annulée")
    @ApiResponse(responseCode = "409", description = "Statut incompatible")
    public ResponseEntity<AbonnementResponse> annuler(@PathVariable Long id, @Valid @RequestBody MotifRequest request) {
        return ResponseEntity.ok(abonnementService.annuler(id, request));
    }

    @PostMapping("/demandes-contrat")
    @Operation(summary = "Demander un contrat personnalisé", description = "Permission ABONNEMENT_GERER. Aucun abonnement n'est créé automatiquement.")
    @ApiResponse(responseCode = "201", description = "Demande enregistrée")
    public ResponseEntity<DemandeContratResponse> demanderContrat(@Valid @RequestBody DemandeContratRequest request) {
        return new ResponseEntity<>(abonnementService.demanderContrat(request), HttpStatus.CREATED);
    }
}
