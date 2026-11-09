package com.pharmacopee.ladafura.controllers.pharmacopee;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pharmacopee.ladafura.dto.pharmacopee.client.ClientResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.client.EnregistrerClientRequest;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeClientService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/pharmacopee/clients")
@RequiredArgsConstructor
@Tag(name = "Pharmacopée - Clients", description = "Clients de la pharmacopée et leur encours")
@SecurityRequirement(name = "bearerAuth")
public class PharmacopeeClientController {

    private final IPharmacopeeClientService clientService;

    @GetMapping
    @Operation(summary = "Lister les clients", description = "Permission CLIENTS_CONSULTER. Recherche facultative sur le nom ou le téléphone.")
    @ApiResponse(responseCode = "200", description = "Liste paginée")
    public ResponseEntity<Page<ClientResponse>> getClients(@RequestParam(required = false) String recherche, @ParameterObject Pageable pageable) {
        return ResponseEntity.ok(clientService.getClients(recherche, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulter un client", description = "Permission CLIENTS_CONSULTER. Avec son encours.")
    @ApiResponse(responseCode = "200", description = "Client")
    @ApiResponse(responseCode = "404", description = "Client introuvable dans cette pharmacopée")
    public ResponseEntity<ClientResponse> getClient(@PathVariable Long id) {
        return ResponseEntity.ok(clientService.getClient(id));
    }

    @PostMapping
    @Operation(summary = "Créer un client", description = "Permission CLIENTS_GERER.")
    @ApiResponse(responseCode = "201", description = "Client créé")
    @ApiResponse(responseCode = "409", description = "Téléphone déjà utilisé")
    public ResponseEntity<ClientResponse> creer(@Valid @RequestBody EnregistrerClientRequest request) {
        return new ResponseEntity<>(clientService.creer(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier un client", description = "Permission CLIENTS_GERER.")
    @ApiResponse(responseCode = "200", description = "Client modifié")
    @ApiResponse(responseCode = "409", description = "Téléphone déjà utilisé")
    public ResponseEntity<ClientResponse> modifier(@PathVariable Long id, @Valid @RequestBody EnregistrerClientRequest request) {
        return ResponseEntity.ok(clientService.modifier(id, request));
    }

    @PatchMapping("/{id}/desactiver")
    @Operation(summary = "Désactiver un client", description = "Permission CLIENTS_GERER.")
    @ApiResponse(responseCode = "200", description = "Client désactivé")
    public ResponseEntity<ClientResponse> desactiver(@PathVariable Long id) {
        return ResponseEntity.ok(clientService.changerActivation(id, false));
    }

    @PatchMapping("/{id}/reactiver")
    @Operation(summary = "Réactiver un client", description = "Permission CLIENTS_GERER.")
    @ApiResponse(responseCode = "200", description = "Client réactivé")
    public ResponseEntity<ClientResponse> reactiver(@PathVariable Long id) {
        return ResponseEntity.ok(clientService.changerActivation(id, true));
    }
}
