package com.pharmacopee.ladafura.controllers;

import java.util.List;

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

import com.pharmacopee.ladafura.Models.Plante;
import com.pharmacopee.ladafura.dto.requestes.PlanteDtoRequeste;
import com.pharmacopee.ladafura.dto.responses.PlanteDtoResponse;
import com.pharmacopee.ladafura.services.interfaces.PlanteService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/plantes")
@RequiredArgsConstructor
public class PlanteController {

    private final PlanteService planteService;

    // Récupérer toutes les plantes
    @GetMapping
    public ResponseEntity<List<PlanteDtoResponse>> getAllPlantes() {
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
    public ResponseEntity<PlanteDtoResponse> createPlante(
            @RequestBody PlanteDtoRequeste plante) {

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