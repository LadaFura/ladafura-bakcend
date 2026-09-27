package com.pharmacopee.ladafura.controllers.pharmacopee;

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

import com.pharmacopee.ladafura.dto.pharmacopee.commande.PharmacopeeCommandeDetailResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.commande.PharmacopeeCommandeItemResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.commande.PharmacopeeCommandeSummaryResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.commande.UpdateStatutCommandeRequest;
import com.pharmacopee.ladafura.enums.StatutCommande;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeCommandeService;

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
@RequestMapping("/api/v1/pharmacopee/commandes")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Pharmacopée - Commandes", description = "Endpoints de gestion, préparation et traitement des commandes clients (Livraison et Pickup)")
@SecurityRequirement(name = "bearerAuth")
public class PharmacopeeCommandeController {

    private final IPharmacopeeCommandeService commandeService;

    @Operation(summary = "Consulter la liste paginée des commandes",
               description = "Renvoie l'historique et les commandes en cours de l'officine avec filtre optionnel par statut.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Liste des commandes récupérée avec succès"),
            @ApiResponse(responseCode = "401", description = "Non authentifié"),
            @ApiResponse(responseCode = "403", description = "Accès refusé ou établissement non validé")
    })
    @GetMapping
    public ResponseEntity<Page<PharmacopeeCommandeItemResponse>> getCommandes(
            @Parameter(description = "Filtre optionnel par statut de commande", example = "EN_ATTENTE")
            @RequestParam(required = false) StatutCommande statut,
            @ParameterObject Pageable pageable) {
        log.info("Requête GET /api/v1/pharmacopee/commandes reçue (statut={})", statut);
        return ResponseEntity.ok(commandeService.getCommandes(statut, pageable));
    }

    @Operation(summary = "Synthèse chiffrée et tableau de bord des commandes",
               description = "Renvoie le volume des commandes par état (en attente, confirmées, préparées, en cours, terminées, chiffre d'affaires).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Synthèse calculée avec succès"),
            @ApiResponse(responseCode = "401", description = "Non authentifié"),
            @ApiResponse(responseCode = "403", description = "Accès refusé ou établissement non validé")
    })
    @GetMapping("/summary")
    public ResponseEntity<PharmacopeeCommandeSummaryResponse> getSummary() {
        log.info("Requête GET /api/v1/pharmacopee/commandes/summary reçue");
        return ResponseEntity.ok(commandeService.getSummary());
    }

    @Operation(summary = "Consulter le détail complet d'une commande",
               description = "Renvoie la fiche détaillée de la commande (articles, acheteur, mode de retrait, paiement et prochains statuts autorisés).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Détail de la commande récupéré avec succès"),
            @ApiResponse(responseCode = "401", description = "Non authentifié"),
            @ApiResponse(responseCode = "403", description = "Accès refusé ou commande n'appartenant pas à cette officine"),
            @ApiResponse(responseCode = "404", description = "Commande non trouvée")
    })
    @GetMapping("/{id}")
    public ResponseEntity<PharmacopeeCommandeDetailResponse> getCommandeDetail(
            @Parameter(description = "Identifiant de la commande", example = "10")
            @PathVariable Long id) {
        log.info("Requête GET /api/v1/pharmacopee/commandes/{} reçue", id);
        return ResponseEntity.ok(commandeService.getCommandeDetail(id));
    }

    @Operation(summary = "Mettre à jour le statut d'une commande",
               description = "Applique une transition de statut contrôlée selon la machine à états stricte.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Statut mis à jour avec succès"),
            @ApiResponse(responseCode = "400", description = "Transition de statut interdite"),
            @ApiResponse(responseCode = "401", description = "Non authentifié"),
            @ApiResponse(responseCode = "403", description = "Accès refusé"),
            @ApiResponse(responseCode = "404", description = "Commande non trouvée")
    })
    @PatchMapping("/{id}/statut")
    public ResponseEntity<PharmacopeeCommandeDetailResponse> updateStatut(
            @Parameter(description = "Identifiant de la commande", example = "10")
            @PathVariable Long id,
            @Valid @RequestBody UpdateStatutCommandeRequest request) {
        log.info("Requête PATCH /api/v1/pharmacopee/commandes/{}/statut reçue", id);
        return ResponseEntity.ok(commandeService.updateStatut(id, request));
    }

    @Operation(summary = "Accepter et confirmer la commande",
               description = "Passe la commande de l'état EN_ATTENTE à CONFIRMEE.")
    @PatchMapping("/{id}/confirmer")
    public ResponseEntity<PharmacopeeCommandeDetailResponse> confirmerCommande(
            @Parameter(description = "Identifiant de la commande", example = "10")
            @PathVariable Long id) {
        log.info("Requête PATCH /api/v1/pharmacopee/commandes/{}/confirmer reçue", id);
        return ResponseEntity.ok(commandeService.confirmerCommande(id));
    }

    @Operation(summary = "Marquer la commande comme préparée",
               description = "Passe la commande de l'état CONFIRMEE à PREPAREE (produits emballés en officine).")
    @PatchMapping("/{id}/preparer")
    public ResponseEntity<PharmacopeeCommandeDetailResponse> preparerCommande(
            @Parameter(description = "Identifiant de la commande", example = "10")
            @PathVariable Long id) {
        log.info("Requête PATCH /api/v1/pharmacopee/commandes/{}/preparer reçue", id);
        return ResponseEntity.ok(commandeService.preparerCommande(id));
    }

    @Operation(summary = "Acheminer la commande préparée (Expédition ou Comptoir)",
               description = "Bascule la commande préparée vers EN_LIVRAISON (si mode Livraison) ou DISPONIBLE_PICKUP (si mode Pickup).")
    @PatchMapping("/{id}/acheminer")
    public ResponseEntity<PharmacopeeCommandeDetailResponse> acheminerCommande(
            @Parameter(description = "Identifiant de la commande", example = "10")
            @PathVariable Long id) {
        log.info("Requête PATCH /api/v1/pharmacopee/commandes/{}/acheminer reçue", id);
        return ResponseEntity.ok(commandeService.acheminerCommande(id));
    }

    @Operation(summary = "Clôturer la commande avec succès (Livrée ou Retirée)",
               description = "Finalise la commande vers LIVREE (si Livraison) ou RETIREE (si Pickup).")
    @PatchMapping("/{id}/finaliser")
    public ResponseEntity<PharmacopeeCommandeDetailResponse> finaliserCommande(
            @Parameter(description = "Identifiant de la commande", example = "10")
            @PathVariable Long id) {
        log.info("Requête PATCH /api/v1/pharmacopee/commandes/{}/finaliser reçue", id);
        return ResponseEntity.ok(commandeService.finaliserCommande(id));
    }

    @Operation(summary = "Annuler la commande",
               description = "Passe la commande à l'état ANNULEE avec motif optionnel.")
    @PatchMapping("/{id}/annuler")
    public ResponseEntity<PharmacopeeCommandeDetailResponse> annulerCommande(
            @Parameter(description = "Identifiant de la commande", example = "10")
            @PathVariable Long id,
            @RequestParam(required = false) String motif) {
        log.info("Requête PATCH /api/v1/pharmacopee/commandes/{}/annuler reçue (motif={})", id, motif);
        return ResponseEntity.ok(commandeService.annulerCommande(id, motif));
    }
}
