package com.pharmacopee.ladafura.services.impl;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pharmacopee.ladafura.Models.AgentCollecte;
import com.pharmacopee.ladafura.Models.Collecte;
import com.pharmacopee.ladafura.Models.Maladie;
import com.pharmacopee.ladafura.Models.Plante;
import com.pharmacopee.ladafura.Models.Source;
import com.pharmacopee.ladafura.Models.VertuDeLaPlante;
import com.pharmacopee.ladafura.dto.agent.connaissance.AgentConnaissanceRequest;
import com.pharmacopee.ladafura.dto.agent.connaissance.AgentConnaissanceResponse;
import com.pharmacopee.ladafura.dto.agent.connaissance.AgentConnaissanceUpdateRequest;
import com.pharmacopee.ladafura.enums.StatutCollecte;
import com.pharmacopee.ladafura.enums.StatutValidation;
import com.pharmacopee.ladafura.exceptions.BadRequestException;
import com.pharmacopee.ladafura.exceptions.ForbiddenException;
import com.pharmacopee.ladafura.exceptions.ResourceNotFoundException;
import com.pharmacopee.ladafura.repository.CollecteRepository;
import com.pharmacopee.ladafura.repository.MaladieRepository;
import com.pharmacopee.ladafura.repository.PlanteRepository;
import com.pharmacopee.ladafura.repository.SourceRepository;
import com.pharmacopee.ladafura.repository.VertuDeLaPlanteRepository;
import com.pharmacopee.ladafura.services.interfaces.IAgentAuthService;
import com.pharmacopee.ladafura.services.interfaces.IAgentConnaissanceService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AgentConnaissanceServiceImpl implements IAgentConnaissanceService {

    private final VertuDeLaPlanteRepository vertuDeLaPlanteRepository;
    private final CollecteRepository collecteRepository;
    private final PlanteRepository planteRepository;
    private final SourceRepository sourceRepository;
    private final MaladieRepository maladieRepository;
    private final IAgentAuthService agentAuthService;

    @Override
    @Transactional
    public AgentConnaissanceResponse enregistrerConnaissance(AgentConnaissanceRequest request) {
        AgentCollecte currentAgent = agentAuthService.getCurrentAgent();
        Collecte collecte = collecteRepository.findById(request.getCollecteId())
                .orElseThrow(() -> new ResourceNotFoundException("Collecte", "id", request.getCollecteId()));

        verifyCollecteOwnershipAndModifiability(collecte, currentAgent);

        Plante plante = planteRepository.findById(request.getPlanteId())
                .orElseThrow(() -> new ResourceNotFoundException("Plante", "id", request.getPlanteId()));

        // Association ou mise à jour de la source sur la fiche de collecte
        if (request.getSourceId() != null) {
            Source source = sourceRepository.findById(request.getSourceId())
                    .orElseThrow(() -> new ResourceNotFoundException("Source", "id", request.getSourceId()));
            collecte.setSource(source);
            collecteRepository.save(collecte);
            log.info("Source ID {} rattachée à la collecte ID {}", request.getSourceId(), collecte.getId());
        }

        // Association des maladies/symptômes traditionnellement ciblés à la plante
        if (request.getMaladieIds() != null && !request.getMaladieIds().isEmpty()) {
            List<Maladie> maladies = maladieRepository.findAllById(request.getMaladieIds());
            plante.getMaladies().addAll(maladies);
            planteRepository.save(plante);
            log.info("{} maladie(s) rattachée(s) à la plante ID {}", maladies.size(), plante.getId());
        }

        // Recherche d'une éventuelle liaison existante créée lors de l'association de la plante
        Optional<VertuDeLaPlante> existingOpt = vertuDeLaPlanteRepository.findByCollecteIdAndPlanteId(collecte.getId(), plante.getId());
        VertuDeLaPlante vertu;

        if (existingOpt.isPresent()) {
            vertu = existingOpt.get();
            vertu.setUsageRapporte(request.getUsageRapporte());
            vertu.setPartieUtilisee(request.getPartieUtilisee());
            vertu.setPreparation(request.getPreparation());
            vertu.setPrecaution(request.getPrecaution());
            vertu.setDescription(request.getDescription());
            vertu.setStatut(StatutValidation.BROUILLON);
            log.info("Connaissance traditionnelle existante ID {} mise à jour pour la collecte ID {} et plante ID {}",
                    vertu.getId(), collecte.getId(), plante.getId());
        } else {
            vertu = VertuDeLaPlante.builder()
                    .collecte(collecte)
                    .plante(plante)
                    .usageRapporte(request.getUsageRapporte())
                    .partieUtilisee(request.getPartieUtilisee())
                    .preparation(request.getPreparation())
                    .precaution(request.getPrecaution())
                    .description(request.getDescription())
                    .statut(StatutValidation.BROUILLON)
                    .build();
            log.info("Nouvelle connaissance traditionnelle créée pour la collecte ID {} et plante ID {}",
                    collecte.getId(), plante.getId());
        }

        VertuDeLaPlante savedVertu = vertuDeLaPlanteRepository.save(vertu);
        return mapToResponse(savedVertu);
    }

    @Override
    @Transactional
    public AgentConnaissanceResponse modifierConnaissance(Long id, AgentConnaissanceUpdateRequest request) {
        AgentCollecte currentAgent = agentAuthService.getCurrentAgent();
        VertuDeLaPlante vertu = vertuDeLaPlanteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("VertuDeLaPlante", "id", id));

        Collecte collecte = vertu.getCollecte();
        if (collecte != null) {
            verifyCollecteOwnershipAndModifiability(collecte, currentAgent);
        }

        vertu.setUsageRapporte(request.getUsageRapporte());
        vertu.setPartieUtilisee(request.getPartieUtilisee());
        vertu.setPreparation(request.getPreparation());
        vertu.setPrecaution(request.getPrecaution());
        vertu.setDescription(request.getDescription());

        // Mise à jour éventuelle de la source
        if (request.getSourceId() != null && collecte != null) {
            Source source = sourceRepository.findById(request.getSourceId())
                    .orElseThrow(() -> new ResourceNotFoundException("Source", "id", request.getSourceId()));
            collecte.setSource(source);
            collecteRepository.save(collecte);
        }

        // Mise à jour des maladies associées à la plante
        if (request.getMaladieIds() != null && vertu.getPlante() != null) {
            List<Maladie> maladies = maladieRepository.findAllById(request.getMaladieIds());
            vertu.getPlante().getMaladies().clear();
            vertu.getPlante().getMaladies().addAll(maladies);
            planteRepository.save(vertu.getPlante());
        }

        VertuDeLaPlante savedVertu = vertuDeLaPlanteRepository.save(vertu);
        log.info("Connaissance traditionnelle ID {} modifiée avec succès", id);
        return mapToResponse(savedVertu);
    }

    @Override
    @Transactional(readOnly = true)
    public AgentConnaissanceResponse getConnaissanceById(Long id) {
        VertuDeLaPlante vertu = vertuDeLaPlanteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("VertuDeLaPlante", "id", id));

        return mapToResponse(vertu);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AgentConnaissanceResponse> getConnaissancesByCollecte(Long collecteId) {
        AgentCollecte currentAgent = agentAuthService.getCurrentAgent();
        Collecte collecte = collecteRepository.findById(collecteId)
                .orElseThrow(() -> new ResourceNotFoundException("Collecte", "id", collecteId));

        if (collecte.getAgentCollecte() == null || !collecte.getAgentCollecte().getId().equals(currentAgent.getId())) {
            throw new ForbiddenException("Accès refusé : cette fiche de collecte ne vous appartient pas.");
        }

        List<VertuDeLaPlante> vertus = vertuDeLaPlanteRepository.findByCollecteId(collecteId);
        return vertus.stream().map(this::mapToResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AgentConnaissanceResponse> getConnaissancesByPlante(Long planteId) {
        if (!planteRepository.existsById(planteId)) {
            throw new ResourceNotFoundException("Plante", "id", planteId);
        }

        List<VertuDeLaPlante> vertus = vertuDeLaPlanteRepository.findByPlanteId(planteId);
        return vertus.stream().map(this::mapToResponse).toList();
    }

    @Override
    @Transactional
    public void supprimerConnaissance(Long id) {
        AgentCollecte currentAgent = agentAuthService.getCurrentAgent();
        VertuDeLaPlante vertu = vertuDeLaPlanteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("VertuDeLaPlante", "id", id));

        Collecte collecte = vertu.getCollecte();
        if (collecte != null) {
            if (collecte.getAgentCollecte() == null || !collecte.getAgentCollecte().getId().equals(currentAgent.getId())) {
                throw new ForbiddenException("Accès refusé : cette collecte ne vous appartient pas.");
            }
            if (collecte.getStatut() != StatutCollecte.BROUILLON) {
                throw new BadRequestException("Impossible de supprimer une connaissance d'une collecte qui n'est plus en statut BROUILLON.");
            }
        }

        vertuDeLaPlanteRepository.delete(vertu);
        log.info("Connaissance traditionnelle ID {} supprimée avec succès", id);
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

    private AgentConnaissanceResponse mapToResponse(VertuDeLaPlante vertu) {
        Plante plante = vertu.getPlante();
        Collecte collecte = vertu.getCollecte();
        Source source = (collecte != null) ? collecte.getSource() : null;

        List<String> nomsVernaculaires = (plante != null && plante.getNomsPlante() != null)
                ? plante.getNomsPlante().stream()
                        .map(n -> n.getNom() + (n.getLangue() != null ? " (" + n.getLangue() + ")" : ""))
                        .toList()
                : Collections.emptyList();

        List<String> maladiesAssociees = (plante != null && plante.getMaladies() != null)
                ? plante.getMaladies().stream()
                        .map(Maladie::getNom)
                        .toList()
                : Collections.emptyList();

        return AgentConnaissanceResponse.builder()
                .id(vertu.getId())
                .collecteId(collecte != null ? collecte.getId() : null)
                .planteId(plante != null ? plante.getId() : null)
                .planteNomScientifique(plante != null ? plante.getNomScientifique() : null)
                .nomsVernaculaires(nomsVernaculaires)
                .usageRapporte(vertu.getUsageRapporte())
                .partieUtilisee(vertu.getPartieUtilisee())
                .preparation(vertu.getPreparation())
                .precaution(vertu.getPrecaution())
                .description(vertu.getDescription())
                .statut(vertu.getStatut())
                .sourceId(source != null ? source.getId() : null)
                .sourceNomComplet(source != null ? source.getPrenom() + " " + source.getNom() : null)
                .sourceSpecialite(source != null ? source.getSpecialite() : null)
                .maladiesAssociees(maladiesAssociees)
                .preuveScientifique(false)
                .typeInformation("CONNAISSANCE_TRADITIONNELLE")
                .avertissementLegal("Information issue du savoir traditionnel local rapporté sur le terrain. Ne constitue ni une preuve clinique ou scientifique, ni une prescription médicale.")
                .build();
    }
}
