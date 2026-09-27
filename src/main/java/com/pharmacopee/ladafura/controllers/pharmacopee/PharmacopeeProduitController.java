package com.pharmacopee.ladafura.controllers.pharmacopee;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pharmacopee.ladafura.dto.pharmacopee.produit.AssocierProduitRequest;
import com.pharmacopee.ladafura.dto.pharmacopee.produit.CatalogueProduitItemResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.produit.PharmacopeeProduitResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.produit.UpdateProduitDisponibiliteRequest;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeProduitService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/pharmacopee/produits")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Pharmacopée - Produits & Catalogue", description = "Endpoints de gestion du catalogue de l'officine (association, mise à jour stock et retrait)")
@SecurityRequirement(name = "bearerAuth")
public class PharmacopeeProduitController {

    private final IPharmacopeeProduitService pharmacopeeProduitService;

    @GetMapping
    @Operation(summary = "Lister les produits associés à sa pharmacopée",
               description = "Renvoie la liste paginée des remèdes et produits référencés dans l'établissement connecté avec filtres optionnels.")
    @ApiResponse(responseCode = "200", description = "Liste paginée des produits de la pharmacopée")
    public ResponseEntity<Page<PharmacopeeProduitResponse>> getMesProduits(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categorieId,
            @RequestParam(required = false) Boolean disponible,
            @ParameterObject Pageable pageable) {
        return ResponseEntity.ok(pharmacopeeProduitService.getMesProduits(keyword, categorieId, disponible, pageable));
    }

    @GetMapping("/catalogue")
    @Operation(summary = "Consulter le catalogue national des produits validés",
               description = "Permet de parcourir les produits certifiés LADAFURA pour identifier ceux que l'officine souhaite ajouter à son stock.")
    @ApiResponse(responseCode = "200", description = "Liste paginée des produits du catalogue général")
    public ResponseEntity<Page<CatalogueProduitItemResponse>> getCatalogueGlobal(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categorieId,
            @ParameterObject Pageable pageable) {
        return ResponseEntity.ok(pharmacopeeProduitService.getCatalogueGlobal(keyword, categorieId, pageable));
    }

    @GetMapping("/{produitId}")
    @Operation(summary = "Obtenir le détail d'un produit dans son officine",
               description = "Renvoie les informations produit et les données de stock et disponibilité pour l'officine connectée.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Détail du produit récupéré avec succès"),
        @ApiResponse(responseCode = "404", description = "Produit non trouvé ou non associé à cette pharmacopée")
    })
    public ResponseEntity<PharmacopeeProduitResponse> getMonProduit(@PathVariable Long produitId) {
        return ResponseEntity.ok(pharmacopeeProduitService.getMonProduit(produitId));
    }

    @PostMapping
    @Operation(summary = "Associer un produit du catalogue à son officine",
               description = "Ajoute un produit certifié à l'inventaire de la pharmacopée. Requiert que la pharmacopée soit agréée (VALIDEE).")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Produit associé avec succès"),
        @ApiResponse(responseCode = "400", description = "Produit non validé ou données invalides"),
        @ApiResponse(responseCode = "403", description = "Pharmacopée non validée ou compte non autorisé"),
        @ApiResponse(responseCode = "409", description = "Ce produit est déjà présent dans le catalogue de votre pharmacopée")
    })
    public ResponseEntity<PharmacopeeProduitResponse> associerProduit(@Valid @RequestBody AssocierProduitRequest request) {
        log.info("Association du produit ID {} à la pharmacopée connectée", request.getProduitId());
        PharmacopeeProduitResponse response = pharmacopeeProduitService.associerProduit(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{produitId}")
    @Operation(summary = "Modifier le stock et la disponibilité d'un produit",
               description = "Met à jour la quantité en stock ou active/désactive la vente pour ce produit spécifique dans votre officine.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Disponibilité et stock mis à jour avec succès"),
        @ApiResponse(responseCode = "400", description = "Données invalides (ex: stock négatif)"),
        @ApiResponse(responseCode = "404", description = "Produit non associé à votre pharmacopée")
    })
    public ResponseEntity<PharmacopeeProduitResponse> updateDisponibilite(
            @PathVariable Long produitId,
            @Valid @RequestBody UpdateProduitDisponibiliteRequest request) {
        log.info("Mise à jour de la disponibilité du produit ID {} pour la pharmacopée connectée", produitId);
        return ResponseEntity.ok(pharmacopeeProduitService.updateDisponibilite(produitId, request));
    }

    @DeleteMapping("/{produitId}")
    @Operation(summary = "Retirer un produit de son officine",
               description = "Supprime la disponibilité du produit pour votre pharmacopée. Le produit reste disponible dans le catalogue général pour les autres officines.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Produit retiré avec succès"),
        @ApiResponse(responseCode = "404", description = "Produit non associé à votre pharmacopée")
    })
    public ResponseEntity<Void> retirerProduit(@PathVariable Long produitId) {
        log.info("Retrait du produit ID {} de la pharmacopée connectée", produitId);
        pharmacopeeProduitService.retirerProduit(produitId);
        return ResponseEntity.noContent().build();
    }
}
