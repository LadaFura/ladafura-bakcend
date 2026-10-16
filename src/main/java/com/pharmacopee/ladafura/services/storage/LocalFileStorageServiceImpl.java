package com.pharmacopee.ladafura.services.storage;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pharmacopee.ladafura.Models.FichierMediaStocke;
import com.pharmacopee.ladafura.exceptions.BadRequestException;
import com.pharmacopee.ladafura.repository.FichierMediaStockeRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class LocalFileStorageServiceImpl implements IFileStorageService {

    private final FichierMediaStockeRepository fichierMediaStockeRepository;
    private final ObjectMapper objectMapper;

    @Value("${file.upload.dir:uploads}")
    private String baseUploadDir;

    @Value("${cloudinary.cloud-name:}")
    private String cloudinaryCloudName;

    @Value("${cloudinary.api-key:}")
    private String cloudinaryApiKey;

    @Value("${cloudinary.api-secret:}")
    private String cloudinaryApiSecret;

    /**
     * Au démarrage de l'application (notamment sur Render après un redéploiement Docker),
     * synchronise les fichiers entre le disque local et la base de données MySQL Aiven Cloud.
     */
    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void syncMediaFilesOnStartup() {
        try {
            Path rootUploadPath = Paths.get(baseUploadDir).toAbsolutePath().normalize();
            if (!Files.exists(rootUploadPath)) {
                Files.createDirectories(rootUploadPath);
            }

            // 1. Sauvegarder en base MySQL les fichiers présents localement dans uploads/ mais absents de la BDD
            try (Stream<Path> paths = Files.walk(rootUploadPath)) {
                paths.filter(Files::isRegularFile).forEach(filePath -> {
                    try {
                        Path relative = rootUploadPath.relativize(filePath);
                        String normalizedRelative = relative.toString().replace("\\", "/");
                        String cheminUrl = "/" + baseUploadDir + "/" + normalizedRelative;

                        if (!fichierMediaStockeRepository.existsByChemin(cheminUrl)) {
                            byte[] bytes = Files.readAllBytes(filePath);
                            String contentType = detectContentType(filePath.getFileName().toString());
                            FichierMediaStocke entity = FichierMediaStocke.builder()
                                    .chemin(cheminUrl)
                                    .nomOriginal(filePath.getFileName().toString())
                                    .contentType(contentType)
                                    .taille((long) bytes.length)
                                    .donnees(bytes)
                                    .dateCreation(LocalDateTime.now())
                                    .build();
                            fichierMediaStockeRepository.save(entity);
                            log.info("Média local synchronisé vers MySQL : {}", cheminUrl);
                        }
                    } catch (Exception ex) {
                        log.warn("Impossible de synchroniser le fichier local {} vers MySQL : {}", filePath, ex.getMessage());
                    }
                });
            }

            // 2. Restaurer sur le disque local tous les fichiers présents dans MySQL mais effacés du conteneur (ex: Render)
            List<FichierMediaStocke> storedFiles = fichierMediaStockeRepository.findAll();
            int restoredCount = 0;
            for (FichierMediaStocke stored : storedFiles) {
                if (stored.getChemin() != null && stored.getChemin().startsWith("/" + baseUploadDir + "/") && stored.getDonnees() != null) {
                    String relativeSubPath = stored.getChemin().substring(("/" + baseUploadDir + "/").length());
                    Path localTarget = rootUploadPath.resolve(relativeSubPath).normalize();
                    if (!Files.exists(localTarget)) {
                        Files.createDirectories(localTarget.getParent());
                        Files.write(localTarget, stored.getDonnees());
                        restoredCount++;
                    }
                }
            }
            if (restoredCount > 0) {
                log.info("{} fichier(s) média restauré(s) depuis MySQL vers le disque local '{}'.", restoredCount, rootUploadPath);
            }
        } catch (Exception e) {
            log.warn("Erreur mineure lors de la synchronisation initiale des médias : {}", e.getMessage());
        }
    }

    @Override
    @Transactional
    public String storeFile(MultipartFile file, String subDirectory, List<String> allowedMimeTypes, long maxSizeBytes) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Le fichier envoyé est vide ou manquant.");
        }

        if (file.getSize() > maxSizeBytes) {
            throw new BadRequestException(String.format("La taille du fichier (%d octets) dépasse la limite autorisée (%d octets).",
                    file.getSize(), maxSizeBytes));
        }

        String contentType = file.getContentType();
        String baseContentType = (contentType != null && contentType.contains(";"))
                ? contentType.split(";")[0].trim().toLowerCase()
                : (contentType != null ? contentType.toLowerCase() : "");

        if (contentType == null || (!allowedMimeTypes.contains(contentType.toLowerCase()) && !allowedMimeTypes.contains(baseContentType))) {
            throw new BadRequestException(String.format("Type de fichier non autorisé : '%s'. Types acceptés : %s",
                    contentType, allowedMimeTypes));
        }

        String originalFilename = StringUtils.cleanPath(
                file.getOriginalFilename() != null ? file.getOriginalFilename() : "media"
        );

        // Si Cloudinary est configuré via les variables d'environnement, déléguer l'hébergement à Cloudinary
        if (isCloudinaryConfigured()) {
            try {
                String cloudinaryUrl = uploadToCloudinary(file, subDirectory, baseContentType);
                if (cloudinaryUrl != null) {
                    return cloudinaryUrl;
                }
            } catch (Exception ex) {
                log.warn("Échec de l'envoi vers Cloudinary ({}), bascule automatique sur le stockage persistant MySQL + Disque.", ex.getMessage());
            }
        }

        // Détermination de l'extension du fichier
        String extension = "";
        int extIndex = originalFilename.lastIndexOf('.');
        if (extIndex > 0) {
            extension = originalFilename.substring(extIndex).toLowerCase();
        } else {
            if (baseContentType.contains("webm")) {
                extension = ".webm";
            } else if (baseContentType.contains("mpeg") || baseContentType.contains("mp3")) {
                extension = ".mp3";
            } else if (baseContentType.contains("ogg")) {
                extension = ".ogg";
            } else if (baseContentType.contains("wav")) {
                extension = ".wav";
            } else if (baseContentType.contains("m4a") || baseContentType.contains("mp4")) {
                extension = ".m4a";
            } else if (baseContentType.contains("jpeg") || baseContentType.contains("jpg")) {
                extension = ".jpg";
            } else if (baseContentType.contains("png")) {
                extension = ".png";
            } else if (baseContentType.contains("webp")) {
                extension = ".webp";
            }
        }

        String uniqueFileName = UUID.randomUUID().toString().substring(0, 8) + "_" + System.currentTimeMillis() + extension;
        String cleanSubDir = subDirectory.replace("\\", "/").replaceAll("^/+|/+$", "");
        String cheminUrl = "/" + baseUploadDir + "/" + cleanSubDir + "/" + uniqueFileName;

        try {
            byte[] fileBytes = file.getBytes();

            // 1. Écriture dans le cache disque local (uploads/...)
            Path targetDirectory = Paths.get(baseUploadDir, cleanSubDir).toAbsolutePath().normalize();
            if (!Files.exists(targetDirectory)) {
                Files.createDirectories(targetDirectory);
            }
            Path targetPath = targetDirectory.resolve(uniqueFileName);
            Files.write(targetPath, fileBytes);

            // 2. Persistance dans MySQL (Aiven Cloud) pour survivre aux redémarrages/redéploiements de Render
            String resolvedMimeType = !baseContentType.isBlank() ? baseContentType : detectContentType(uniqueFileName);
            Optional<FichierMediaStocke> existing = fichierMediaStockeRepository.findByChemin(cheminUrl);
            FichierMediaStocke mediaStocke = existing.orElseGet(FichierMediaStocke::new);
            mediaStocke.setChemin(cheminUrl);
            mediaStocke.setNomOriginal(originalFilename);
            mediaStocke.setContentType(resolvedMimeType);
            mediaStocke.setTaille((long) fileBytes.length);
            mediaStocke.setDonnees(fileBytes);
            mediaStocke.setDateCreation(LocalDateTime.now());
            fichierMediaStockeRepository.save(mediaStocke);

            log.info("Fichier stocké avec succès (Disque + MySQL) sous : {} ({} octets)", cheminUrl, fileBytes.length);
            return cheminUrl;
        } catch (IOException e) {
            log.error("Échec du stockage du fichier", e);
            throw new BadRequestException("Une erreur est survenue lors de l'enregistrement du fichier : " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) {
            return;
        }

        try {
            if (fichierMediaStockeRepository.existsByChemin(fileUrl)) {
                fichierMediaStockeRepository.deleteByChemin(fileUrl);
                log.info("Fichier supprimé de la base MySQL : {}", fileUrl);
            }
        } catch (Exception e) {
            log.warn("Impossible de supprimer le fichier '{}' de MySQL : {}", fileUrl, e.getMessage());
        }

        try {
            String relativePath = fileUrl.startsWith("/") ? fileUrl.substring(1) : fileUrl;
            Path filePath = Paths.get(relativePath).toAbsolutePath().normalize();
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.info("Fichier physique supprimé du cache disque : {}", filePath);
            }
        } catch (IOException e) {
            log.warn("Impossible de supprimer le fichier physique '{}' : {}", fileUrl, e.getMessage());
        }
    }

    private boolean isCloudinaryConfigured() {
        return cloudinaryCloudName != null && !cloudinaryCloudName.isBlank()
                && cloudinaryApiKey != null && !cloudinaryApiKey.isBlank()
                && cloudinaryApiSecret != null && !cloudinaryApiSecret.isBlank();
    }

    /**
     * Téléversement direct vers Cloudinary (supporte images et vidéos/audios via resource_type=auto).
     */
    private String uploadToCloudinary(MultipartFile file, String subDirectory, String baseContentType) throws Exception {
        long timestamp = System.currentTimeMillis() / 1000L;
        String folder = "ladafura/" + subDirectory.replace("\\", "/");
        String toSign = "folder=" + folder + "&timestamp=" + timestamp + cloudinaryApiSecret;

        MessageDigest md = MessageDigest.getInstance("SHA-1");
        String signature = HexFormat.of().formatHex(md.digest(toSign.getBytes(StandardCharsets.UTF_8)));

        String boundary = "----LadafuraCloudinaryBoundary" + UUID.randomUUID().toString().replace("-", "");
        byte[] fileBytes = file.getBytes();
        String fileName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "upload.bin";

        StringBuilder partHeader = new StringBuilder();
        appendFormField(partHeader, boundary, "api_key", cloudinaryApiKey);
        appendFormField(partHeader, boundary, "timestamp", String.valueOf(timestamp));
        appendFormField(partHeader, boundary, "folder", folder);
        appendFormField(partHeader, boundary, "signature", signature);

        partHeader.append("--").append(boundary).append("\r\n");
        partHeader.append("Content-Disposition: form-data; name=\"file\"; filename=\"").append(fileName).append("\"\r\n");
        partHeader.append("Content-Type: ").append(baseContentType.isBlank() ? "application/octet-stream" : baseContentType).append("\r\n\r\n");

        byte[] headerBytes = partHeader.toString().getBytes(StandardCharsets.UTF_8);
        byte[] footerBytes = ("\r\n--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8);

        byte[] body = new byte[headerBytes.length + fileBytes.length + footerBytes.length];
        System.arraycopy(headerBytes, 0, body, 0, headerBytes.length);
        System.arraycopy(fileBytes, 0, body, headerBytes.length, fileBytes.length);
        System.arraycopy(footerBytes, 0, body, headerBytes.length + fileBytes.length, footerBytes.length);

        String endpoint = "https://api.cloudinary.com/v1_1/" + cloudinaryCloudName.trim() + "/auto/upload";
        HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(15)).build();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .timeout(Duration.ofSeconds(60))
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .POST(HttpRequest.BodyPublishers.ofByteArray(body))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            JsonNode json = objectMapper.readTree(response.body());
            if (json.hasNonNull("secure_url")) {
                String secureUrl = json.get("secure_url").asText();
                log.info("Fichier téléversé sur Cloudinary avec succès : {}", secureUrl);
                return secureUrl;
            }
        }
        throw new IOException("Réponse Cloudinary HTTP " + response.statusCode() + " : " + response.body());
    }

    private void appendFormField(StringBuilder sb, String boundary, String name, String value) {
        sb.append("--").append(boundary).append("\r\n");
        sb.append("Content-Disposition: form-data; name=\"").append(name).append("\"\r\n\r\n");
        sb.append(value).append("\r\n");
    }

    public static String detectContentType(String filename) {
        String lower = filename != null ? filename.toLowerCase() : "";
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".webp")) return "image/webp";
        if (lower.endsWith(".mp3")) return "audio/mpeg";
        if (lower.endsWith(".wav")) return "audio/wav";
        if (lower.endsWith(".ogg")) return "audio/ogg";
        if (lower.endsWith(".m4a")) return "audio/mp4";
        if (lower.endsWith(".aac")) return "audio/aac";
        if (lower.endsWith(".webm")) return "audio/webm";
        return "application/octet-stream";
    }
}
