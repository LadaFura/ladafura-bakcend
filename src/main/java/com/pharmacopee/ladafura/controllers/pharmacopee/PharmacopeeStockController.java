package com.pharmacopee.ladafura.controllers.pharmacopee;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pharmacopee.ladafura.dto.pharmacopee.stock.PharmacopeeStockResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.stock.PharmacopeeStockSummaryResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.stock.UpdatePrixRequest;
import com.pharmacopee.ladafura.dto.pharmacopee.stock.UpdateStockCompletRequest;
import com.pharmacopee.ladafura.dto.pharmacopee.stock.UpdateStockRequest;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeStockService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/pharmacopee/stock")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Pharmacopée - Stock & Disponibilité", description = "Endpoints de gestion physique de l'inventaire, des prix de vente et des activations de produits")
@SecurityRequirement(name = "bearerAuth")
public class PharmacopeeStockController {

    private final IPharmacopeeStockService stockService;

    @GetMapping
    @Operation(summary = "Lister les stocks et disponibilités de l'officine",
               description = "Renvoie la liste paginée de l'inventaire avec filtrage possible par état de disponibilité.")
    @ApiResponse(responseCode = "200", description = "Inventaire des stocks récupéré avec succès")
    public ResponseEntity<Page<PharmacopeeStockResponse>> getStocks(
            @RequestParam(required = false) Boolean disponible,
            @RequestParam(required = false) Boolean enRupture,
            @ParameterObject Pageable pageable) {
        return ResponseEntity.ok(stockService.getStocks(disponible, enRupture, pageable));
    }

    @GetMapping("/summary")
    @Operation(summary = "Obtenir la synthèse chiffrée de l'inventaire",
               description = "Fournit les indicateurs clés : valeur marchande globale, nombre de références actives, nombre de ruptures.")
    @ApiResponse(responseCode = "200", description = "Synthèse d'inventaire calculée avec succès")
    public ResponseEntity<PharmacopeeStockSummaryResponse> getSummary() {
        log.info("Consultation de la synthèse d'inventaire de la pharmacopée connectée");
        return ResponseEntity.ok(stockService.getSummary());
    }

    @GetMapping("/{produitId}")
    @Operation(summary = "Consulter le stock et le prix d'un produit précis",
               description = "Détaille la quantité restante, le prix de vente effectif et l'historique de mise à jour pour ce remède.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Fiche de stock récupérée avec succès"),
        @ApiResponse(responseCode = "404", description = "Produit non trouvé dans votre officine")
    })
    public ResponseEntity<PharmacopeeStockResponse> getStockByProduit(@PathVariable Long produitId) {
        return ResponseEntity.ok(stockService.getStockByProduit(produitId));
    }

    @PatchMapping("/{produitId}/quantite")
    @Operation(summary = "Mettre à jour rapidement la quantité en stock",
               description = "Permet d'ajuster immédiatement le stock physique suite à une entrée ou un réapprovisionnement.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Quantité en stock ajustée avec succès"),
        @ApiResponse(responseCode = "400", description = "Quantité négative ou invalide"),
        @ApiResponse(responseCode = "404", description = "Produit non trouvé dans votre officine")
    })
    public ResponseEntity<PharmacopeeStockResponse> updateQuantiteStock(
            @PathVariable Long produitId,
            @Valid @RequestBody UpdateStockRequest request) {
        log.info("Ajustement de la quantité en stock pour le produit ID {} : {} unités", produitId, request.getQuantiteStock());
        return ResponseEntity.ok(stockService.updateQuantiteStock(produitId, request));
    }

    @PatchMapping("/{produitId}/prix")
    @Operation(summary = "Définir ou ajuster le prix de vente pratiqué par l'officine",
               description = "Permet de personnaliser le tarif de vente en FCFA (différent ou aligné sur le prix national conseillé).")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Prix de vente mis à jour avec succès"),
        @ApiResponse(responseCode = "400", description = "Prix négatif ou nul"),
        @ApiResponse(responseCode = "404", description = "Produit non trouvé dans votre officine")
    })
    public ResponseEntity<PharmacopeeStockResponse> updatePrix(
            @PathVariable Long produitId,
            @Valid @RequestBody UpdatePrixRequest request) {
        log.info("Ajustement du prix de vente pour le produit ID {} : {} FCFA", produitId, request.getPrix());
        return ResponseEntity.ok(stockService.updatePrix(produitId, request));
    }

    @PatchMapping("/{produitId}/toggle-disponibilite")
    @Operation(summary = "Activer ou désactiver rapidement la vente d'un produit",
               description = "Bascule immédiate (1 clic) : suspend temporairement la vente sans altérer la quantité en stock.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Disponibilité basculée avec succès"),
        @ApiResponse(responseCode = "404", description = "Produit non trouvé dans votre officine")
    })
    public ResponseEntity<PharmacopeeStockResponse> toggleDisponibilite(@PathVariable Long produitId) {
        log.info("Bascule de la disponibilité pour le produit ID {}", produitId);
        return ResponseEntity.ok(stockService.toggleDisponibilite(produitId));
    }

    @PutMapping("/{produitId}")
    @Operation(summary = "Mise à jour complète du stock, prix et disponibilité",
               description = "Formulaire complet pour modifier conjointement quantité, tarif spécifique et statut de vente.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Fiche de stock mise à jour avec succès"),
        @ApiResponse(responseCode = "400", description = "Données invalides"),
        @ApiResponse(responseCode = "404", description = "Produit non trouvé dans votre officine")
    })
    public ResponseEntity<PharmacopeeStockResponse> updateStockComplet(
            @PathVariable Long produitId,
            @Valid @RequestBody UpdateStockCompletRequest request) {
        log.info("Mise à jour complète de la gestion stock/prix pour le produit ID {}", produitId);
        return ResponseEntity.ok(stockService.updateStockComplet(produitId, request));
    }
}
