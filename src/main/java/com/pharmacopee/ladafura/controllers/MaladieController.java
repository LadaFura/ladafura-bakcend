package com.pharmacopee.ladafura.controllers;

import com.pharmacopee.ladafura.Models.Maladie;
import com.pharmacopee.ladafura.services.interfaces.MaladieService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/maladies")
@RequiredArgsConstructor
public class MaladieController {

    private final MaladieService maladieService;

    // Récupérer toutes les maladies
    @GetMapping
    public ResponseEntity<List<Maladie>> getAllMaladies() {
        return ResponseEntity.ok(
                maladieService.getAllMaladies()
        );
    }

    // Récupérer une maladie par son ID
    @GetMapping("/{id}")
    public ResponseEntity<Maladie> getMaladieById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                maladieService.getMaladieById(id)
        );
    }

    // Créer une maladie
    @PostMapping
    public ResponseEntity<Maladie> createMaladie(
            @RequestBody Maladie maladie) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        maladieService.createMaladie(maladie)
                );
    }

    // Modifier une maladie
    @PutMapping("/{id}")
    public ResponseEntity<Maladie> updateMaladie(
            @PathVariable Long id,
            @RequestBody Maladie maladie) {

        return ResponseEntity.ok(
                maladieService.updateMaladie(id, maladie)
        );
    }

    // Supprimer une maladie
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMaladie(
            @PathVariable Long id) {

        maladieService.deleteMaladie(id);

        return ResponseEntity.noContent().build();
    }
}