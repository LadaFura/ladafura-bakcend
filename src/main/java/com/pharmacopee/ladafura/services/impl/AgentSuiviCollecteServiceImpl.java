package com.pharmacopee.ladafura.services.impl;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pharmacopee.ladafura.Models.AgentCollecte;
import com.pharmacopee.ladafura.Models.Collecte;
import com.pharmacopee.ladafura.Models.VertuDeLaPlante;
import com.pharmacopee.ladafura.dto.agent.collecte.AgentCollecteSummaryResponse;
import com.pharmacopee.ladafura.dto.agent.suivi.AgentCollecteRejetDetailResponse;
import com.pharmacopee.ladafura.dto.agent.suivi.AgentSuiviStatsResponse;
import com.pharmacopee.ladafura.enums.StatutCollecte;
import com.pharmacopee.ladafura.exceptions.BadRequestException;
import com.pharmacopee.ladafura.exceptions.ForbiddenException;
import com.pharmacopee.ladafura.exceptions.ResourceNotFoundException;
import com.pharmacopee.ladafura.mappers.AgentCollecteMapper;
import com.pharmacopee.ladafura.repository.CollecteRepository;
import com.pharmacopee.ladafura.services.interfaces.IAgentAuthService;
import com.pharmacopee.ladafura.services.interfaces.IAgentSuiviCollecteService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AgentSuiviCollecteServiceImpl implements IAgentSuiviCollecteService {

    private final CollecteRepository collecteRepository;
    private final AgentCollecteMapper agentCollecteMapper;
    private final IAgentAuthService agentAuthService;

    @Override
    @Transactional(readOnly = true)
    public AgentSuiviStatsResponse getStatsSuivi() {
        AgentCollecte currentAgent = agentAuthService.getCurrentAgent();
        Long agentId = currentAgent.getId();

        long total = collecteRepository.countByAgentCollecteId(agentId);
        long brouillons = collecteRepository.countByAgentCollecteIdAndStatut(agentId, StatutCollecte.BROUILLON);
        long soumises = collecteRepository.countByAgentCollecteIdAndStatut(agentId, StatutCollecte.SOUMISE);
        long enExamen = collecteRepository.countByAgentCollecteIdAndStatut(agentId, StatutCollecte.EN_EXAMEN);
        long validees = collecteRepository.countByAgentCollecteIdAndStatut(agentId, StatutCollecte.VALIDEE);
        long rejetees = collecteRepository.countByAgentCollecteIdAndStatut(agentId, StatutCollecte.REJETEE);

        long instruites = validees + rejetees;
        double tauxValidation = 0.0;
        if (instruites > 0) {
            tauxValidation = Math.round(((double) validees / (double) instruites) * 1000.0) / 10.0;
        }

        log.info("Stats de suivi pour agent ID {} : total={}, brouillons={}, soumises={}, enExamen={}, validées={}, rejetées={}",
                agentId, total, brouillons, soumises, enExamen, validees, rejetees);

        return AgentSuiviStatsResponse.builder()
                .total(total)
                .brouillons(brouillons)
                .soumises(soumises)
                .enExamen(enExamen)
                .validees(validees)
                .rejetees(rejetees)
                .necessitantCorrection(rejetees)
                .tauxValidation(tauxValidation)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AgentCollecteSummaryResponse> getCollectesParStatut(StatutCollecte statut, Pageable pageable) {
        AgentCollecte currentAgent = agentAuthService.getCurrentAgent();
        Page<Collecte> page;
        if (statut != null) {
            page = collecteRepository.findByAgentCollecteIdAndStatut(currentAgent.getId(), statut, pageable);
        } else {
            page = collecteRepository.findByAgentCollecteId(currentAgent.getId(), pageable);
        }
        return page.map(agentCollecteMapper::toSummaryDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AgentCollecteSummaryResponse> getBrouillons(Pageable pageable) {
        return getCollectesParStatut(StatutCollecte.BROUILLON, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AgentCollecteSummaryResponse> getSoumises(Pageable pageable) {
        return getCollectesParStatut(StatutCollecte.SOUMISE, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AgentCollecteSummaryResponse> getEnExamen(Pageable pageable) {
        return getCollectesParStatut(StatutCollecte.EN_EXAMEN, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AgentCollecteSummaryResponse> getValidees(Pageable pageable) {
        return getCollectesParStatut(StatutCollecte.VALIDEE, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AgentCollecteSummaryResponse> getRejetees(Pageable pageable) {
        return getCollectesParStatut(StatutCollecte.REJETEE, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public AgentCollecteRejetDetailResponse getDetailRejet(Long collecteId) {
        AgentCollecte currentAgent = agentAuthService.getCurrentAgent();
        Collecte collecte = collecteRepository.findById(collecteId)
                .orElseThrow(() -> new ResourceNotFoundException("Collecte", "id", collecteId));

        verifyCollecteOwnership(collecte, currentAgent);

        if (collecte.getStatut() != StatutCollecte.REJETEE) {
            log.warn("Consultation motif de rejet non permise pour la collecte ID {} (statut : {})", collecteId, collecte.getStatut());
            throw new BadRequestException("Cette collecte n'est pas au statut REJETEE mais au statut " + collecte.getStatut()
                    + ". Les motifs de rejet ne sont consultables que pour les fiches rejetées nécessitant correction.");
        }

        String planteNomScientifique = null;
        if (collecte.getVertus() != null && !collecte.getVertus().isEmpty()) {
            VertuDeLaPlante firstVertu = collecte.getVertus().get(0);
            if (firstVertu != null && firstVertu.getPlante() != null) {
                planteNomScientifique = firstVertu.getPlante().getNomScientifique();
            }
        }

        String nomSource = null;
        if (collecte.getSource() != null) {
            nomSource = collecte.getSource().getPrenom() + " " + collecte.getSource().getNom();
        }

        String localite = null;
        String region = null;
        if (collecte.getLocalisation() != null) {
            localite = collecte.getLocalisation().getLocalite();
            region = collecte.getLocalisation().getRegion();
        }

        List<String> guide = List.of(
                "1. Examiner attentivement le motif officiel communiqué par l'examinateur ci-dessus.",
                "2. Modifier et enrichir les informations concernées (plante, vernaculaires, vertus, photo/audio, localisation).",
                "3. Vérifier le récapitulatif complet de conformité avant soumission (GET /api/v1/agent/collectes/" + collecteId + "/recapitulatif).",
                "4. Re-soumettre formellement la fiche corrigée (POST /api/v1/agent/collectes/" + collecteId + "/soumettre)."
        );

        return AgentCollecteRejetDetailResponse.builder()
                .collecteId(collecte.getId())
                .dateCollecte(collecte.getDateCollecte())
                .dateSoumission(collecte.getDateSoumission())
                .statut(collecte.getStatut())
                .motifRejet(collecte.getMotifRejet() != null && !collecte.getMotifRejet().isBlank()
                        ? collecte.getMotifRejet()
                        : "Aucun motif spécifique renseigné par l'examinateur.")
                .planteNomScientifique(planteNomScientifique)
                .localite(localite)
                .region(region)
                .sourceNomComplet(nomSource)
                .modifiable(true)
                .guideCorrection(guide)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AgentCollecteSummaryResponse> searchSuiviCollectes(StatutCollecte statut, String query, Pageable pageable) {
        AgentCollecte currentAgent = agentAuthService.getCurrentAgent();
        if (query == null || query.isBlank()) {
            return getCollectesParStatut(statut, pageable);
        }
        Page<Collecte> page = collecteRepository.searchSuiviCollectes(currentAgent.getId(), statut, query.trim(), pageable);
        return page.map(agentCollecteMapper::toSummaryDto);
    }

    private void verifyCollecteOwnership(Collecte collecte, AgentCollecte currentAgent) {
        if (collecte.getAgentCollecte() == null || !collecte.getAgentCollecte().getId().equals(currentAgent.getId())) {
            throw new ForbiddenException("Accès refusé : cette collecte ne vous appartient pas.");
        }
    }
}
