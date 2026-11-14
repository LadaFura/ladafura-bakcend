package com.pharmacopee.ladafura.controllers.pharmacopee;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pharmacopee.ladafura.dto.pharmacopee.membre.InvitationResponse;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeInvitationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/pharmacopee/invitations")
@RequiredArgsConstructor
@Tag(name = "Pharmacopée - Invitations", description = "Invitations reçues par l'utilisateur connecté")
@SecurityRequirement(name = "bearerAuth")
public class PharmacopeeInvitationController {

    private final IPharmacopeeInvitationService invitationService;

    @GetMapping
    @Operation(summary = "Lister mes invitations en cours")
    @ApiResponse(responseCode = "200", description = "Invitations non expirées")
    public ResponseEntity<List<InvitationResponse>> getMesInvitations() {
        return ResponseEntity.ok(invitationService.getMesInvitations());
    }

    @PostMapping("/{id}/accepter")
    @Operation(summary = "Accepter une invitation", description = "Soumis au quota d'utilisateurs actifs de la pharmacopée.")
    @ApiResponse(responseCode = "204", description = "Invitation acceptée")
    @ApiResponse(responseCode = "404", description = "Invitation introuvable pour cet utilisateur")
    @ApiResponse(responseCode = "409", description = "Invitation expirée")
    @ApiResponse(responseCode = "429", description = "Quota d'utilisateurs actifs atteint")
    public ResponseEntity<Void> accepter(@PathVariable Long id) {
        invitationService.accepter(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/refuser")
    @Operation(summary = "Refuser une invitation")
    @ApiResponse(responseCode = "204", description = "Invitation refusée")
    @ApiResponse(responseCode = "404", description = "Invitation introuvable pour cet utilisateur")
    public ResponseEntity<Void> refuser(@PathVariable Long id) {
        invitationService.refuser(id);
        return ResponseEntity.noContent().build();
    }
}
