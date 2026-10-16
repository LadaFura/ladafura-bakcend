package com.pharmacopee.ladafura.configs;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

import com.pharmacopee.ladafura.Models.FichierMediaStocke;
import com.pharmacopee.ladafura.repository.FichierMediaStockeRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class WebMvcConfig implements WebMvcConfigurer {

    private final FichierMediaStockeRepository fichierMediaStockeRepository;

    @Value("${file.upload.dir:uploads}")
    private String baseUploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path uploadDirectory = Paths.get(baseUploadDir).toAbsolutePath().normalize();
        String location = uploadDirectory.toUri().toString();
        if (!location.endsWith("/")) {
            location += "/";
        }

        registry.addResourceHandler("/" + baseUploadDir + "/**")
                .addResourceLocations(location, "file:uploads/", "file:ladafura/uploads/")
                .resourceChain(false)
                .addResolver(new PathResourceResolver() {
                    @Override
                    protected Resource getResource(String resourcePath, Resource location) throws IOException {
                        // 1. Recherche standard sur le système de fichiers local
                        Resource diskResource = super.getResource(resourcePath, location);
                        if (diskResource != null && diskResource.exists() && diskResource.isReadable()) {
                            return diskResource;
                        }

                        // 2. Si introuvable sur le disque local (ex: conteneur Render redémarré),
                        //    rechercher dans la base de données MySQL Aiven Cloud et restaurer le cache disque
                        String cleanSubPath = resourcePath.replace("\\", "/").replaceAll("^/+", "");
                        String cheminUrl = "/" + baseUploadDir + "/" + cleanSubPath;

                        try {
                            Optional<FichierMediaStocke> storedOpt = fichierMediaStockeRepository.findByChemin(cheminUrl);
                            if (storedOpt.isPresent() && storedOpt.get().getDonnees() != null) {
                                byte[] bytes = storedOpt.get().getDonnees();
                                Path localTarget = uploadDirectory.resolve(cleanSubPath).normalize();
                                if (localTarget.startsWith(uploadDirectory)) {
                                    try {
                                        Files.createDirectories(localTarget.getParent());
                                        Files.write(localTarget, bytes);
                                        log.info("Média restauré à la volée depuis MySQL vers le disque : {}", cheminUrl);
                                        return new FileSystemResource(localTarget);
                                    } catch (IOException ioEx) {
                                        return new ByteArrayResource(bytes) {
                                            @Override
                                            public String getFilename() {
                                                return localTarget.getFileName().toString();
                                            }
                                        };
                                    }
                                }
                            }
                        } catch (Exception dbEx) {
                            log.warn("Erreur lors de la récupération du média '{}' depuis MySQL : {}", cheminUrl, dbEx.getMessage());
                        }

                        // 3. Fallback gracieux pour les anciens fichiers orphelins perdus avant la persistance MySQL
                        Resource fallback = resolveFallbackResource(cleanSubPath, uploadDirectory);
                        if (fallback != null) {
                            return fallback;
                        }

                        return null;
                    }
                });
    }

    /**
     * Fournit un média de secours de la même catégorie lorsqu'une ancienne URL pointe vers un fichier
     * qui avait été effacé par le disque éphémère de Render avant la mise en place de la persistance MySQL.
     */
    private Resource resolveFallbackResource(String cleanSubPath, Path uploadDirectory) {
        try {
            String lower = cleanSubPath.toLowerCase();
            Path categoryDir = null;

            if (lower.startsWith("collectes/audios/") || lower.endsWith(".mp3") || lower.endsWith(".wav")
                    || lower.endsWith(".ogg") || lower.endsWith(".m4a") || lower.endsWith(".webm")) {
                categoryDir = uploadDirectory.resolve("collectes/audios");
            } else if (lower.startsWith("plantes/")) {
                categoryDir = uploadDirectory.resolve("plantes");
            } else if (lower.startsWith("produits/")) {
                categoryDir = uploadDirectory.resolve("produits");
            } else if (lower.startsWith("pharmacopees/")) {
                categoryDir = uploadDirectory.resolve("pharmacopees");
            } else if (lower.startsWith("collectes/photos/") || lower.startsWith("collectesphotos/")) {
                categoryDir = uploadDirectory.resolve("collectes/photos");
            }

            if (categoryDir != null && Files.exists(categoryDir) && Files.isDirectory(categoryDir)) {
                try (Stream<Path> files = Files.list(categoryDir)) {
                    Optional<Path> firstFile = files.filter(Files::isRegularFile).findFirst();
                    if (firstFile.isPresent()) {
                        return new FileSystemResource(firstFile.get());
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }
}
