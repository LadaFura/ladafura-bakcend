package com.pharmacopee.ladafura.controllers.pharmacopee;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pharmacopee.ladafura.dto.pharmacopee.paiement.EncaisserCashRequest;
import com.pharmacopee.ladafura.dto.pharmacopee.paiement.PharmacopeePaiementResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.paiement.PharmacopeePaiementSummaryResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.paiement.SimulerPaiementRequest;
import com.pharmacopee.ladafura.enums.MethodePaiement;
import com.pharmacopee.ladafura.enums.StatutPaiement;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeePaiementService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/pharmacopee/paiements")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Pharmacopée - Paiements", description = "Endpoints de consultation financière, encaissement (Cash) et simulation de règlement (Mobile Money, Espèces, Carte bancaire)")
@SecurityRequirement(name = "bearerAuth")
public class PharmacopeePaiementController {

    private final IPharmacopeePaiementService paiementService;

    @Operation(summary = "Consulter la liste paginée des règlements financiers",
               description = "Renvoie l'historique des transactions financières de l'officine avec filtres optionnels par statut et méthode.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Liste des règlements récupérée avec succès"),
            @ApiResponse(responseCode = "401", description = "Non authentifié"),
            @ApiResponse(responseCode = "403", description = "Accès refusé ou établissement non validé")
    })
    @GetMapping
    public ResponseEntity<Page<PharmacopeePaiementResponse>> getPaiements(
            @Parameter(description = "Filtre par statut de règlement", example = "REUSSI")
            @RequestParam(required = false) StatutPaiement statut,
            @Parameter(description = "Filtre par méthode de règlement", example = "MOBILE_MONEY")
            @RequestParam(required = false) MethodePaiement methode,
            @ParameterObject Pageable pageable) {
        log.info("Requête GET /api/v1/pharmacopee/paiements reçue (statut={}, methode={})", statut, methode);
        return ResponseEntity.ok(paiementService.getPaiements(statut, methode, pageable));
    }

    @Operation(summary = "Synthèse financière et encaissements par canal",
               description = "Renvoie le volume total encaissé et la ventilation par Mobile Money, Espèces (Cash) et Carte bancaire.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Synthèse financière calculée avec succès"),
            @ApiResponse(responseCode = "401", description = "Non authentifié"),
            @ApiResponse(responseCode = "403", description = "Accès refusé ou établissement non validé")
    })
    @GetMapping("/summary")
    public ResponseEntity<PharmacopeePaiementSummaryResponse> getSummary() {
        log.info("Requête GET /api/v1/pharmacopee/paiements/summary reçue");
        return ResponseEntity.ok(paiementService.getSummary());
    }

    @Operation(summary = "Consulter le détail d'un paiement",
               description = "Renvoie la fiche complète d'une transaction financière.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Détail du paiement récupéré avec succès"),
            @ApiResponse(responseCode = "401", description = "Non authentifié"),
            @ApiResponse(responseCode = "403", description = "Accès refusé"),
            @ApiResponse(responseCode = "404", description = "Paiement non trouvé")
    })
    @GetMapping("/{id}")
    public ResponseEntity<PharmacopeePaiementResponse> getPaiementDetail(
            @Parameter(description = "Identifiant du paiement", example = "1")
            @PathVariable Long id) {
        log.info("Requête GET /api/v1/pharmacopee/paiements/{} reçue", id);
        return ResponseEntity.ok(paiementService.getPaiementDetail(id));
    }

    @Operation(summary = "Consulter le paiement rattaché à une commande",
               description = "Renvoie le détail de la transaction financière liée au numéro de commande spécifié.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Paiement de la commande récupéré avec succès"),
            @ApiResponse(responseCode = "401", description = "Non authentifié"),
            @ApiResponse(responseCode = "403", description = "Accès refusé"),
            @ApiResponse(responseCode = "404", description = "Commande ou paiement introuvable")
    })
    @GetMapping("/commande/{commandeId}")
    public ResponseEntity<PharmacopeePaiementResponse> getPaiementByCommande(
            @Parameter(description = "Identifiant de la commande", example = "10")
            @PathVariable Long commandeId) {
        log.info("Requête GET /api/v1/pharmacopee/paiements/commande/{} reçue", commandeId);
        return ResponseEntity.ok(paiementService.getPaiementByCommande(commandeId));
    }

    @Operation(summary = "Simuler un règlement financier (Mobile Money, Espèces, Carte)",
               description = "Simule la passerelle de paiement (sans API externe réelle) avec choix de la méthode et du résultat (succès/échec). Confirme automatiquement la commande si le paiement réussit.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Paiement simulé avec succès"),
            @ApiResponse(responseCode = "400", description = "Données invalides"),
            @ApiResponse(responseCode = "401", description = "Non authentifié"),
            @ApiResponse(responseCode = "403", description = "Accès refusé"),
            @ApiResponse(responseCode = "404", description = "Commande non trouvée")
    })
    @PostMapping("/simuler")
    public ResponseEntity<PharmacopeePaiementResponse> simulerPaiement(
            @Valid @RequestBody SimulerPaiementRequest request) {
        log.info("Requête POST /api/v1/pharmacopee/paiements/simuler reçue pour commande ID {}", request.getCommandeId());
        return ResponseEntity.ok(paiementService.simulerPaiement(request));
    }

    @Operation(summary = "Valider l'encaissement en espèces (Cash)",
               description = "Enregistre la réception physique des espèces par l'officine (au guichet ou remise par le livreur) et marque le règlement comme réussi.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Encaissement validé avec succès"),
            @ApiResponse(responseCode = "400", description = "Le paiement n'est pas de type CASH"),
            @ApiResponse(responseCode = "401", description = "Non authentifié"),
            @ApiResponse(responseCode = "403", description = "Accès refusé"),
            @ApiResponse(responseCode = "404", description = "Paiement non trouvé")
    })
    @PatchMapping("/{id}/encaisser-cash")
    public ResponseEntity<PharmacopeePaiementResponse> encaisserCash(
            @Parameter(description = "Identifiant du paiement CASH", example = "1")
            @PathVariable Long id,
            @RequestBody(required = false) EncaisserCashRequest request) {
        log.info("Requête PATCH /api/v1/pharmacopee/paiements/{}/encaisser-cash reçue", id);
        return ResponseEntity.ok(paiementService.encaisserCash(id, request));
    }
}
