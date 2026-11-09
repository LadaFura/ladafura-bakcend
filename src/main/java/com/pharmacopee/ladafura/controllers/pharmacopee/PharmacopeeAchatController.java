package com.pharmacopee.ladafura.controllers.pharmacopee;

import java.util.List;

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

import com.pharmacopee.ladafura.dto.pharmacopee.abonnement.MotifRequest;
import com.pharmacopee.ladafura.dto.pharmacopee.achat.AchatResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.achat.EnregistrerAchatRequest;
import com.pharmacopee.ladafura.dto.pharmacopee.achat.EnregistrerReglementAchatRequest;
import com.pharmacopee.ladafura.dto.pharmacopee.achat.ReglementAchatResponse;
import com.pharmacopee.ladafura.enums.StatutAchat;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeAchatService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/pharmacopee/achats")
@RequiredArgsConstructor
@Tag(name = "Pharmacopée - Achats", description = "Achats de marchandises, entrées en stock et règlements")
@SecurityRequirement(name = "bearerAuth")
public class PharmacopeeAchatController {

    private final IPharmacopeeAchatService achatService;

    @GetMapping
    @Operation(summary = "Lister les achats", description = "Permission ACHATS_CONSULTER. Filtres facultatifs.")
    @ApiResponse(responseCode = "200", description = "Liste paginée")
    public ResponseEntity<Page<AchatResponse>> getAchats(@RequestParam(required = false) StatutAchat statut,
                                                         @RequestParam(required = false) Long fournisseurId,
                                                         @ParameterObject Pageable pageable) {
        return ResponseEntity.ok(achatService.getAchats(statut, fournisseurId, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulter un achat", description = "Permission ACHATS_CONSULTER. Avec lignes, montant payé et reste à payer.")
    @ApiResponse(responseCode = "200", description = "Achat")
    @ApiResponse(responseCode = "404", description = "Achat introuvable dans cette pharmacopée")
    public ResponseEntity<AchatResponse> getAchat(@PathVariable Long id) {
        return ResponseEntity.ok(achatService.getAchat(id));
    }

    @PostMapping
    @Operation(summary = "Créer un achat en brouillon", description = "Permission ACHATS_GERER. Le fournisseur est facultatif ; le stock ne bouge pas.")
    @ApiResponse(responseCode = "201", description = "Brouillon créé")
    @ApiResponse(responseCode = "400", description = "Produit non validé, fournisseur inactif ou remises excessives")
    public ResponseEntity<AchatResponse> creer(@Valid @RequestBody EnregistrerAchatRequest request) {
        return new ResponseEntity<>(achatService.creer(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier un brouillon", description = "Permission ACHATS_GERER. Les lignes sont remplacées.")
    @ApiResponse(responseCode = "200", description = "Brouillon modifié")
    @ApiResponse(responseCode = "409", description = "L'achat n'est plus en brouillon")
    public ResponseEntity<AchatResponse> modifier(@PathVariable Long id, @Valid @RequestBody EnregistrerAchatRequest request) {
        return ResponseEntity.ok(achatService.modifier(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un brouillon", description = "Permission ACHATS_GERER.")
    @ApiResponse(responseCode = "204", description = "Brouillon supprimé")
    @ApiResponse(responseCode = "409", description = "L'achat n'est plus en brouillon")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        achatService.supprimer(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/valider")
    @Operation(summary = "Valider un achat", description = "Permission ACHATS_GERER. Fait entrer la marchandise en stock.")
    @ApiResponse(responseCode = "200", description = "Achat validé")
    @ApiResponse(responseCode = "400", description = "Achat sans ligne")
    @ApiResponse(responseCode = "429", description = "Un nouveau produit dépasserait le quota de l'abonnement")
    public ResponseEntity<AchatResponse> valider(@PathVariable Long id) {
        return ResponseEntity.ok(achatService.valider(id));
    }

    @PatchMapping("/{id}/annuler")
    @Operation(summary = "Annuler un achat", description = "Permission ACHATS_GERER. Un achat validé sort du stock.")
    @ApiResponse(responseCode = "200", description = "Achat annulé")
    @ApiResponse(responseCode = "409", description = "Règlements restants ou stock déjà consommé")
    public ResponseEntity<AchatResponse> annuler(@PathVariable Long id, @Valid @RequestBody MotifRequest request) {
        return ResponseEntity.ok(achatService.annuler(id, request.getMotif()));
    }

    @GetMapping("/{id}/reglements")
    @Operation(summary = "Règlements d'un achat", description = "Permission ACHATS_CONSULTER.")
    @ApiResponse(responseCode = "200", description = "Règlements")
    public ResponseEntity<List<ReglementAchatResponse>> getReglements(@PathVariable Long id) {
        return ResponseEntity.ok(achatService.getReglements(id));
    }

    @PostMapping("/{id}/reglements")
    @Operation(summary = "Enregistrer un règlement", description = "Permission ACHATS_GERER. Sortie d'argent sur une caisse ou un compte.")
    @ApiResponse(responseCode = "201", description = "Règlement enregistré")
    @ApiResponse(responseCode = "400", description = "Montant supérieur au reste à payer, ou mode incompatible avec la caisse")
    @ApiResponse(responseCode = "409", description = "Achat non validé, caisse sans session ou solde insuffisant")
    public ResponseEntity<ReglementAchatResponse> enregistrerReglement(@PathVariable Long id, @Valid @RequestBody EnregistrerReglementAchatRequest request) {
        return new ResponseEntity<>(achatService.enregistrerReglement(id, request), HttpStatus.CREATED);
    }

    @PatchMapping("/{id}/reglements/{reglementId}/annuler")
    @Operation(summary = "Annuler un règlement", description = "Permission ACHATS_GERER. Contre-passation en caisse.")
    @ApiResponse(responseCode = "200", description = "Règlement annulé")
    @ApiResponse(responseCode = "409", description = "Règlement déjà annulé")
    public ResponseEntity<ReglementAchatResponse> annulerReglement(@PathVariable Long id, @PathVariable Long reglementId, @Valid @RequestBody MotifRequest request) {
        return ResponseEntity.ok(achatService.annulerReglement(id, reglementId, request.getMotif()));
    }
}
