package com.pharmacopee.ladafura.controllers.admin;

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

import com.pharmacopee.ladafura.dto.admin.user.AdminChangeStatusRequest;
import com.pharmacopee.ladafura.dto.admin.user.AdminCreateUserRequest;
import com.pharmacopee.ladafura.dto.admin.user.AdminUpdateUserRequest;
import com.pharmacopee.ladafura.dto.admin.user.AdminUserResponse;
import com.pharmacopee.ladafura.enums.Role;
import com.pharmacopee.ladafura.enums.StatutUtilisateur;
import com.pharmacopee.ladafura.services.interfaces.IAdminUserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMINISTRATEUR')")
@Tag(name = "Admin - Gestion des Utilisateurs", description = "Endpoints de gestion, modération et provisionnement des comptes utilisateurs (US-26)")
public class AdminUserController {

    private final IAdminUserService userService;

    @GetMapping
    @Operation(summary = "Lister les utilisateurs avec pagination et filtres", description = "Permet de filtrer par rôle (ex: AGENT_COLLECTE, PHARMACOPEE, POPULATION) et par statut (ACTIF, INACTIF, SUSPENDU).")
    @ApiResponse(responseCode = "200", description = "Liste paginée des utilisateurs récupérée avec succès")
    public ResponseEntity<Page<AdminUserResponse>> getAllUsers(
            @RequestParam(required = false) Role role,
            @RequestParam(required = false) StatutUtilisateur statut,
            @ParameterObject Pageable pageable) {
        return ResponseEntity.ok(userService.getAllUsers(role, statut, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtenir les détails d'un utilisateur", description = "Récupère les informations complètes d'un utilisateur ainsi que son profil métier le cas échéant.")
    @ApiResponse(responseCode = "200", description = "Détails de l'utilisateur")
    @ApiResponse(responseCode = "404", description = "Utilisateur introuvable")
    public ResponseEntity<AdminUserResponse> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PostMapping
    @Operation(summary = "Créer un utilisateur", description = "Provisionne un utilisateur dans Firebase Authentication et l'enregistre en base MySQL avec son rôle.")
    @ApiResponse(responseCode = "201", description = "Utilisateur créé avec succès")
    @ApiResponse(responseCode = "400", description = "Données d'entrée invalides")
    @ApiResponse(responseCode = "409", description = "Conflit - Adresse email déjà utilisée")
    public ResponseEntity<AdminUserResponse> createUser(@Valid @RequestBody AdminCreateUserRequest request) {
        AdminUserResponse response = userService.createUser(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Mettre à jour un utilisateur", description = "Modifie les coordonnées, le rôle ou le statut d'un utilisateur.")
    @ApiResponse(responseCode = "200", description = "Utilisateur mis à jour avec succès")
    @ApiResponse(responseCode = "404", description = "Utilisateur introuvable")
    public ResponseEntity<AdminUserResponse> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody AdminUpdateUserRequest request) {
        return ResponseEntity.ok(userService.updateUser(id, request));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Modifier le statut d'un compte utilisateur", description = "Active, suspend ou désactive un compte, synchronisé avec Firebase Authentication.")
    @ApiResponse(responseCode = "200", description = "Statut mis à jour avec succès")
    @ApiResponse(responseCode = "404", description = "Utilisateur introuvable")
    public ResponseEntity<AdminUserResponse> changeUserStatus(
            @PathVariable Long id,
            @Valid @RequestBody AdminChangeStatusRequest request) {
        return ResponseEntity.ok(userService.changeUserStatus(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un utilisateur", description = "Supprime définitivement le compte utilisateur dans MySQL et dans Firebase Authentication.")
    @ApiResponse(responseCode = "204", description = "Utilisateur supprimé avec succès")
    @ApiResponse(responseCode = "404", description = "Utilisateur introuvable")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
