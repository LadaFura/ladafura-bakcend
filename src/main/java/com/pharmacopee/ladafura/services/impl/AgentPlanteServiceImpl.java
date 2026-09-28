package com.pharmacopee.ladafura.services.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pharmacopee.ladafura.Models.AgentCollecte;
import com.pharmacopee.ladafura.Models.Collecte;
import com.pharmacopee.ladafura.Models.NomPlante;
import com.pharmacopee.ladafura.Models.Plante;
import com.pharmacopee.ladafura.Models.VertuDeLaPlante;
import com.pharmacopee.ladafura.dto.admin.plante.NomPlanteDto;
import com.pharmacopee.ladafura.dto.agent.plante.AgentCreatePlanteRequest;
import com.pharmacopee.ladafura.dto.agent.plante.AgentPlanteAssociationResponse;
import com.pharmacopee.ladafura.dto.agent.plante.AgentPlanteResponse;
import com.pharmacopee.ladafura.enums.StatutCollecte;
import com.pharmacopee.ladafura.enums.StatutPlante;
import com.pharmacopee.ladafura.enums.StatutValidation;
import com.pharmacopee.ladafura.exceptions.BadRequestException;
import com.pharmacopee.ladafura.exceptions.ConflictException;
import com.pharmacopee.ladafura.exceptions.ForbiddenException;
import com.pharmacopee.ladafura.exceptions.ResourceNotFoundException;
import com.pharmacopee.ladafura.repository.CollecteRepository;
import com.pharmacopee.ladafura.repository.NomPlanteRepository;
import com.pharmacopee.ladafura.repository.PlanteRepository;
import com.pharmacopee.ladafura.repository.VertuDeLaPlanteRepository;
import com.pharmacopee.ladafura.services.interfaces.IAgentAuthService;
import com.pharmacopee.ladafura.services.interfaces.IAgentPlanteService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AgentPlanteServiceImpl implements IAgentPlanteService {

    private final PlanteRepository planteRepository;
    private final NomPlanteRepository nomPlanteRepository;
    private final CollecteRepository collecteRepository;
    private final VertuDeLaPlanteRepository vertuDeLaPlanteRepository;
    private final IAgentAuthService agentAuthService;

    @Override
    @Transactional(readOnly = true)
    public Page<AgentPlanteResponse> searchPlantes(String keyword, Pageable pageable) {
        log.info("Recherche de plantes par l'agent (keyword={})", keyword);

        Page<Plante> page;
        if (keyword != null && !keyword.trim().isEmpty()) {
            page = planteRepository.searchByKeyword(keyword.trim(), pageable);
        } else {
            page = planteRepository.findAll(pageable);
        }

        return page.map(this::mapToPlanteResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public AgentPlanteResponse getPlanteById(Long id) {
        log.info("Consultation de la plante ID: {}", id);
        Plante plante = planteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Plante", "id", id));
        return mapToPlanteResponse(plante);
    }

    @Override
    public AgentPlanteResponse createPlante(AgentCreatePlanteRequest request) {
        String nomScientifique = request.getNomScientifique().trim();
        log.info("Création d'une nouvelle plante par l'agent : '{}'", nomScientifique);

        // Règle anti-doublon par nom scientifique
        if (planteRepository.existsByNomScientifiqueIgnoreCase(nomScientifique)) {
            log.warn("Tentative de création d'une plante en doublon : '{}'", nomScientifique);
            throw new ConflictException("Plante", "nomScientifique", nomScientifique);
        }

        Plante plante = Plante.builder()
                .nomScientifique(nomScientifique)
                .description(request.getDescription())
                .photoUrl(request.getPhotoUrl())
                .statut(StatutPlante.BROUILLON) // Les plantes créées sur le terrain naissent en statut BROUILLON
                .nomsPlante(new ArrayList<>())
                .build();

        Plante savedPlante = planteRepository.save(plante);

        // Association des noms vernaculaires initiaux
        if (request.getNomsVernaculaires() != null && !request.getNomsVernaculaires().isEmpty()) {
            for (NomPlanteDto dto : request.getNomsVernaculaires()) {
                if (dto.getNom() != null && !dto.getNom().trim().isEmpty()) {
                    NomPlante nomPlante = NomPlante.builder()
                            .nom(dto.getNom().trim())
                            .langue(dto.getLangue())
                            .pays(dto.getPays() != null ? dto.getPays() : "Mali")
                            .plante(savedPlante)
                            .build();
                    nomPlanteRepository.save(nomPlante);
                    savedPlante.getNomsPlante().add(nomPlante);
                }
            }
        }

        log.info("Plante créée avec succès : ID {} ('{}')", savedPlante.getId(), savedPlante.getNomScientifique());
        return mapToPlanteResponse(savedPlante);
    }

    @Override
    public AgentPlanteAssociationResponse associatePlanteToCollecte(Long collecteId, Long planteId) {
        AgentCollecte currentAgent = agentAuthService.getCurrentAgent();
        Collecte collecte = collecteRepository.findById(collecteId)
                .orElseThrow(() -> new ResourceNotFoundException("Collecte", "id", collecteId));

        verifyCollecteOwnershipAndModifiability(collecte, currentAgent);

        Plante plante = planteRepository.findById(planteId)
                .orElseThrow(() -> new ResourceNotFoundException("Plante", "id", planteId));

        // Règle anti-doublon : vérifier si déjà associée dans cette collecte
        Optional<VertuDeLaPlante> existing = vertuDeLaPlanteRepository.findByCollecteIdAndPlanteId(collecteId, planteId);
        if (existing.isPresent()) {
            log.info("La plante ID {} est déjà associée à la collecte ID {}", planteId, collecteId);
            return mapToAssociationResponse(existing.get());
        }

        VertuDeLaPlante vertu = VertuDeLaPlante.builder()
                .collecte(collecte)
                .plante(plante)
                .statut(StatutValidation.BROUILLON)
                .build();

        VertuDeLaPlante savedVertu = vertuDeLaPlanteRepository.save(vertu);
        collecte.getVertus().add(savedVertu);
        log.info("Plante ID {} associée avec succès à la collecte ID {}", planteId, collecteId);

        return mapToAssociationResponse(savedVertu);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AgentPlanteAssociationResponse> getPlantesByCollecte(Long collecteId) {
        AgentCollecte currentAgent = agentAuthService.getCurrentAgent();
        Collecte collecte = collecteRepository.findById(collecteId)
                .orElseThrow(() -> new ResourceNotFoundException("Collecte", "id", collecteId));

        if (collecte.getAgentCollecte() == null || !collecte.getAgentCollecte().getId().equals(currentAgent.getId())) {
            throw new ForbiddenException("Accès refusé : cette collecte ne vous appartient pas.");
        }

        List<VertuDeLaPlante> vertus = vertuDeLaPlanteRepository.findByCollecteId(collecteId);
        return vertus.stream()
                .map(this::mapToAssociationResponse)
                .toList();
    }

    @Override
    public void dissociatePlanteFromCollecte(Long collecteId, Long planteId) {
        AgentCollecte currentAgent = agentAuthService.getCurrentAgent();
        Collecte collecte = collecteRepository.findById(collecteId)
                .orElseThrow(() -> new ResourceNotFoundException("Collecte", "id", collecteId));

        verifyCollecteOwnershipAndModifiability(collecte, currentAgent);

        VertuDeLaPlante vertu = vertuDeLaPlanteRepository.findByCollecteIdAndPlanteId(collecteId, planteId)
                .orElseThrow(() -> new ResourceNotFoundException("Association plante-collecte introuvable"));

        vertuDeLaPlanteRepository.delete(vertu);
        collecte.getVertus().remove(vertu);
        log.info("Plante ID {} dissociée de la collecte ID {}", planteId, collecteId);
    }

    private void verifyCollecteOwnershipAndModifiability(Collecte collecte, AgentCollecte currentAgent) {
        if (collecte.getAgentCollecte() == null || !collecte.getAgentCollecte().getId().equals(currentAgent.getId())) {
            throw new ForbiddenException("Accès refusé : cette collecte ne vous appartient pas.");
        }

        if (collecte.getStatut() != StatutCollecte.BROUILLON && collecte.getStatut() != StatutCollecte.REJETEE) {
            throw new BadRequestException("Impossible de modifier une collecte avec le statut " + collecte.getStatut()
                    + ". Seules les collectes en statut BROUILLON ou REJETEE peuvent être modifiées.");
        }
    }

    private AgentPlanteResponse mapToPlanteResponse(Plante plante) {
        List<NomPlanteDto> nomsDtos = (plante.getNomsPlante() != null)
                ? plante.getNomsPlante().stream()
                        .map(n -> NomPlanteDto.builder()
                                .id(n.getId())
                                .nom(n.getNom())
                                .langue(n.getLangue())
                                .pays(n.getPays())
                                .build())
                        .toList()
                : Collections.emptyList();

        int nbVertus = (plante.getVertus() != null) ? plante.getVertus().size() : 0;

        return AgentPlanteResponse.builder()
                .id(plante.getId())
                .nomScientifique(plante.getNomScientifique())
                .description(plante.getDescription())
                .photoUrl(plante.getPhotoUrl())
                .statut(plante.getStatut())
                .nomsVernaculaires(nomsDtos)
                .nombreConnaissancesTraditionnelles(nbVertus)
                .build();
    }

    private AgentPlanteAssociationResponse mapToAssociationResponse(VertuDeLaPlante vertu) {
        Plante p = vertu.getPlante();
        List<String> nomsVernaculaires = (p != null && p.getNomsPlante() != null)
                ? p.getNomsPlante().stream()
                        .map(n -> n.getLangue() != null ? String.format("%s (%s)", n.getNom(), n.getLangue()) : n.getNom())
                        .toList()
                : Collections.emptyList();

        return AgentPlanteAssociationResponse.builder()
                .collecteId(vertu.getCollecte() != null ? vertu.getCollecte().getId() : null)
                .planteId(p != null ? p.getId() : null)
                .nomScientifique(p != null ? p.getNomScientifique() : null)
                .descriptionPlante(p != null ? p.getDescription() : null)
                .photoUrl(p != null ? p.getPhotoUrl() : null)
                .nomsVernaculaires(nomsVernaculaires)
                .vertuId(vertu.getId())
                .usageRapporte(vertu.getUsageRapporte())
                .statutAssociation(vertu.getStatut())
                .build();
    }
}
