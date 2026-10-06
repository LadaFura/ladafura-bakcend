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

import com.pharmacopee.ladafura.dto.pharmacopee.plante.PharmacopeePlanteDto;
import com.pharmacopee.ladafura.dto.pharmacopee.plante.CreatePharmacopeePlanteRequest;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeePlanteService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/pharmacopee/plantes")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class PharmacopeePlanteController {

    private final IPharmacopeePlanteService pharmacopeePlanteService;

    @GetMapping("/search")
    public ResponseEntity<List<PharmacopeePlanteDto>> searchPlantes(@RequestParam String query) {
        return ResponseEntity.ok(pharmacopeePlanteService.searchPlantes(query));
    }

    @PostMapping
    public ResponseEntity<PharmacopeePlanteDto> createPlante(@Valid @RequestBody CreatePharmacopeePlanteRequest request) {
        return new ResponseEntity<>(pharmacopeePlanteService.createPlante(request), HttpStatus.CREATED);
    }
}
