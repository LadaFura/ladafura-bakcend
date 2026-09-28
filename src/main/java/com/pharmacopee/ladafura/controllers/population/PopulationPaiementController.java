package com.pharmacopee.ladafura.controllers.population;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pharmacopee.ladafura.dto.population.paiement.PopulationMethodePaiementInfoDto;
import com.pharmacopee.ladafura.dto.population.paiement.PopulationPaiementResponse;
import com.pharmacopee.ladafura.dto.population.paiement.PopulationProcessPaiementRequest;
import com.pharmacopee.ladafura.services.interfaces.IPopulationPaiementService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/population/paiements")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Population - Paiements", description = "Endpoints de règlement des commandes (Mobile Money, Espèces, Carte Bancaire)")
public class PopulationPaiementController {

    private final IPopulationPaiementService populationPaiementService;

    @GetMapping("/methodes")
    @Operation(summary = "Consulter les moyens de paiement acceptés",
               description = "Fournit la liste des méthodes disponibles sur LADAFURA (Mobile Money : Orange/Moov/Wave, Espèces à la livraison/retrait, Carte bancaire) avec consignes.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Moyens de paiement récupérés avec succès")
    })
    public ResponseEntity<List<PopulationMethodePaiementInfoDto>> getMethodesPaiement() {
        log.info("Requête GET /api/v1/population/paiements/methodes reçue");
        return ResponseEntity.ok(populationPaiementService.getMethodesPaiement());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_POPULATION')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Effectuer ou initier le règlement d'une commande",
               description = "Permet de payer une commande par Mobile Money (Orange Money, Moov Money, Wave), par Carte Bancaire ou de valider le règlement en Espèces (Cash à la livraison / retrait).")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Paiement traité avec succès"),
        @ApiResponse(responseCode = "400", description = "Commande déjà payée, annulée ou données invalides"),
        @ApiResponse(responseCode = "401", description = "Non authentifié"),
        @ApiResponse(responseCode = "403", description = "Accès refusé"),
        @ApiResponse(responseCode = "404", description = "Commande introuvable")
    })
    public ResponseEntity<PopulationPaiementResponse> payerCommande(
            @Valid @RequestBody PopulationProcessPaiementRequest request) {
        log.info("Requête POST /api/v1/population/paiements reçue pour commande ID: {} (méthode: {})",
                request.getCommandeId(), request.getMethode());
        return ResponseEntity.ok(populationPaiementService.payerCommande(request));
    }

    @GetMapping("/commandes/{commandeId}")
    @PreAuthorize("hasAuthority('ROLE_POPULATION')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Consulter le reçu ou statut de paiement d'une commande",
               description = "Fournit les détails de la transaction financière rattachée à une commande du client.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Reçu de paiement récupéré avec succès"),
        @ApiResponse(responseCode = "401", description = "Non authentifié"),
        @ApiResponse(responseCode = "403", description = "Accès refusé"),
        @ApiResponse(responseCode = "404", description = "Commande ou transaction introuvable")
    })
    public ResponseEntity<PopulationPaiementResponse> getPaiementByCommande(@PathVariable Long commandeId) {
        log.info("Requête GET /api/v1/population/paiements/commandes/{} reçue", commandeId);
        return ResponseEntity.ok(populationPaiementService.getPaiementByCommande(commandeId));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_POPULATION')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Consulter l'historique de ses paiements",
               description = "Fournit la liste paginée de toutes les transactions de paiement effectuées par l'utilisateur connecté.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Historique des paiements récupéré avec succès"),
        @ApiResponse(responseCode = "401", description = "Non authentifié"),
        @ApiResponse(responseCode = "403", description = "Accès refusé")
    })
    public ResponseEntity<Page<PopulationPaiementResponse>> getHistoriquePaiements(
            @PageableDefault(size = 10, sort = "datePaiement", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("Requête GET /api/v1/population/paiements reçue");
        return ResponseEntity.ok(populationPaiementService.getHistoriquePaiements(pageable));
    }
}
