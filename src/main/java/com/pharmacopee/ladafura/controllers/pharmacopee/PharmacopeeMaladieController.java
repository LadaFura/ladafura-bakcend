package com.pharmacopee.ladafura.controllers.pharmacopee;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pharmacopee.ladafura.dto.pharmacopee.maladie.CreatePharmacopeeMaladieRequest;
import com.pharmacopee.ladafura.dto.pharmacopee.maladie.PharmacopeeMaladieDto;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeMaladieService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/pharmacopee/maladies")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class PharmacopeeMaladieController {

    private final IPharmacopeeMaladieService pharmacopeeMaladieService;

    @GetMapping
    public ResponseEntity<List<PharmacopeeMaladieDto>> getAllMaladies() {
        return ResponseEntity.ok(pharmacopeeMaladieService.getAllMaladies());
    }

    @GetMapping("/search")
    public ResponseEntity<List<PharmacopeeMaladieDto>> searchMaladies(@RequestParam String query) {
        return ResponseEntity.ok(pharmacopeeMaladieService.searchMaladies(query));
    }

    @PostMapping
    public ResponseEntity<PharmacopeeMaladieDto> createMaladie(
            @Valid @RequestBody CreatePharmacopeeMaladieRequest request) {
        return new ResponseEntity<>(pharmacopeeMaladieService.createMaladie(request), HttpStatus.CREATED);
    }
}

