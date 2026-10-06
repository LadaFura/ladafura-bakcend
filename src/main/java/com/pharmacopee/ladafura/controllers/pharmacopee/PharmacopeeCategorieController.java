package com.pharmacopee.ladafura.controllers.pharmacopee;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pharmacopee.ladafura.dto.pharmacopee.produit.CategorieResponse;
import com.pharmacopee.ladafura.repository.CategorieProduitRepository;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/pharmacopee/categories")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class PharmacopeeCategorieController {

    private final CategorieProduitRepository categorieRepository;

    @GetMapping
    public ResponseEntity<List<CategorieResponse>> getCategories() {
        return ResponseEntity.ok(categorieRepository.findByStatutTrue().stream()
                .map(c -> CategorieResponse.builder().id(c.getId()).nom(c.getNom()).build())
                .collect(Collectors.toList()));
    }
}
