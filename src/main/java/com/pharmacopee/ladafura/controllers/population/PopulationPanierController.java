package com.pharmacopee.ladafura.controllers.population;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pharmacopee.ladafura.dto.population.panier.PopulationAddToCartRequest;
import com.pharmacopee.ladafura.dto.population.panier.PopulationPanierResponse;
import com.pharmacopee.ladafura.dto.population.panier.PopulationUpdateQuantityRequest;
import com.pharmacopee.ladafura.services.interfaces.IPopulationPanierService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/population/panier")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasAuthority('ROLE_POPULATION')")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Population - Panier", description = "Endpoints de gestion du panier d'achat (ajout, modification de quantité, suppression d'articles, vidage)")
public class PopulationPanierController {

    private final IPopulationPanierService populationPanierService;

    @GetMapping
    @Operation(summary = "Consulter son panier actif",
               description = "Récupère le panier d'achat actif de l'utilisateur connecté avec l'ensemble des articles, le détail des prix et le montant total calculé.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Panier actif récupéré avec succès"),
        @ApiResponse(responseCode = "401", description = "Non authentifié (token Firebase manquant ou invalide)"),
        @ApiResponse(responseCode = "403", description = "Accès refusé : rôle POPULATION requis")
    })
    public ResponseEntity<PopulationPanierResponse> getPanier() {
        log.info("Requête GET /api/v1/population/panier reçue");
        return ResponseEntity.ok(populationPanierService.getPanier());
    }

    @PostMapping("/lignes")
    @Operation(summary = "Ajouter un produit au panier",
               description = "Ajoute un produit au panier actif ou incrémente la quantité s'il s'y trouve déjà. Vérifie la validité du produit.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Produit ajouté au panier avec succès"),
        @ApiResponse(responseCode = "400", description = "Données invalides ou produit non disponible"),
        @ApiResponse(responseCode = "401", description = "Non authentifié"),
        @ApiResponse(responseCode = "403", description = "Accès refusé"),
        @ApiResponse(responseCode = "404", description = "Produit introuvable")
    })
    public ResponseEntity<PopulationPanierResponse> ajouterProduitAuPanier(
            @Valid @RequestBody PopulationAddToCartRequest request) {
        log.info("Requête POST /api/v1/population/panier/lignes reçue pour produit ID: {}", request.getProduitId());
        return ResponseEntity.ok(populationPanierService.ajouterProduitAuPanier(request));
    }

    @PutMapping("/lignes/{ligneId}")
    @Operation(summary = "Modifier la quantité d'un article dans le panier",
               description = "Modifie la quantité commandée pour une ligne d'article donnée. Recalcule le sous-total de la ligne et le total du panier.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Quantité modifiée avec succès"),
        @ApiResponse(responseCode = "400", description = "Quantité invalide"),
        @ApiResponse(responseCode = "401", description = "Non authentifié"),
        @ApiResponse(responseCode = "403", description = "Accès refusé"),
        @ApiResponse(responseCode = "404", description = "Ligne de panier introuvable")
    })
    public ResponseEntity<PopulationPanierResponse> modifierQuantiteLigne(
            @PathVariable Long ligneId,
            @Valid @RequestBody PopulationUpdateQuantityRequest request) {
        log.info("Requête PUT /api/v1/population/panier/lignes/{} reçue avec quantité: {}", ligneId, request.getQuantite());
        return ResponseEntity.ok(populationPanierService.modifierQuantiteLigne(ligneId, request));
    }

    @DeleteMapping("/lignes/{ligneId}")
    @Operation(summary = "Supprimer un article du panier",
               description = "Retire définitivement une ligne d'article du panier actif.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Ligne supprimée avec succès"),
        @ApiResponse(responseCode = "401", description = "Non authentifié"),
        @ApiResponse(responseCode = "403", description = "Accès refusé"),
        @ApiResponse(responseCode = "404", description = "Ligne de panier introuvable")
    })
    public ResponseEntity<PopulationPanierResponse> supprimerLignePanier(@PathVariable Long ligneId) {
        log.info("Requête DELETE /api/v1/population/panier/lignes/{} reçue", ligneId);
        return ResponseEntity.ok(populationPanierService.supprimerLignePanier(ligneId));
    }

    @DeleteMapping
    @Operation(summary = "Vider intégralement le panier",
               description = "Supprime toutes les lignes d'articles présentes dans le panier actif de l'utilisateur.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Panier vidé avec succès"),
        @ApiResponse(responseCode = "401", description = "Non authentifié"),
        @ApiResponse(responseCode = "403", description = "Accès refusé")
    })
    public ResponseEntity<Void> viderPanier() {
        log.info("Requête DELETE /api/v1/population/panier reçue (vidage complet)");
        populationPanierService.viderPanier();
        return ResponseEntity.noContent().build();
    }
}
