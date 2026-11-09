package com.pharmacopee.ladafura.controllers.admin;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pharmacopee.ladafura.dto.admin.abonnement.ModifierParametreRequest;
import com.pharmacopee.ladafura.dto.admin.abonnement.ParametreResponse;
import com.pharmacopee.ladafura.services.interfaces.IAdminParametreService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/admin/parametres")
@RequiredArgsConstructor
@Tag(name = "Admin - Paramètres", description = "Paramètres de la plateforme")
@SecurityRequirement(name = "bearerAuth")
public class AdminParametreController {

    private final IAdminParametreService parametreService;

    @GetMapping
    @Operation(summary = "Lister les paramètres")
    @ApiResponse(responseCode = "200", description = "Paramètres")
    public ResponseEntity<List<ParametreResponse>> getParametres() {
        return ResponseEntity.ok(parametreService.getParametres());
    }

    @PutMapping("/{code}")
    @Operation(summary = "Modifier un paramètre")
    @ApiResponse(responseCode = "200", description = "Paramètre modifié")
    @ApiResponse(responseCode = "400", description = "Valeur invalide pour ce type")
    @ApiResponse(responseCode = "404", description = "Paramètre inconnu")
    public ResponseEntity<ParametreResponse> modifier(@PathVariable String code, @Valid @RequestBody ModifierParametreRequest request) {
        return ResponseEntity.ok(parametreService.modifier(code, request.getValeur()));
    }
}
