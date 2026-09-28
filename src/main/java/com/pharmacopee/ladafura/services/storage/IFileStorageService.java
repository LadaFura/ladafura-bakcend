package com.pharmacopee.ladafura.services.storage;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

public interface IFileStorageService {

    /**
     * Stocke un fichier téléversé dans le répertoire de destination spécifié après validation.
     *
     * @param file Fichier reçu
     * @param subDirectory Sous-répertoire de classement (ex: "collectes/photos", "collectes/audios")
     * @param allowedMimeTypes Liste des types MIME autorisés
     * @param maxSizeBytes Taille maximale autorisée en octets
     * @return URL d'accès au fichier stocké
     */
    String storeFile(MultipartFile file, String subDirectory, List<String> allowedMimeTypes, long maxSizeBytes);

    /**
     * Supprime un fichier stocké à partir de son URL relative.
     *
     * @param fileUrl URL relative ou chemin du fichier
     */
    void deleteFile(String fileUrl);
}
