package com.pharmacopee.ladafura.controllers.agent;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pharmacopee.ladafura.Models.Maladie;
import com.pharmacopee.ladafura.dto.agent.maladie.AgentCreateMaladieRequest;
import com.pharmacopee.ladafura.dto.agent.maladie.AgentMaladieResponse;
import com.pharmacopee.ladafura.repository.MaladieRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/agent/maladies")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasAuthority('ROLE_AGENT_COLLECTE')")
@Tag(name = "Agent de Collecte - Maladies & Symptômes", description = "Endpoints de recherche et d'enregistrement des maladies et symptômes associés aux plantes")
@SecurityRequirement(name = "bearerAuth")
public class AgentMaladieController {

    private final MaladieRepository maladieRepository;

    @GetMapping
    @Operation(summary = "Rechercher ou lister les maladies et symptômes",
               description = "Permet à l'agent de chercher des maladies par nom pour l'autocomplétion ou de récupérer la liste complète.")
    @ApiResponse(responseCode = "200", description = "Liste des maladies trouvées")
    public ResponseEntity<List<AgentMaladieResponse>> searchMaladies(
            @RequestParam(required = false) String search) {
        log.info("Recherche de maladies par l'agent (search='{}')", search);
        List<Maladie> list;
        if (search != null && !search.trim().isEmpty()) {
            list = maladieRepository.findByNomContainingIgnoreCase(search.trim());
        } else {
            list = maladieRepository.findAll();
        }

        List<AgentMaladieResponse> responses = list.stream()
                .map(m -> AgentMaladieResponse.builder()
                        .id(m.getId())
                        .nom(m.getNom())
                        .description(m.getDescription())
                        .build())
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @PostMapping
    @Operation(summary = "Ajouter une nouvelle maladie / symptôme",
               description = "Enregistre une nouvelle maladie ou catégorie de symptômes avec contrôle anti-doublon strict insensible à la casse.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Maladie créée avec succès"),
        @ApiResponse(responseCode = "400", description = "Données invalides"),
        @ApiResponse(responseCode = "409", description = "Une maladie portant ce nom existe déjà")
    })
    public ResponseEntity<AgentMaladieResponse> createMaladie(
            @Valid @RequestBody AgentCreateMaladieRequest request) {
        final String nomNettoye = request.getNom().trim();
        log.info("Création d'une nouvelle maladie par l'agent : '{}'", nomNettoye);

        var existante = maladieRepository.findByNomIgnoreCase(nomNettoye);
        if (existante.isPresent()) {
            log.info("La maladie '{}' existe déjà (ID: {}). Renvoi de l'entité existante.", nomNettoye, existante.get().getId());
            Maladie m = existante.get();
            return ResponseEntity.ok(AgentMaladieResponse.builder()
                    .id(m.getId())
                    .nom(m.getNom())
                    .description(m.getDescription())
                    .build());
        }

        Maladie nouvelle = Maladie.builder()
                .nom(nomNettoye)
                .description(request.getDescription() != null ? request.getDescription().trim() : null)
                .build();

        Maladie saved = maladieRepository.save(nouvelle);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                AgentMaladieResponse.builder()
                        .id(saved.getId())
                        .nom(saved.getNom())
                        .description(saved.getDescription())
                        .build()
        );
    }
}

