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
import org.springframework.web.bind.annotation.RestController;

import com.pharmacopee.ladafura.dto.pharmacopee.abonnement.MotifRequest;
import com.pharmacopee.ladafura.dto.pharmacopee.caisse.CaisseResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.caisse.CloturerSessionRequest;
import com.pharmacopee.ladafura.dto.pharmacopee.caisse.EnregistrerCaisseRequest;
import com.pharmacopee.ladafura.dto.pharmacopee.caisse.OperationCaisseResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.caisse.OperationManuelleRequest;
import com.pharmacopee.ladafura.dto.pharmacopee.caisse.OuvrirSessionRequest;
import com.pharmacopee.ladafura.dto.pharmacopee.caisse.SessionCaisseResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.caisse.TransfertCaisseRequest;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeCaisseService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/pharmacopee/caisses")
@RequiredArgsConstructor
@Tag(name = "Pharmacopée - Caisses", description = "Caisses et comptes, sessions, opérations et transferts")
@SecurityRequirement(name = "bearerAuth")
public class PharmacopeeCaisseController {

    private final IPharmacopeeCaisseService caisseService;

    @GetMapping
    @Operation(summary = "Lister les caisses et comptes", description = "Permission CAISSE_CONSULTER. Avec solde théorique et session ouverte.")
    @ApiResponse(responseCode = "200", description = "Caisses")
    public ResponseEntity<List<CaisseResponse>> getCaisses() {
        return ResponseEntity.ok(caisseService.getCaisses());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulter une caisse", description = "Permission CAISSE_CONSULTER.")
    @ApiResponse(responseCode = "200", description = "Caisse")
    @ApiResponse(responseCode = "404", description = "Caisse introuvable dans cette pharmacopée")
    public ResponseEntity<CaisseResponse> getCaisse(@PathVariable Long id) {
        return ResponseEntity.ok(caisseService.getCaisse(id));
    }

    @PostMapping
    @Operation(summary = "Créer une caisse ou un compte", description = "Permission CAISSE_GERER.")
    @ApiResponse(responseCode = "201", description = "Caisse créée")
    public ResponseEntity<CaisseResponse> creer(@Valid @RequestBody EnregistrerCaisseRequest request) {
        return new ResponseEntity<>(caisseService.creer(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier une caisse", description = "Permission CAISSE_GERER. Le type n'est pas modifiable.")
    @ApiResponse(responseCode = "200", description = "Caisse modifiée")
    public ResponseEntity<CaisseResponse> modifier(@PathVariable Long id, @Valid @RequestBody EnregistrerCaisseRequest request) {
        return ResponseEntity.ok(caisseService.modifier(id, request));
    }

    @PatchMapping("/{id}/desactiver")
    @Operation(summary = "Désactiver une caisse", description = "Permission CAISSE_GERER.")
    @ApiResponse(responseCode = "200", description = "Caisse désactivée")
    @ApiResponse(responseCode = "409", description = "Une session est ouverte")
    public ResponseEntity<CaisseResponse> desactiver(@PathVariable Long id) {
        return ResponseEntity.ok(caisseService.changerActivation(id, false));
    }

    @PatchMapping("/{id}/reactiver")
    @Operation(summary = "Réactiver une caisse", description = "Permission CAISSE_GERER.")
    @ApiResponse(responseCode = "200", description = "Caisse réactivée")
    public ResponseEntity<CaisseResponse> reactiver(@PathVariable Long id) {
        return ResponseEntity.ok(caisseService.changerActivation(id, true));
    }

    @GetMapping("/{id}/sessions")
    @Operation(summary = "Historique des sessions", description = "Permission CAISSE_CONSULTER.")
    @ApiResponse(responseCode = "200", description = "Liste paginée")
    public ResponseEntity<Page<SessionCaisseResponse>> getSessions(@PathVariable Long id, @ParameterObject Pageable pageable) {
        return ResponseEntity.ok(caisseService.getSessions(id, pageable));
    }

    @PostMapping("/{id}/sessions")
    @Operation(summary = "Ouvrir une session", description = "Permission CAISSE_OUVRIR_FERMER. Le fond reprend par défaut le montant de la dernière clôture.")
    @ApiResponse(responseCode = "201", description = "Session ouverte")
    @ApiResponse(responseCode = "400", description = "Caisse non physique, ou fond différent sans commentaire")
    @ApiResponse(responseCode = "409", description = "Une session est déjà ouverte")
    public ResponseEntity<SessionCaisseResponse> ouvrirSession(@PathVariable Long id, @Valid @RequestBody OuvrirSessionRequest request) {
        return new ResponseEntity<>(caisseService.ouvrirSession(id, request), HttpStatus.CREATED);
    }

    @PostMapping("/{id}/sessions/courante/cloture")
    @Operation(summary = "Clôturer la session ouverte", description = "Permission CAISSE_OUVRIR_FERMER. Un écart doit être justifié.")
    @ApiResponse(responseCode = "200", description = "Session clôturée")
    @ApiResponse(responseCode = "400", description = "Écart sans commentaire")
    @ApiResponse(responseCode = "409", description = "Aucune session ouverte")
    public ResponseEntity<SessionCaisseResponse> cloturerSession(@PathVariable Long id, @Valid @RequestBody CloturerSessionRequest request) {
        return ResponseEntity.ok(caisseService.cloturerSession(id, request));
    }

    @GetMapping("/{id}/operations")
    @Operation(summary = "Journal des opérations", description = "Permission CAISSE_CONSULTER.")
    @ApiResponse(responseCode = "200", description = "Liste paginée")
    public ResponseEntity<Page<OperationCaisseResponse>> getOperations(@PathVariable Long id, @ParameterObject Pageable pageable) {
        return ResponseEntity.ok(caisseService.getOperations(id, pageable));
    }

    @PostMapping("/{id}/operations")
    @Operation(summary = "Saisir un apport, un retrait ou une recette diverse", description = "Permission CAISSE_GERER.")
    @ApiResponse(responseCode = "201", description = "Opération enregistrée")
    @ApiResponse(responseCode = "400", description = "Nature non saisissable")
    @ApiResponse(responseCode = "409", description = "Session fermée ou solde insuffisant")
    public ResponseEntity<OperationCaisseResponse> enregistrerOperation(@PathVariable Long id, @Valid @RequestBody OperationManuelleRequest request) {
        return new ResponseEntity<>(caisseService.enregistrerOperation(id, request), HttpStatus.CREATED);
    }

    @PostMapping("/transferts")
    @Operation(summary = "Transférer entre deux caisses", description = "Permission CAISSE_GERER.")
    @ApiResponse(responseCode = "201", description = "Transfert enregistré : sortie puis entrée")
    @ApiResponse(responseCode = "400", description = "Caisses identiques")
    public ResponseEntity<List<OperationCaisseResponse>> transferer(@Valid @RequestBody TransfertCaisseRequest request) {
        return new ResponseEntity<>(caisseService.transferer(request), HttpStatus.CREATED);
    }

    @PatchMapping("/operations/{id}/annuler")
    @Operation(summary = "Annuler une opération manuelle", description = "Permission CAISSE_GERER. Contre-passation.")
    @ApiResponse(responseCode = "200", description = "Contre-passation enregistrée")
    @ApiResponse(responseCode = "409", description = "Opération non annulable ici")
    public ResponseEntity<OperationCaisseResponse> annulerOperation(@PathVariable Long id, @Valid @RequestBody MotifRequest request) {
        return ResponseEntity.ok(caisseService.annulerOperation(id, request.getMotif()));
    }
}
