package com.pharmacopee.ladafura.controllers.agent;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.pharmacopee.ladafura.dto.agent.media.AgentMediaResponse;
import com.pharmacopee.ladafura.dto.agent.media.AgentMediaUpdateRequest;
import com.pharmacopee.ladafura.services.interfaces.IAgentMediaService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/agent/collectes/{collecteId}/medias")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasAuthority('ROLE_AGENT_COLLECTE')")
@Tag(name = "Agent - Médias de Collecte", description = "Endpoints réservés aux agents de collecte pour associer, téléverser et gérer les photos d'échantillons et enregistrements audio de témoignages oraux. Les fichiers lourds sont stockés en dehors de MySQL.")
public class AgentMediaController {

    private final IAgentMediaService agentMediaService;

    @PostMapping(value = "/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Téléverser une photo d'échantillon ou de plante",
               description = "Téléverse un fichier image (JPEG, PNG, WebP jusqu'à 10 Mo) et l'associe à la fiche de collecte. Stocke le fichier sur le système de stockage sécurisé et conserve l'URL dans la collecte.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Photo téléversée et associée avec succès"),
        @ApiResponse(responseCode = "400", description = "Fichier invalide, format non supporté ou collecte verrouillée"),
        @ApiResponse(responseCode = "403", description = "Collecte n'appartenant pas à l'agent"),
        @ApiResponse(responseCode = "404", description = "Collecte introuvable")
    })
    public ResponseEntity<AgentMediaResponse> uploadPhoto(
            @PathVariable Long collecteId,
            @Parameter(description = "Fichier image à téléverser", required = true)
            @RequestParam("file") MultipartFile file) {
        log.info("Téléversement de photo pour la collecte ID {}", collecteId);
        AgentMediaResponse response = agentMediaService.uploadPhoto(collecteId, file);
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/audio", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Téléverser un enregistrement audio de témoignage oral",
               description = "Téléverse un fichier audio (MP3, WAV, OGG, M4A, AAC jusqu'à 25 Mo) de témoignage ou de description orale en langue locale et l'associe à la fiche de collecte.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Enregistrement audio téléversé et associé avec succès"),
        @ApiResponse(responseCode = "400", description = "Fichier audio invalide ou collecte verrouillée"),
        @ApiResponse(responseCode = "403", description = "Collecte n'appartenant pas à l'agent"),
        @ApiResponse(responseCode = "404", description = "Collecte introuvable")
    })
    public ResponseEntity<AgentMediaResponse> uploadAudio(
            @PathVariable Long collecteId,
            @Parameter(description = "Fichier audio à téléverser", required = true)
            @RequestParam("file") MultipartFile file) {
        log.info("Téléversement audio pour la collecte ID {}", collecteId);
        AgentMediaResponse response = agentMediaService.uploadAudio(collecteId, file);
        return ResponseEntity.ok(response);
    }

    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Associer ou mettre à jour directement les URLs de médias",
               description = "Permet de renseigner manuellement les URLs de photos ou d'enregistrements audio hébergés sur un service externe (Firebase Cloud Storage, S3, etc.).")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "URLs de médias mises à jour"),
        @ApiResponse(responseCode = "400", description = "Collecte verrouillée"),
        @ApiResponse(responseCode = "403", description = "Collecte n'appartenant pas à l'agent"),
        @ApiResponse(responseCode = "404", description = "Collecte introuvable")
    })
    public ResponseEntity<AgentMediaResponse> associerMediasUrls(
            @PathVariable Long collecteId,
            @Valid @RequestBody AgentMediaUpdateRequest request) {
        log.info("Mise à jour des URLs de médias pour la collecte ID {}", collecteId);
        AgentMediaResponse response = agentMediaService.associerMediasUrls(collecteId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Consulter les médias associés à une collecte",
               description = "Retourne les URLs de la photo et de l'enregistrement audio associés à la fiche de collecte.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Médias récupérés"),
        @ApiResponse(responseCode = "403", description = "Collecte n'appartenant pas à l'agent"),
        @ApiResponse(responseCode = "404", description = "Collecte introuvable")
    })
    public ResponseEntity<AgentMediaResponse> getMediasByCollecte(@PathVariable Long collecteId) {
        AgentMediaResponse response = agentMediaService.getMediasByCollecte(collecteId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/photo")
    @Operation(summary = "Supprimer la photo d'une collecte",
               description = "Supprime la photo associée à une fiche de collecte modifiable.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Photo supprimée avec succès"),
        @ApiResponse(responseCode = "400", description = "Collecte verrouillée"),
        @ApiResponse(responseCode = "403", description = "Collecte n'appartenant pas à l'agent"),
        @ApiResponse(responseCode = "404", description = "Collecte introuvable")
    })
    public ResponseEntity<AgentMediaResponse> supprimerPhoto(@PathVariable Long collecteId) {
        log.info("Suppression de la photo pour la collecte ID {}", collecteId);
        AgentMediaResponse response = agentMediaService.supprimerPhoto(collecteId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/audio")
    @Operation(summary = "Supprimer l'enregistrement audio d'une collecte",
               description = "Supprime l'enregistrement audio associé à une fiche de collecte modifiable.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Enregistrement audio supprimé avec succès"),
        @ApiResponse(responseCode = "400", description = "Collecte verrouillée"),
        @ApiResponse(responseCode = "403", description = "Collecte n'appartenant pas à l'agent"),
        @ApiResponse(responseCode = "404", description = "Collecte introuvable")
    })
    public ResponseEntity<AgentMediaResponse> supprimerAudio(@PathVariable Long collecteId) {
        log.info("Suppression de l'audio pour la collecte ID {}", collecteId);
        AgentMediaResponse response = agentMediaService.supprimerAudio(collecteId);
        return ResponseEntity.ok(response);
    }
}
