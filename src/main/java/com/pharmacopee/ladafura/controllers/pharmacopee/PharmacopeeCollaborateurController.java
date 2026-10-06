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
import org.springframework.web.bind.annotation.RestController;

import com.pharmacopee.ladafura.dto.pharmacopee.collaborateur.CreateCollaborateurRequest;
import com.pharmacopee.ladafura.dto.pharmacopee.collaborateur.PharmacopeeCollaborateurResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.collaborateur.UpdateCollaborateurRequest;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeCollaborateurService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/pharmacopee/collaborateurs")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Pharmacopée - Collaborateurs", description = "Endpoints de gestion du personnel de la pharmacopée")
@SecurityRequirement(name = "bearerAuth")
public class PharmacopeeCollaborateurController {

    private final IPharmacopeeCollaborateurService collaborateurService;

    @GetMapping
    @Operation(summary = "Lister les collaborateurs de la pharmacopée active",
               description = "Renvoie la liste paginée des employés/collaborateurs rattachés à cette officine.")
    public ResponseEntity<Page<PharmacopeeCollaborateurResponse>> getCollaborateurs(@ParameterObject Pageable pageable) {
        log.info("Récupération de la liste des collaborateurs");
        return ResponseEntity.ok(collaborateurService.getCollaborateurs(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulter le détail d'un collaborateur")
    public ResponseEntity<PharmacopeeCollaborateurResponse> getCollaborateurById(@PathVariable Long id) {
        return ResponseEntity.ok(collaborateurService.getCollaborateurById(id));
    }

    @PostMapping
    @Operation(summary = "Ajouter un nouveau collaborateur à l'officine",
               description = "Créé un compte praticien collaborateur rattaché à la pharmacopée. Réservé au praticien principal.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Collaborateur ajouté avec succès"),
        @ApiResponse(responseCode = "400", description = "Données invalides ou email déjà existant"),
        @ApiResponse(responseCode = "403", description = "Opération réservée au praticien principal")
    })
    public ResponseEntity<PharmacopeeCollaborateurResponse> addCollaborateur(@Valid @RequestBody CreateCollaborateurRequest request) {
        log.info("Ajout d'un collaborateur: {}", request.getEmail());
        PharmacopeeCollaborateurResponse response = collaborateurService.addCollaborateur(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Mettre à jour les informations d'un collaborateur",
               description = "Permet de modifier le nom, téléphone, spécialité ou le statut (ACTIF/INACTIF). Réservé au praticien principal.")
    public ResponseEntity<PharmacopeeCollaborateurResponse> updateCollaborateur(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCollaborateurRequest request) {
        log.info("Mise à jour du collaborateur ID: {}", id);
        return ResponseEntity.ok(collaborateurService.updateCollaborateur(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer ou détacher un collaborateur",
               description = "Retire le collaborateur de cette officine. S'il n'est rattaché à aucune autre officine, son compte est supprimé. Réservé au praticien principal.")
    public ResponseEntity<Void> deleteCollaborateur(@PathVariable Long id) {
        log.info("Suppression du collaborateur ID: {}", id);
        collaborateurService.deleteCollaborateur(id);
        return ResponseEntity.noContent().build();
    }
}
