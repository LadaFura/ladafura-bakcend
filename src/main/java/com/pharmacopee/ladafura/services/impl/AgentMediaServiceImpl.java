package com.pharmacopee.ladafura.services.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.pharmacopee.ladafura.Models.AgentCollecte;
import com.pharmacopee.ladafura.Models.Collecte;
import com.pharmacopee.ladafura.dto.agent.media.AgentMediaResponse;
import com.pharmacopee.ladafura.dto.agent.media.AgentMediaUpdateRequest;
import com.pharmacopee.ladafura.enums.StatutCollecte;
import com.pharmacopee.ladafura.exceptions.BadRequestException;
import com.pharmacopee.ladafura.exceptions.ForbiddenException;
import com.pharmacopee.ladafura.exceptions.ResourceNotFoundException;
import com.pharmacopee.ladafura.repository.CollecteRepository;
import com.pharmacopee.ladafura.services.interfaces.IAgentAuthService;
import com.pharmacopee.ladafura.services.interfaces.IAgentMediaService;
import com.pharmacopee.ladafura.services.storage.IFileStorageService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AgentMediaServiceImpl implements IAgentMediaService {

    private static final List<String> ALLOWED_PHOTO_MIMES = List.of(
            "image/jpeg", "image/png", "image/webp"
    );
    private static final long MAX_PHOTO_SIZE = 10 * 1024 * 1024L; // 10 MB

    private static final List<String> ALLOWED_AUDIO_MIMES = List.of(
            "audio/mpeg", "audio/mp3", "audio/wav", "audio/ogg", "audio/m4a", "audio/aac", "audio/x-m4a"
    );
    private static final long MAX_AUDIO_SIZE = 25 * 1024 * 1024L; // 25 MB

    private final CollecteRepository collecteRepository;
    private final IFileStorageService fileStorageService;
    private final IAgentAuthService agentAuthService;

    @Override
    @Transactional
    public AgentMediaResponse associerMediasUrls(Long collecteId, AgentMediaUpdateRequest request) {
        AgentCollecte currentAgent = agentAuthService.getCurrentAgent();
        Collecte collecte = collecteRepository.findById(collecteId)
                .orElseThrow(() -> new ResourceNotFoundException("Collecte", "id", collecteId));

        verifyCollecteOwnershipAndModifiability(collecte, currentAgent);

        if (request.getPhotoUrl() != null) {
            collecte.setPhotoUrl(request.getPhotoUrl().isBlank() ? null : request.getPhotoUrl().trim());
        }
        if (request.getAudioUrl() != null) {
            collecte.setAudioUrl(request.getAudioUrl().isBlank() ? null : request.getAudioUrl().trim());
        }

        Collecte saved = collecteRepository.save(collecte);
        log.info("URLs de médias mises à jour pour la collecte ID {}", collecteId);
        return mapToResponse(saved, "URLs de médias mises à jour avec succès");
    }

    @Override
    @Transactional
    public AgentMediaResponse uploadPhoto(Long collecteId, MultipartFile file) {
        AgentCollecte currentAgent = agentAuthService.getCurrentAgent();
        Collecte collecte = collecteRepository.findById(collecteId)
                .orElseThrow(() -> new ResourceNotFoundException("Collecte", "id", collecteId));

        verifyCollecteOwnershipAndModifiability(collecte, currentAgent);

        // Nettoyage de l'ancienne photo si elle était stockée localement
        if (collecte.getPhotoUrl() != null && collecte.getPhotoUrl().startsWith("/uploads/")) {
            fileStorageService.deleteFile(collecte.getPhotoUrl());
        }

        String photoUrl = fileStorageService.storeFile(file, "collectes/photos", ALLOWED_PHOTO_MIMES, MAX_PHOTO_SIZE);
        collecte.setPhotoUrl(photoUrl);

        Collecte saved = collecteRepository.save(collecte);
        log.info("Photo téléversée pour la collecte ID {} : {}", collecteId, photoUrl);
        return mapToResponse(saved, "Photo d'échantillon téléversée et associée avec succès");
    }

    @Override
    @Transactional
    public AgentMediaResponse uploadAudio(Long collecteId, MultipartFile file) {
        AgentCollecte currentAgent = agentAuthService.getCurrentAgent();
        Collecte collecte = collecteRepository.findById(collecteId)
                .orElseThrow(() -> new ResourceNotFoundException("Collecte", "id", collecteId));

        verifyCollecteOwnershipAndModifiability(collecte, currentAgent);

        // Nettoyage de l'ancien fichier audio si local
        if (collecte.getAudioUrl() != null && collecte.getAudioUrl().startsWith("/uploads/")) {
            fileStorageService.deleteFile(collecte.getAudioUrl());
        }

        String audioUrl = fileStorageService.storeFile(file, "collectes/audios", ALLOWED_AUDIO_MIMES, MAX_AUDIO_SIZE);
        collecte.setAudioUrl(audioUrl);

        Collecte saved = collecteRepository.save(collecte);
        log.info("Enregistrement audio téléversé pour la collecte ID {} : {}", collecteId, audioUrl);
        return mapToResponse(saved, "Enregistrement audio de témoignage téléversé et associé avec succès");
    }

    @Override
    @Transactional(readOnly = true)
    public AgentMediaResponse getMediasByCollecte(Long collecteId) {
        AgentCollecte currentAgent = agentAuthService.getCurrentAgent();
        Collecte collecte = collecteRepository.findById(collecteId)
                .orElseThrow(() -> new ResourceNotFoundException("Collecte", "id", collecteId));

        if (collecte.getAgentCollecte() == null || !collecte.getAgentCollecte().getId().equals(currentAgent.getId())) {
            throw new ForbiddenException("Accès refusé : cette fiche de collecte ne vous appartient pas.");
        }

        return mapToResponse(collecte, "Médias de la collecte récupérés avec succès");
    }

    @Override
    @Transactional
    public AgentMediaResponse supprimerPhoto(Long collecteId) {
        AgentCollecte currentAgent = agentAuthService.getCurrentAgent();
        Collecte collecte = collecteRepository.findById(collecteId)
                .orElseThrow(() -> new ResourceNotFoundException("Collecte", "id", collecteId));

        verifyCollecteOwnershipAndModifiability(collecte, currentAgent);

        if (collecte.getPhotoUrl() != null && collecte.getPhotoUrl().startsWith("/uploads/")) {
            fileStorageService.deleteFile(collecte.getPhotoUrl());
        }
        collecte.setPhotoUrl(null);

        Collecte saved = collecteRepository.save(collecte);
        log.info("Photo dissociée de la collecte ID {}", collecteId);
        return mapToResponse(saved, "Photo supprimée avec succès");
    }

    @Override
    @Transactional
    public AgentMediaResponse supprimerAudio(Long collecteId) {
        AgentCollecte currentAgent = agentAuthService.getCurrentAgent();
        Collecte collecte = collecteRepository.findById(collecteId)
                .orElseThrow(() -> new ResourceNotFoundException("Collecte", "id", collecteId));

        verifyCollecteOwnershipAndModifiability(collecte, currentAgent);

        if (collecte.getAudioUrl() != null && collecte.getAudioUrl().startsWith("/uploads/")) {
            fileStorageService.deleteFile(collecte.getAudioUrl());
        }
        collecte.setAudioUrl(null);

        Collecte saved = collecteRepository.save(collecte);
        log.info("Audio dissocié de la collecte ID {}", collecteId);
        return mapToResponse(saved, "Enregistrement audio supprimé avec succès");
    }

    private void verifyCollecteOwnershipAndModifiability(Collecte collecte, AgentCollecte currentAgent) {
        if (collecte.getAgentCollecte() == null || !collecte.getAgentCollecte().getId().equals(currentAgent.getId())) {
            throw new ForbiddenException("Accès refusé : cette collecte ne vous appartient pas.");
        }

        if (collecte.getStatut() != StatutCollecte.BROUILLON && collecte.getStatut() != StatutCollecte.REJETEE) {
            throw new BadRequestException("Impossible de modifier les médias d'une collecte avec le statut " + collecte.getStatut()
                    + ". Seules les collectes en statut BROUILLON ou REJETEE peuvent être modifiées.");
        }
    }

    private AgentMediaResponse mapToResponse(Collecte collecte, String message) {
        return AgentMediaResponse.builder()
                .collecteId(collecte.getId())
                .photoUrl(collecte.getPhotoUrl())
                .audioUrl(collecte.getAudioUrl())
                .photoPresente(collecte.getPhotoUrl() != null && !collecte.getPhotoUrl().isBlank())
                .audioPresent(collecte.getAudioUrl() != null && !collecte.getAudioUrl().isBlank())
                .message(message)
                .build();
    }
}
