package com.pharmacopee.ladafura.controllers.pharmacopee;

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

import com.pharmacopee.ladafura.dto.pharmacopee.membre.CreerMembreRequest;
import com.pharmacopee.ladafura.dto.pharmacopee.membre.InviterMembreRequest;
import com.pharmacopee.ladafura.dto.pharmacopee.membre.MembreResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.membre.ModifierRolesMembreRequest;
import com.pharmacopee.ladafura.dto.pharmacopee.membre.TransfertProprieteRequest;
import com.pharmacopee.ladafura.enums.StatutMembre;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeMembreService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/pharmacopee/membres")
@RequiredArgsConstructor
@Tag(name = "Pharmacopée - Membres", description = "Équipe de la pharmacopée : invitations, rôles, suspensions, transfert de propriété")
@SecurityRequirement(name = "bearerAuth")
public class PharmacopeeMembreController {

    private final IPharmacopeeMembreService membreService;

    @GetMapping
    @Operation(summary = "Lister les membres", description = "Permission MEMBRES_CONSULTER. Filtre facultatif par statut.")
    @ApiResponse(responseCode = "200", description = "Liste paginée")
    @ApiResponse(responseCode = "403", description = "Permission manquante")
    public ResponseEntity<Page<MembreResponse>> getMembres(@RequestParam(required = false) StatutMembre statut,
                                                           @ParameterObject Pageable pageable) {
        return ResponseEntity.ok(membreService.getMembres(statut, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulter un membre", description = "Permission MEMBRES_CONSULTER.")
    @ApiResponse(responseCode = "200", description = "Détail du membre")
    @ApiResponse(responseCode = "404", description = "Membre introuvable dans cette pharmacopée")
    public ResponseEntity<MembreResponse> getMembre(@PathVariable Long id) {
        return ResponseEntity.ok(membreService.getMembre(id));
    }

    @PostMapping("/invitations")
    @Operation(summary = "Inviter un utilisateur existant",
               description = "Permission MEMBRES_INVITER. Le compte est trouvé par email ou téléphone exact ; il devient membre actif à l'acceptation.")
    @ApiResponse(responseCode = "201", description = "Invitation créée")
    @ApiResponse(responseCode = "404", description = "Aucun compte actif ne correspond")
    @ApiResponse(responseCode = "409", description = "Déjà membre ou invité, ou téléphone partagé par plusieurs comptes")
    public ResponseEntity<MembreResponse> inviter(@Valid @RequestBody InviterMembreRequest request) {
        return new ResponseEntity<>(membreService.inviter(request), HttpStatus.CREATED);
    }

    @PostMapping
    @Operation(summary = "Créer un compte et l'ajouter comme membre actif",
               description = "Permission MEMBRES_INVITER. Pour une personne sans compte LADAFURA. Soumis au quota d'utilisateurs actifs.")
    @ApiResponse(responseCode = "201", description = "Membre créé")
    @ApiResponse(responseCode = "409", description = "Email déjà utilisé")
    @ApiResponse(responseCode = "429", description = "Quota d'utilisateurs actifs atteint")
    public ResponseEntity<MembreResponse> creer(@Valid @RequestBody CreerMembreRequest request) {
        return new ResponseEntity<>(membreService.creer(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}/roles")
    @Operation(summary = "Remplacer les rôles d'un membre", description = "Permission MEMBRES_GERER. Le rôle de gérant n'est pas attribuable.")
    @ApiResponse(responseCode = "200", description = "Rôles mis à jour")
    @ApiResponse(responseCode = "403", description = "Permission manquante, rôle de gérant ou membre propriétaire")
    public ResponseEntity<MembreResponse> modifierRoles(@PathVariable Long id, @Valid @RequestBody ModifierRolesMembreRequest request) {
        return ResponseEntity.ok(membreService.modifierRoles(id, request));
    }

    @PatchMapping("/{id}/suspendre")
    @Operation(summary = "Suspendre un membre actif", description = "Permission MEMBRES_GERER.")
    @ApiResponse(responseCode = "200", description = "Membre suspendu")
    public ResponseEntity<MembreResponse> suspendre(@PathVariable Long id) {
        return ResponseEntity.ok(membreService.suspendre(id));
    }

    @PatchMapping("/{id}/reactiver")
    @Operation(summary = "Réactiver un membre suspendu", description = "Permission MEMBRES_GERER. Soumis au quota d'utilisateurs actifs.")
    @ApiResponse(responseCode = "200", description = "Membre réactivé")
    @ApiResponse(responseCode = "429", description = "Quota d'utilisateurs actifs atteint")
    public ResponseEntity<MembreResponse> reactiver(@PathVariable Long id) {
        return ResponseEntity.ok(membreService.reactiver(id));
    }

    @PatchMapping("/{id}/retirer")
    @Operation(summary = "Retirer un membre", description = "Permission MEMBRES_GERER. La ligne est conservée pour l'historique.")
    @ApiResponse(responseCode = "200", description = "Membre retiré")
    public ResponseEntity<MembreResponse> retirer(@PathVariable Long id) {
        return ResponseEntity.ok(membreService.retirer(id));
    }

    @PostMapping("/transfert-propriete")
    @Operation(summary = "Transférer la propriété de la pharmacopée", description = "Réservé au gérant propriétaire. Il devient manager.")
    @ApiResponse(responseCode = "200", description = "Propriété transférée")
    @ApiResponse(responseCode = "403", description = "Réservé au gérant propriétaire")
    public ResponseEntity<MembreResponse> transfererPropriete(@Valid @RequestBody TransfertProprieteRequest request) {
        return ResponseEntity.ok(membreService.transfererPropriete(request));
    }
}
