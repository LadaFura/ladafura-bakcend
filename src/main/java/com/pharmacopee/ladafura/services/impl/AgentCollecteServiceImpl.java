package com.pharmacopee.ladafura.services.impl;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pharmacopee.ladafura.Models.AgentCollecte;
import com.pharmacopee.ladafura.Models.Collecte;
import com.pharmacopee.ladafura.Models.Localisation;
import com.pharmacopee.ladafura.Models.Source;
import com.pharmacopee.ladafura.dto.agent.collecte.AgentCollecteDetailResponse;
import com.pharmacopee.ladafura.dto.agent.collecte.AgentCollecteSummaryResponse;
import com.pharmacopee.ladafura.dto.agent.collecte.AgentCreateCollecteRequest;
import com.pharmacopee.ladafura.dto.agent.collecte.AgentUpdateCollecteRequest;
import com.pharmacopee.ladafura.enums.StatutCollecte;
import com.pharmacopee.ladafura.exceptions.BadRequestException;
import com.pharmacopee.ladafura.exceptions.ForbiddenException;
import com.pharmacopee.ladafura.exceptions.ResourceNotFoundException;
import com.pharmacopee.ladafura.mappers.AgentCollecteMapper;
import com.pharmacopee.ladafura.repository.CollecteRepository;
import com.pharmacopee.ladafura.repository.LocalisationRepository;
import com.pharmacopee.ladafura.repository.SourceRepository;
import com.pharmacopee.ladafura.services.interfaces.IAgentAuthService;
import com.pharmacopee.ladafura.services.interfaces.IAgentCollecteService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AgentCollecteServiceImpl implements IAgentCollecteService {

    private final CollecteRepository collecteRepository;
    private final IAgentAuthService agentAuthService;
    private final AgentCollecteMapper agentCollecteMapper;
    private final SourceRepository sourceRepository;
    private final LocalisationRepository localisationRepository;

    @Override
    public AgentCollecteDetailResponse createCollecte(AgentCreateCollecteRequest request) {
        AgentCollecte agent = agentAuthService.getCurrentAgent();

        log.info("Création d'une nouvelle collecte par l'agent ID: {} (soumettre={})", agent.getId(), request.isSoumettre());

        Collecte collecte = new Collecte();
        collecte.setAgentCollecte(agent);
        collecte.setDateCollecte(request.getDateCollecte() != null ? request.getDateCollecte() : LocalDateTime.now());
        collecte.setDescription(request.getDescription());
        collecte.setPhotoUrl(request.getPhotoUrl());
        collecte.setAudioUrl(request.getAudioUrl());

        if (request.isSoumettre()) {
            collecte.setStatut(StatutCollecte.SOUMISE);
            collecte.setDateSoumission(LocalDateTime.now());
        } else {
            collecte.setStatut(StatutCollecte.BROUILLON);
        }

        if (request.getSourceId() != null) {
            Source source = sourceRepository.findById(request.getSourceId())
                    .orElseThrow(() -> new ResourceNotFoundException("Source", "id", request.getSourceId()));
            collecte.setSource(source);
        }

        if (request.getLocalisationId() != null) {
            Localisation loc = localisationRepository.findById(request.getLocalisationId())
                    .orElseThrow(() -> new ResourceNotFoundException("Localisation", "id", request.getLocalisationId()));
            collecte.setLocalisation(loc);
        }

        Collecte saved = collecteRepository.save(collecte);
        log.info("Collecte créée avec succès (ID={}, Statut={})", saved.getId(), saved.getStatut());

        return agentCollecteMapper.toDetailDto(saved);
    }

    @Override
    public AgentCollecteDetailResponse saveDraft(AgentCreateCollecteRequest request) {
        request.setSoumettre(false);
        return createCollecte(request);
    }

    @Override
    public AgentCollecteDetailResponse updateCollecte(Long id, AgentUpdateCollecteRequest request) {
        AgentCollecte agent = agentAuthService.getCurrentAgent();
        Collecte collecte = findCollecteOrThrow(id);
        verifyCollecteOwnership(collecte, agent);

        // Règle métier : Seules les collectes en brouillon ou rejetées peuvent être modifiées
        if (collecte.getStatut() != StatutCollecte.BROUILLON && collecte.getStatut() != StatutCollecte.REJETEE) {
            log.warn("Tentative de modification non autorisée pour la collecte ID: {} au statut {}", id, collecte.getStatut());
            throw new BadRequestException("Impossible de modifier une collecte avec le statut " + collecte.getStatut()
                    + ". Seules les collectes en statut BROUILLON ou REJETEE peuvent être modifiées.");
        }

        if (request.getDateCollecte() != null) {
            collecte.setDateCollecte(request.getDateCollecte());
        }
        if (request.getDescription() != null) {
            collecte.setDescription(request.getDescription());
        }
        if (request.getPhotoUrl() != null) {
            collecte.setPhotoUrl(request.getPhotoUrl());
        }
        if (request.getAudioUrl() != null) {
            collecte.setAudioUrl(request.getAudioUrl());
        }

        if (request.getSourceId() != null) {
            Source source = sourceRepository.findById(request.getSourceId())
                    .orElseThrow(() -> new ResourceNotFoundException("Source", "id", request.getSourceId()));
            collecte.setSource(source);
        }

        if (request.getLocalisationId() != null) {
            Localisation loc = localisationRepository.findById(request.getLocalisationId())
                    .orElseThrow(() -> new ResourceNotFoundException("Localisation", "id", request.getLocalisationId()));
            collecte.setLocalisation(loc);
        }

        Collecte saved = collecteRepository.save(collecte);
        log.info("Collecte ID: {} modifiée avec succès", saved.getId());

        return agentCollecteMapper.toDetailDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AgentCollecteSummaryResponse> getMyCollectes(StatutCollecte statut, Pageable pageable) {
        AgentCollecte agent = agentAuthService.getCurrentAgent();
        log.info("Récupération des collectes de l'agent ID: {} (statut={})", agent.getId(), statut);

        Page<Collecte> page;
        if (statut != null) {
            page = collecteRepository.findByAgentCollecteIdAndStatut(agent.getId(), statut, pageable);
        } else {
            page = collecteRepository.findByAgentCollecteId(agent.getId(), pageable);
        }

        return page.map(agentCollecteMapper::toSummaryDto);
    }

    @Override
    @Transactional(readOnly = true)
    public AgentCollecteDetailResponse getMyCollecteById(Long id) {
        AgentCollecte agent = agentAuthService.getCurrentAgent();
        Collecte collecte = findCollecteOrThrow(id);
        verifyCollecteOwnership(collecte, agent);

        return agentCollecteMapper.toDetailDto(collecte);
    }

    @Override
    public AgentCollecteDetailResponse submitCollecte(Long id) {
        AgentCollecte agent = agentAuthService.getCurrentAgent();
        Collecte collecte = findCollecteOrThrow(id);
        verifyCollecteOwnership(collecte, agent);

        if (collecte.getStatut() != StatutCollecte.BROUILLON && collecte.getStatut() != StatutCollecte.REJETEE) {
            log.warn("Tentative de soumission impossible pour la collecte ID: {} (statut actuel : {})", id, collecte.getStatut());
            throw new BadRequestException("Cette collecte ne peut pas être soumise car son statut actuel est : " + collecte.getStatut());
        }

        collecte.setStatut(StatutCollecte.SOUMISE);
        collecte.setDateSoumission(LocalDateTime.now());
        collecte.setMotifRejet(null); // Réinitialisation de l'ancien motif si c'était un renvoi après correction

        Collecte saved = collecteRepository.save(collecte);
        log.info("Collecte ID: {} soumise avec succès pour validation", saved.getId());

        return agentCollecteMapper.toDetailDto(saved);
    }

    @Override
    public void deleteDraft(Long id) {
        AgentCollecte agent = agentAuthService.getCurrentAgent();
        Collecte collecte = findCollecteOrThrow(id);
        verifyCollecteOwnership(collecte, agent);

        if (collecte.getStatut() != StatutCollecte.BROUILLON) {
            log.warn("Tentative de suppression interdite pour la collecte ID: {} au statut {}", id, collecte.getStatut());
            throw new BadRequestException("Seules les collectes au statut BROUILLON peuvent être supprimées.");
        }

        collecteRepository.delete(collecte);
        log.info("Brouillon de collecte ID: {} supprimé avec succès", id);
    }

    private Collecte findCollecteOrThrow(Long id) {
        return collecteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Collecte", "id", id));
    }

    private void verifyCollecteOwnership(Collecte collecte, AgentCollecte currentAgent) {
        if (collecte.getAgentCollecte() == null || !collecte.getAgentCollecte().getId().equals(currentAgent.getId())) {
            log.warn("Tentative d'accès non autorisé : l'agent ID {} tente d'accéder à la collecte ID {} appartenant à un autre agent",
                    currentAgent.getId(), collecte.getId());
            throw new ForbiddenException("Accès refusé : cette collecte ne vous appartient pas.");
        }
    }
}
