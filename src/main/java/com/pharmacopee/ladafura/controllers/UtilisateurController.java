package com.pharmacopee.ladafura.controllers;

import com.pharmacopee.ladafura.Models.Utilisateur;
import com.pharmacopee.ladafura.services.interfaces.UtilisateurService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/utilisateurs")
@RequiredArgsConstructor
public class UtilisateurController {

    private final UtilisateurService utilisateurService;

    // Récupérer tous les utilisateurs
    @GetMapping
    public ResponseEntity<List<Utilisateur>> getAllUtilisateurs() {
        return ResponseEntity.ok(
                utilisateurService.getAllUtilisateurs()
        );
    }

    // Récupérer un utilisateur par son ID
    @GetMapping("/{id}")
    public ResponseEntity<Utilisateur> getUtilisateurById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                utilisateurService.getUtilisateurById(id)
        );
    }

    // Créer un utilisateur
    @PostMapping
    public ResponseEntity<Utilisateur> createUtilisateur(
            @RequestBody Utilisateur utilisateur) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        utilisateurService.createUtilisateur(utilisateur)
                );
    }

    // Modifier un utilisateur
    @PutMapping("/{id}")
    public ResponseEntity<Utilisateur> updateUtilisateur(
            @PathVariable Long id,
            @RequestBody Utilisateur utilisateur) {

        return ResponseEntity.ok(
                utilisateurService.updateUtilisateur(id, utilisateur)
        );
    }

    // Supprimer un utilisateur
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUtilisateur(
            @PathVariable Long id) {

        utilisateurService.deleteUtilisateur(id);

        return ResponseEntity.noContent().build();
    }
}
