package com.pharmacopee.ladafura.controllers.admin;

import java.util.List;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pharmacopee.ladafura.dto.admin.produit.AdminCategorieRequest;
import com.pharmacopee.ladafura.dto.admin.produit.AdminCategorieResponse;
import com.pharmacopee.ladafura.dto.admin.produit.AdminModerateProduitRequest;
import com.pharmacopee.ladafura.dto.admin.produit.AdminProduitResponse;
import com.pharmacopee.ladafura.enums.StatutProduit;
import com.pharmacopee.ladafura.services.interfaces.IAdminProduitService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/admin/produits")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMINISTRATEUR')")
@Tag(name = "Admin - Catalogue & Catégories Produits", description = "Endpoints de modération des remèdes traditionnels et gestion des catégories (US-30)")
public class AdminProduitController {

    private final IAdminProduitService produitService;

    @GetMapping
    @Operation(summary = "Lister les produits avec pagination et filtres", description = "Permet de filtrer par statut (EN_ATTENTE, VALIDE, REJETE, ARCHIVE) et par catégorie.")
    @ApiResponse(responseCode = "200", description = "Liste paginée des produits")
    public ResponseEntity<Page<AdminProduitResponse>> getAllProduits(
            @RequestParam(required = false) StatutProduit statut,
            @RequestParam(required = false) Long categorieId,
            @ParameterObject Pageable pageable) {
        return ResponseEntity.ok(produitService.getAllProduits(statut, categorieId, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtenir le détail d'un produit", description = "Retourne la composition à base de plantes, la forme galénique, le prix et les notes.")
    @ApiResponse(responseCode = "200", description = "Détails du produit")
    @ApiResponse(responseCode = "404", description = "Produit introuvable")
    public ResponseEntity<AdminProduitResponse> getProduitById(@PathVariable Long id) {
        return ResponseEntity.ok(produitService.getProduitById(id));
    }

    @PatchMapping("/{id}/moderate")
    @Operation(summary = "Modérer un produit", description = "Permet d'approuver ou rejeter la mise en vente d'un produit traditionnel.")
    @ApiResponse(responseCode = "200", description = "Statut du produit mis à jour avec succès")
    @ApiResponse(responseCode = "400", description = "Données invalides")
    @ApiResponse(responseCode = "404", description = "Produit introuvable")
    public ResponseEntity<AdminProduitResponse> moderateProduit(
            @PathVariable Long id,
            @Valid @RequestBody AdminModerateProduitRequest request) {
        return ResponseEntity.ok(produitService.moderateProduit(id, request));
    }

    @GetMapping("/categories")
    @Operation(summary = "Lister toutes les catégories de produits", description = "Fournit l'ensemble des catégories avec le nombre de produits associés.")
    @ApiResponse(responseCode = "200", description = "Liste des catégories")
    public ResponseEntity<List<AdminCategorieResponse>> getAllCategories() {
        return ResponseEntity.ok(produitService.getAllCategories());
    }

    @PostMapping("/categories")
    @Operation(summary = "Créer une nouvelle catégorie de produit", description = "Permet d'ajouter une classification (ex: Sirops, Infusions, Poudres, Baumes).")
    @ApiResponse(responseCode = "201", description = "Catégorie créée avec succès")
    @ApiResponse(responseCode = "400", description = "Données invalides")
    public ResponseEntity<AdminCategorieResponse> createCategorie(@Valid @RequestBody AdminCategorieRequest request) {
        AdminCategorieResponse created = produitService.createCategorie(request);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/categories/{id}")
    @Operation(summary = "Mettre à jour une catégorie de produit", description = "Modifie le libellé, la description ou l'état actif/inactif d'une catégorie.")
    @ApiResponse(responseCode = "200", description = "Catégorie mise à jour avec succès")
    @ApiResponse(responseCode = "404", description = "Catégorie introuvable")
    public ResponseEntity<AdminCategorieResponse> updateCategorie(
            @PathVariable Long id,
            @Valid @RequestBody AdminCategorieRequest request) {
        return ResponseEntity.ok(produitService.updateCategorie(id, request));
    }

    @DeleteMapping("/categories/{id}")
    @Operation(summary = "Supprimer une catégorie de produit", description = "Supprime la catégorie si aucun produit n'y est rattaché.")
    @ApiResponse(responseCode = "204", description = "Catégorie supprimée avec succès")
    @ApiResponse(responseCode = "400", description = "Suppression impossible car des produits sont liés")
    @ApiResponse(responseCode = "404", description = "Catégorie introuvable")
    public ResponseEntity<Void> deleteCategorie(@PathVariable Long id) {
        produitService.deleteCategorie(id);
        return ResponseEntity.noContent().build();
    }
}
