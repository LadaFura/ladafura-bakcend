package com.pharmacopee.ladafura.controllers.common;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.pharmacopee.ladafura.dto.common.FileUploadResponse;
import com.pharmacopee.ladafura.services.storage.IFileStorageService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/fichiers")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Fichiers & Médias", description = "Endpoints de téléversement et gestion de fichiers médias (images, photos)")
@SecurityRequirement(name = "bearerAuth")
public class FileUploadController {

    private static final List<String> ALLOWED_IMAGE_TYPES = List.of(
            "image/jpeg",
            "image/png",
            "image/webp",
            "image/jpg"
    );
    private static final long MAX_IMAGE_SIZE = 10 * 1024 * 1024L; // 10 Mo

    private final IFileStorageService fileStorageService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Téléverser une image ou photo",
               description = "Téléverse une image (JPEG, PNG, WebP jusqu'à 10 Mo) dans un dossier spécifique (ex: plantes, produits) et retourne son URL d'accès.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Fichier téléversé avec succès"),
        @ApiResponse(responseCode = "400", description = "Fichier manquant, taille dépassée ou format non supporté"),
        @ApiResponse(responseCode = "401", description = "Utilisateur non authentifié")
    })
    public ResponseEntity<FileUploadResponse> uploadImage(
            @Parameter(description = "Fichier image à téléverser", required = true)
            @RequestParam("file") MultipartFile file,
            @Parameter(description = "Dossier de classement (plantes, produits, etc.)")
            @RequestParam(value = "dossier", defaultValue = "general") String dossier) {

        log.info("Réception d'une image pour le dossier '{}', nom: {}, taille: {} octets",
                dossier, file.getOriginalFilename(), file.getSize());

        // Nettoyage du nom de sous-dossier pour éviter l'évasion de répertoire
        String sanitizedDossier = dossier.replaceAll("[^a-zA-Z0-9_-]", "");
        if (sanitizedDossier.isBlank()) {
            sanitizedDossier = "general";
        }

        String fileUrl = fileStorageService.storeFile(file, sanitizedDossier, ALLOWED_IMAGE_TYPES, MAX_IMAGE_SIZE);

        FileUploadResponse response = FileUploadResponse.builder()
                .url(fileUrl)
                .fileName(file.getOriginalFilename())
                .contentType(file.getContentType())
                .size(file.getSize())
                .build();

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}
