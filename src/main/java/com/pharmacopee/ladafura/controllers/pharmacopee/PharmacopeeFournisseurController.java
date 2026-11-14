package com.pharmacopee.ladafura.controllers.pharmacopee;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

import com.pharmacopee.ladafura.dto.pharmacopee.fournisseur.EnregistrerFournisseurProduitRequest;
import com.pharmacopee.ladafura.dto.pharmacopee.fournisseur.EnregistrerFournisseurRequest;
import com.pharmacopee.ladafura.dto.pharmacopee.fournisseur.FournisseurProduitResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.fournisseur.FournisseurResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.fournisseur.HistoriqueAchatFournisseurResponse;
import com.pharmacopee.ladafura.enums.StatutFournisseur;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeFournisseurService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/pharmacopee/fournisseurs")
@RequiredArgsConstructor
@Tag(name = "Pharmacopée - Fournisseurs", description = "Gestion facultative des fournisseurs et des produits qu'ils proposent")
@SecurityRequirement(name = "bearerAuth")
public class PharmacopeeFournisseurController {

    private final IPharmacopeeFournisseurService fournisseurService;

    @GetMapping
    @Operation(summary = "Lister les fournisseurs", description = "Permission FOURNISSEURS_CONSULTER.")
    @ApiResponse(responseCode = "200", description = "Liste paginée")
    public ResponseEntity<Page<FournisseurResponse>> getFournisseurs(@RequestParam(required = false) StatutFournisseur statut,
                                                                     @ParameterObject Pageable pageable) {
        return ResponseEntity.ok(fournisseurService.getFournisseurs(statut, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulter un fournisseur", description = "Permission FOURNISSEURS_CONSULTER.")
    @ApiResponse(responseCode = "200", description = "Fournisseur")
    @ApiResponse(responseCode = "404", description = "Fournisseur introuvable dans cette pharmacopée")
    public ResponseEntity<FournisseurResponse> getFournisseur(@PathVariable Long id) {
        return ResponseEntity.ok(fournisseurService.getFournisseur(id));
    }

    @PostMapping
    @Operation(summary = "Créer un fournisseur", description = "Permission FOURNISSEURS_GERER.")
    @ApiResponse(responseCode = "201", description = "Fournisseur créé")
    public ResponseEntity<FournisseurResponse> creer(@Valid @RequestBody EnregistrerFournisseurRequest request) {
        return new ResponseEntity<>(fournisseurService.creer(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier un fournisseur", description = "Permission FOURNISSEURS_GERER.")
    @ApiResponse(responseCode = "200", description = "Fournisseur modifié")
    public ResponseEntity<FournisseurResponse> modifier(@PathVariable Long id, @Valid @RequestBody EnregistrerFournisseurRequest request) {
        return ResponseEntity.ok(fournisseurService.modifier(id, request));
    }

    @PatchMapping("/{id}/desactiver")
    @Operation(summary = "Désactiver un fournisseur", description = "Il ne peut plus être choisi pour un nouvel achat.")
    @ApiResponse(responseCode = "200", description = "Fournisseur désactivé")
    public ResponseEntity<FournisseurResponse> desactiver(@PathVariable Long id) {
        return ResponseEntity.ok(fournisseurService.changerStatut(id, StatutFournisseur.INACTIF));
    }

    @PatchMapping("/{id}/reactiver")
    @Operation(summary = "Réactiver un fournisseur", description = "Permission FOURNISSEURS_GERER.")
    @ApiResponse(responseCode = "200", description = "Fournisseur réactivé")
    public ResponseEntity<FournisseurResponse> reactiver(@PathVariable Long id) {
        return ResponseEntity.ok(fournisseurService.changerStatut(id, StatutFournisseur.ACTIF));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un fournisseur sans achat", description = "Permission FOURNISSEURS_GERER.")
    @ApiResponse(responseCode = "204", description = "Fournisseur supprimé")
    @ApiResponse(responseCode = "409", description = "Le fournisseur a des achats")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        fournisseurService.supprimer(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/produits")
    @Operation(summary = "Produits proposés par un fournisseur", description = "Permission FOURNISSEURS_CONSULTER.")
    @ApiResponse(responseCode = "200", description = "Liste paginée")
    public ResponseEntity<Page<FournisseurProduitResponse>> getProduits(@PathVariable Long id, @ParameterObject Pageable pageable) {
        return ResponseEntity.ok(fournisseurService.getProduits(id, pageable));
    }

    @PostMapping("/{id}/produits")
    @Operation(summary = "Ajouter un produit proposé", description = "Permission FOURNISSEURS_GERER. Information seulement : ce n'est pas une commande.")
    @ApiResponse(responseCode = "201", description = "Produit ajouté")
    @ApiResponse(responseCode = "409", description = "Produit déjà enregistré pour ce fournisseur")
    public ResponseEntity<FournisseurProduitResponse> ajouterProduit(@PathVariable Long id, @Valid @RequestBody EnregistrerFournisseurProduitRequest request) {
        return new ResponseEntity<>(fournisseurService.ajouterProduit(id, request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}/produits/{ligneId}")
    @Operation(summary = "Modifier un produit proposé", description = "Permission FOURNISSEURS_GERER.")
    @ApiResponse(responseCode = "200", description = "Produit modifié")
    public ResponseEntity<FournisseurProduitResponse> modifierProduit(@PathVariable Long id, @PathVariable Long ligneId,
                                                                      @Valid @RequestBody EnregistrerFournisseurProduitRequest request) {
        return ResponseEntity.ok(fournisseurService.modifierProduit(id, ligneId, request));
    }

    @DeleteMapping("/{id}/produits/{ligneId}")
    @Operation(summary = "Retirer un produit proposé", description = "Permission FOURNISSEURS_GERER.")
    @ApiResponse(responseCode = "204", description = "Produit retiré")
    public ResponseEntity<Void> retirerProduit(@PathVariable Long id, @PathVariable Long ligneId) {
        fournisseurService.retirerProduit(id, ligneId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/historique")
    @Operation(summary = "Produits commandés auprès d'un fournisseur", description = "Permission ACHATS_CONSULTER. Achats validés uniquement.")
    @ApiResponse(responseCode = "200", description = "Liste paginée")
    public ResponseEntity<Page<HistoriqueAchatFournisseurResponse>> getHistorique(@PathVariable Long id, @ParameterObject Pageable pageable) {
        return ResponseEntity.ok(fournisseurService.getHistorique(id, pageable));
    }
}
