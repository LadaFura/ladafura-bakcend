package com.pharmacopee.ladafura.services.storage;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.pharmacopee.ladafura.exceptions.BadRequestException;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class LocalFileStorageServiceImpl implements IFileStorageService {

    @Value("${file.upload.dir:uploads}")
    private String baseUploadDir;

    @Override
    public String storeFile(MultipartFile file, String subDirectory, List<String> allowedMimeTypes, long maxSizeBytes) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Le fichier envoyé est vide ou manquant.");
        }

        if (file.getSize() > maxSizeBytes) {
            throw new BadRequestException(String.format("La taille du fichier (%d octets) dépasse la limite autorisée (%d octets).",
                    file.getSize(), maxSizeBytes));
        }

        String contentType = file.getContentType();
        if (contentType == null || !allowedMimeTypes.contains(contentType.toLowerCase())) {
            throw new BadRequestException(String.format("Type de fichier non autorisé : '%s'. Types acceptés : %s",
                    contentType, allowedMimeTypes));
        }

        String originalFilename = StringUtils.cleanPath(
                file.getOriginalFilename() != null ? file.getOriginalFilename() : "media"
        );

        // Nettoyage des caractères spéciaux dans le nom de fichier
        String extension = "";
        int extIndex = originalFilename.lastIndexOf('.');
        if (extIndex > 0) {
            extension = originalFilename.substring(extIndex).toLowerCase();
        }

        String uniqueFileName = UUID.randomUUID().toString().substring(0, 8) + "_" + System.currentTimeMillis() + extension;

        try {
            Path targetDirectory = Paths.get(baseUploadDir, subDirectory).toAbsolutePath().normalize();
            if (!Files.exists(targetDirectory)) {
                Files.createDirectories(targetDirectory);
            }

            Path targetPath = targetDirectory.resolve(uniqueFileName);
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
            }

            log.info("Fichier stocké avec succès sous : {}", targetPath);
            return "/" + baseUploadDir + "/" + subDirectory + "/" + uniqueFileName;
        } catch (IOException e) {
            log.error("Échec du stockage physique du fichier", e);
            throw new BadRequestException("Une erreur est survenue lors de l'enregistrement du fichier : " + e.getMessage());
        }
    }

    @Override
    public void deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) {
            return;
        }

        try {
            String relativePath = fileUrl.startsWith("/") ? fileUrl.substring(1) : fileUrl;
            Path filePath = Paths.get(relativePath).toAbsolutePath().normalize();
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.info("Fichier physique supprimé : {}", filePath);
            }
        } catch (IOException e) {
            log.warn("Impossible de supprimer le fichier physique '{}' : {}", fileUrl, e.getMessage());
        }
    }
}
