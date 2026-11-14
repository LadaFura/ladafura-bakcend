package com.pharmacopee.ladafura.controllers.pharmacopee;

import java.util.List;

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
import org.springframework.web.bind.annotation.RestController;

import com.pharmacopee.ladafura.dto.pharmacopee.role.EnregistrerRoleRequest;
import com.pharmacopee.ladafura.dto.pharmacopee.role.PermissionResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.role.RoleResponse;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeRoleService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/pharmacopee/roles")
@RequiredArgsConstructor
@Tag(name = "Pharmacopée - Rôles", description = "Rôles système et rôles personnalisés de la pharmacopée")
@SecurityRequirement(name = "bearerAuth")
public class PharmacopeeRoleController {

    private final IPharmacopeeRoleService roleService;

    @GetMapping
    @Operation(summary = "Lister les rôles utilisables", description = "Permission MEMBRES_CONSULTER.")
    @ApiResponse(responseCode = "200", description = "Rôles système et personnalisés actifs")
    public ResponseEntity<List<RoleResponse>> getRoles() {
        return ResponseEntity.ok(roleService.getRoles());
    }

    @GetMapping("/permissions")
    @Operation(summary = "Lister les permissions attribuables", description = "Permission MEMBRES_CONSULTER. Portée PHARMACOPEE uniquement.")
    @ApiResponse(responseCode = "200", description = "Catalogue des permissions")
    public ResponseEntity<List<PermissionResponse>> getPermissions() {
        return ResponseEntity.ok(roleService.getPermissions());
    }

    @PostMapping
    @Operation(summary = "Créer un rôle personnalisé", description = "Permission ROLES_GERER.")
    @ApiResponse(responseCode = "201", description = "Rôle créé")
    @ApiResponse(responseCode = "400", description = "Permission inconnue ou de portée plateforme")
    @ApiResponse(responseCode = "409", description = "Un rôle porte déjà ce nom")
    public ResponseEntity<RoleResponse> creer(@Valid @RequestBody EnregistrerRoleRequest request) {
        return new ResponseEntity<>(roleService.creer(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier un rôle personnalisé", description = "Permission ROLES_GERER. Les rôles système sont en lecture seule.")
    @ApiResponse(responseCode = "200", description = "Rôle modifié")
    @ApiResponse(responseCode = "403", description = "Rôle système ou permission manquante")
    @ApiResponse(responseCode = "404", description = "Rôle introuvable dans cette pharmacopée")
    public ResponseEntity<RoleResponse> modifier(@PathVariable Long id, @Valid @RequestBody EnregistrerRoleRequest request) {
        return ResponseEntity.ok(roleService.modifier(id, request));
    }

    @PatchMapping("/{id}/desactiver")
    @Operation(summary = "Désactiver un rôle personnalisé", description = "Permission ROLES_GERER.")
    @ApiResponse(responseCode = "200", description = "Rôle désactivé")
    public ResponseEntity<RoleResponse> desactiver(@PathVariable Long id) {
        return ResponseEntity.ok(roleService.desactiver(id));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un rôle personnalisé non attribué", description = "Permission ROLES_GERER.")
    @ApiResponse(responseCode = "204", description = "Rôle supprimé")
    @ApiResponse(responseCode = "409", description = "Rôle attribué à des membres")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        roleService.supprimer(id);
        return ResponseEntity.noContent().build();
    }
}
