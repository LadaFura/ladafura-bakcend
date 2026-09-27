package com.pharmacopee.ladafura.controllers;

import com.pharmacopee.ladafura.Models.Source;
import com.pharmacopee.ladafura.services.interfaces.SourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sources")
@RequiredArgsConstructor
public class SourceController {

    private final SourceService sourceService;

    // Récupérer toutes les sources
    @GetMapping
    public ResponseEntity<List<Source>> getAllSources() {
        return ResponseEntity.ok(
                sourceService.getAllSources()
        );
    }

    // Récupérer une source par son ID
    @GetMapping("/{id}")
    public ResponseEntity<Source> getSourceById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                sourceService.getSourceById(id)
        );
    }

    // Créer une source
    @PostMapping
    public ResponseEntity<Source> createSource(
            @RequestBody Source source) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        sourceService.createSource(source)
                );
    }

    // Modifier une source
    @PutMapping("/{id}")
    public ResponseEntity<Source> updateSource(
            @PathVariable Long id,
            @RequestBody Source source) {

        return ResponseEntity.ok(
                sourceService.updateSource(id, source)
        );
    }

    // Supprimer une source
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSource(
            @PathVariable Long id) {

        sourceService.deleteSource(id);

        return ResponseEntity.noContent().build();
    }
}
