package com.pharmacopee.ladafura.controllers;

import com.pharmacopee.ladafura.Models.Plante;
import com.pharmacopee.ladafura.services.interfaces.PlanteService;
import lombok.RequiredArgsConstructor;

import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/plantes")
@RequiredArgsConstructor
public class PlanteController {

    private final PlanteService planteService;

    // Récupérer toutes les plantes
    @GetMapping
    public ResponseEntity<List<Plante>> getAllPlantes() {
        return ResponseEntity.ok(
                planteService.getAllPlantes()
        );
    }

    // Récupérer une plante par son ID
    @GetMapping("/{id}")
    public ResponseEntity<Plante> getPlanteById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                planteService.getPlanteById(id)
        );
    }

    // Créer une plante
    @PostMapping
    public ResponseEntity<@Nullable Object> createPlante(
            @RequestBody Plante plante) {

        return ResponseEntity.status(HttpStatus.CREATED).body(planteService.createPlante(plante));
    }

    // Modifier une plante
    @PutMapping("/{id}")
    public ResponseEntity<Plante> updatePlante(
            @PathVariable Long id,
            @RequestBody Plante plante) {

        return ResponseEntity.ok(
                planteService.updatePlante(id, plante)
        );
    }

    // Supprimer une plante
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePlante(
            @PathVariable Long id) {

        planteService.deletePlante(id);

        return ResponseEntity.noContent().build();
    }
}