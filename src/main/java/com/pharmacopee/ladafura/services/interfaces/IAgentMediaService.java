package com.pharmacopee.ladafura.services.interfaces;

import org.springframework.web.multipart.MultipartFile;

import com.pharmacopee.ladafura.dto.agent.media.AgentMediaResponse;
import com.pharmacopee.ladafura.dto.agent.media.AgentMediaUpdateRequest;

public interface IAgentMediaService {

    /**
     * Associe ou met à jour directement les URLs de médias (photoUrl, audioUrl) à une collecte.
     * Utile lorsque le stockage est géré en amont par Firebase Cloud Storage ou un CDN externe.
     *
     * @param collecteId Identifiant de la collecte
     * @param request Requête contenant les URLs
     * @return DTO AgentMediaResponse
     */
    AgentMediaResponse associerMediasUrls(Long collecteId, AgentMediaUpdateRequest request);

    /**
     * Téléverse et associe une photo d'échantillon ou de plante à une collecte.
     *
     * @param collecteId Identifiant de la collecte
     * @param file Fichier photo (JPEG, PNG, WebP)
     * @return DTO AgentMediaResponse
     */
    AgentMediaResponse uploadPhoto(Long collecteId, MultipartFile file);

    /**
     * Téléverse et associe un enregistrement audio de témoignage oral à une collecte.
     *
     * @param collecteId Identifiant de la collecte
     * @param file Fichier audio (MP3, WAV, OGG, M4A, AAC)
     * @return DTO AgentMediaResponse
     */
    AgentMediaResponse uploadAudio(Long collecteId, MultipartFile file);

    /**
     * Récupère la liste des médias rattachés à une fiche de collecte.
     *
     * @param collecteId Identifiant de la collecte
     * @return DTO AgentMediaResponse
     */
    AgentMediaResponse getMediasByCollecte(Long collecteId);

    /**
     * Supprime la photo rattachée à une collecte modifiable.
     *
     * @param collecteId Identifiant de la collecte
     * @return DTO AgentMediaResponse
     */
    AgentMediaResponse supprimerPhoto(Long collecteId);

    /**
     * Supprime l'audio rattaché à une collecte modifiable.
     *
     * @param collecteId Identifiant de la collecte
     * @return DTO AgentMediaResponse
     */
    AgentMediaResponse supprimerAudio(Long collecteId);
}
