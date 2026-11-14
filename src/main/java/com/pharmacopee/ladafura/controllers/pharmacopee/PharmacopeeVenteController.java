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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pharmacopee.ladafura.dto.pharmacopee.abonnement.MotifRequest;
import com.pharmacopee.ladafura.dto.pharmacopee.vente.EnregistrerVenteRequest;
import com.pharmacopee.ladafura.dto.pharmacopee.vente.PaiementVenteRequest;
import com.pharmacopee.ladafura.dto.pharmacopee.vente.PaiementVenteResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.vente.ValiderVenteRequest;
import com.pharmacopee.ladafura.dto.pharmacopee.vente.VenteResponse;
import com.pharmacopee.ladafura.enums.StatutVente;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeVenteService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/pharmacopee/ventes")
@RequiredArgsConstructor
@Tag(name = "Pharmacopée - Ventes", description = "Ventes au comptoir, paiements et crédits clients")
@SecurityRequirement(name = "bearerAuth")
public class PharmacopeeVenteController {

    private final IPharmacopeeVenteService venteService;

    @GetMapping
    @Operation(summary = "Lister les ventes", description = "Permission VENTES_CONSULTER. Filtres facultatifs.")
    @ApiResponse(responseCode = "200", description = "Liste paginée")
    public ResponseEntity<Page<VenteResponse>> getVentes(@RequestParam(required = false) StatutVente statut,
                                                         @RequestParam(required = false) Long clientId,
                                                         @ParameterObject Pageable pageable) {
        return ResponseEntity.ok(venteService.getVentes(statut, clientId, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulter une vente", description = "Permission VENTES_CONSULTER. Avec lignes, montant payé et reste à payer.")
    @ApiResponse(responseCode = "200", description = "Vente")
    @ApiResponse(responseCode = "404", description = "Vente introuvable dans cette pharmacopée")
    public ResponseEntity<VenteResponse> getVente(@PathVariable Long id) {
        return ResponseEntity.ok(venteService.getVente(id));
    }

    @PostMapping
    @Operation(summary = "Créer une vente en attente", description = "Permission VENTES_CREER. Aucun effet sur le stock ni sur la caisse.")
    @ApiResponse(responseCode = "201", description = "Vente en attente créée")
    @ApiResponse(responseCode = "403", description = "Remise sans la permission VENTES_REMISE")
    public ResponseEntity<VenteResponse> creer(@Valid @RequestBody EnregistrerVenteRequest request) {
        return new ResponseEntity<>(venteService.creer(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier une vente en attente", description = "Permission VENTES_CREER. Les lignes sont remplacées.")
    @ApiResponse(responseCode = "200", description = "Vente modifiée")
    @ApiResponse(responseCode = "409", description = "La vente n'est plus en attente")
    public ResponseEntity<VenteResponse> modifier(@PathVariable Long id, @Valid @RequestBody EnregistrerVenteRequest request) {
        return ResponseEntity.ok(venteService.modifier(id, request));
    }

    @PatchMapping("/{id}/abandonner")
    @Operation(summary = "Abandonner une vente en attente", description = "Permission VENTES_CREER.")
    @ApiResponse(responseCode = "200", description = "Vente abandonnée")
    @ApiResponse(responseCode = "409", description = "La vente n'est plus en attente")
    public ResponseEntity<VenteResponse> abandonner(@PathVariable Long id) {
        return ResponseEntity.ok(venteService.abandonner(id));
    }

    @PostMapping("/{id}/valider")
    @Operation(summary = "Valider une vente", description = "Permission VENTES_CREER, et PAIEMENTS_ENCAISSER si des paiements sont fournis. Sort le stock et fige les prix.")
    @ApiResponse(responseCode = "200", description = "Vente validée")
    @ApiResponse(responseCode = "400", description = "Vente vide, trop-perçu, ou crédit sans client")
    @ApiResponse(responseCode = "409", description = "Stock insuffisant ou session de caisse fermée")
    @ApiResponse(responseCode = "429", description = "Quota mensuel de ventes atteint")
    public ResponseEntity<VenteResponse> valider(@PathVariable Long id, @Valid @RequestBody(required = false) ValiderVenteRequest request) {
        return ResponseEntity.ok(venteService.valider(id, request));
    }

    @PatchMapping("/{id}/annuler")
    @Operation(summary = "Annuler une vente validée", description = "Permission VENTES_ANNULER. Le stock revient et les paiements sont contre-passés.")
    @ApiResponse(responseCode = "200", description = "Vente annulée")
    @ApiResponse(responseCode = "409", description = "Vente non validée, ou session de caisse fermée pour un remboursement")
    public ResponseEntity<VenteResponse> annuler(@PathVariable Long id, @Valid @RequestBody MotifRequest request) {
        return ResponseEntity.ok(venteService.annuler(id, request.getMotif()));
    }

    @GetMapping("/{id}/paiements")
    @Operation(summary = "Paiements d'une vente", description = "Permission VENTES_CONSULTER.")
    @ApiResponse(responseCode = "200", description = "Paiements")
    public ResponseEntity<List<PaiementVenteResponse>> getPaiements(@PathVariable Long id) {
        return ResponseEntity.ok(venteService.getPaiements(id));
    }

    @PostMapping("/{id}/paiements")
    @Operation(summary = "Enregistrer un paiement", description = "Permission PAIEMENTS_ENCAISSER. Solde une dette client.")
    @ApiResponse(responseCode = "201", description = "Paiement enregistré")
    @ApiResponse(responseCode = "400", description = "Montant supérieur au reste à payer")
    @ApiResponse(responseCode = "409", description = "Vente non validée ou session de caisse fermée")
    public ResponseEntity<PaiementVenteResponse> enregistrerPaiement(@PathVariable Long id, @Valid @RequestBody PaiementVenteRequest request) {
        return new ResponseEntity<>(venteService.enregistrerPaiement(id, request), HttpStatus.CREATED);
    }

    @PatchMapping("/{id}/paiements/{paiementId}/annuler")
    @Operation(summary = "Annuler un paiement", description = "Permission VENTES_ANNULER. Contre-passation en caisse.")
    @ApiResponse(responseCode = "200", description = "Paiement annulé")
    @ApiResponse(responseCode = "409", description = "Paiement déjà annulé")
    public ResponseEntity<PaiementVenteResponse> annulerPaiement(@PathVariable Long id, @PathVariable Long paiementId, @Valid @RequestBody MotifRequest request) {
        return ResponseEntity.ok(venteService.annulerPaiement(id, paiementId, request.getMotif()));
    }
}
