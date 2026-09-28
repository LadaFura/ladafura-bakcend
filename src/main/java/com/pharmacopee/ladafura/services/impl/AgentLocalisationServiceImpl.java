package com.pharmacopee.ladafura.services.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pharmacopee.ladafura.Models.AgentCollecte;
import com.pharmacopee.ladafura.Models.Collecte;
import com.pharmacopee.ladafura.Models.Localisation;
import com.pharmacopee.ladafura.dto.agent.localisation.AgentLocalisationRequest;
import com.pharmacopee.ladafura.dto.agent.localisation.AgentLocalisationResponse;
import com.pharmacopee.ladafura.enums.StatutCollecte;
import com.pharmacopee.ladafura.exceptions.BadRequestException;
import com.pharmacopee.ladafura.exceptions.ForbiddenException;
import com.pharmacopee.ladafura.exceptions.ResourceNotFoundException;
import com.pharmacopee.ladafura.repository.CollecteRepository;
import com.pharmacopee.ladafura.repository.LocalisationRepository;
import com.pharmacopee.ladafura.services.interfaces.IAgentAuthService;
import com.pharmacopee.ladafura.services.interfaces.IAgentLocalisationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AgentLocalisationServiceImpl implements IAgentLocalisationService {

    private final CollecteRepository collecteRepository;
    private final LocalisationRepository localisationRepository;
    private final IAgentAuthService agentAuthService;

    @Override
    @Transactional
    public AgentLocalisationResponse saveOrUpdateLocalisation(Long collecteId, AgentLocalisationRequest request) {
        AgentCollecte currentAgent = agentAuthService.getCurrentAgent();
        Collecte collecte = collecteRepository.findById(collecteId)
                .orElseThrow(() -> new ResourceNotFoundException("Collecte", "id", collecteId));

        verifyCollecteOwnershipAndModifiability(collecte, currentAgent);

        Localisation loc = collecte.getLocalisation();
        if (loc != null) {
            loc.setRegion(request.getRegion().trim());
            loc.setCercle(request.getCercle().trim());
            loc.setCommune(request.getCommune().trim());
            loc.setLocalite(request.getLocalite().trim());
            loc.setLatitude(request.getLatitude());
            loc.setLongitude(request.getLongitude());
            log.info("Localisation existante ID {} mise à jour pour la collecte ID {}", loc.getId(), collecteId);
        } else {
            loc = Localisation.builder()
                    .region(request.getRegion().trim())
                    .cercle(request.getCercle().trim())
                    .commune(request.getCommune().trim())
                    .localite(request.getLocalite().trim())
                    .latitude(request.getLatitude())
                    .longitude(request.getLongitude())
                    .build();
            log.info("Nouvelle localisation créée pour la collecte ID {}", collecteId);
        }

        Localisation savedLoc = localisationRepository.save(loc);
        collecte.setLocalisation(savedLoc);
        collecteRepository.save(collecte);

        return mapToResponse(collecte.getId(), savedLoc);
    }

    @Override
    @Transactional(readOnly = true)
    public AgentLocalisationResponse getLocalisationByCollecte(Long collecteId) {
        AgentCollecte currentAgent = agentAuthService.getCurrentAgent();
        Collecte collecte = collecteRepository.findById(collecteId)
                .orElseThrow(() -> new ResourceNotFoundException("Collecte", "id", collecteId));

        if (collecte.getAgentCollecte() == null || !collecte.getAgentCollecte().getId().equals(currentAgent.getId())) {
            throw new ForbiddenException("Accès refusé : cette collecte ne vous appartient pas.");
        }

        if (collecte.getLocalisation() == null) {
            throw new ResourceNotFoundException("Localisation", "collecteId", collecteId);
        }

        return mapToResponse(collecte.getId(), collecte.getLocalisation());
    }

    @Override
    @Transactional
    public void supprimerLocalisation(Long collecteId) {
        AgentCollecte currentAgent = agentAuthService.getCurrentAgent();
        Collecte collecte = collecteRepository.findById(collecteId)
                .orElseThrow(() -> new ResourceNotFoundException("Collecte", "id", collecteId));

        verifyCollecteOwnershipAndModifiability(collecte, currentAgent);

        collecte.setLocalisation(null);
        collecteRepository.save(collecte);
        log.info("Localisation dissociée avec succès de la collecte ID {}", collecteId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AgentLocalisationResponse> rechercherLocalisations(String query, Pageable pageable) {
        Page<Localisation> localisations = localisationRepository.searchLocalisations(query, pageable);
        return localisations.map(loc -> mapToResponse(null, loc));
    }

    private void verifyCollecteOwnershipAndModifiability(Collecte collecte, AgentCollecte currentAgent) {
        if (collecte.getAgentCollecte() == null || !collecte.getAgentCollecte().getId().equals(currentAgent.getId())) {
            throw new ForbiddenException("Accès refusé : cette collecte ne vous appartient pas.");
        }

        if (collecte.getStatut() != StatutCollecte.BROUILLON && collecte.getStatut() != StatutCollecte.REJETEE) {
            throw new BadRequestException("Impossible de modifier la localisation d'une collecte avec le statut " + collecte.getStatut()
                    + ". Seules les collectes en statut BROUILLON ou REJETEE peuvent être modifiées.");
        }
    }

    private AgentLocalisationResponse mapToResponse(Long collecteId, Localisation loc) {
        boolean hasGps = loc.getLatitude() != null && loc.getLongitude() != null;
        String formatted = String.format("%s, %s, %s, %s",
                loc.getLocalite(), loc.getCommune(), loc.getCercle(), loc.getRegion());

        return AgentLocalisationResponse.builder()
                .collecteId(collecteId)
                .localisationId(loc.getId())
                .region(loc.getRegion())
                .cercle(loc.getCercle())
                .commune(loc.getCommune())
                .localite(loc.getLocalite())
                .latitude(loc.getLatitude())
                .longitude(loc.getLongitude())
                .coordonneesGpsPresentes(hasGps)
                .adresseFormatee(formatted)
                .build();
    }
}
