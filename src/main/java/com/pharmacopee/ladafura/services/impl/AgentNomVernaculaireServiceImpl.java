package com.pharmacopee.ladafura.services.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pharmacopee.ladafura.Models.NomPlante;
import com.pharmacopee.ladafura.Models.Plante;
import com.pharmacopee.ladafura.dto.agent.vernaculaire.AgentNomVernaculaireRequest;
import com.pharmacopee.ladafura.dto.agent.vernaculaire.AgentNomVernaculaireResponse;
import com.pharmacopee.ladafura.exceptions.ConflictException;
import com.pharmacopee.ladafura.exceptions.ResourceNotFoundException;
import com.pharmacopee.ladafura.repository.NomPlanteRepository;
import com.pharmacopee.ladafura.repository.PlanteRepository;
import com.pharmacopee.ladafura.services.interfaces.IAgentNomVernaculaireService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AgentNomVernaculaireServiceImpl implements IAgentNomVernaculaireService {

    private final NomPlanteRepository nomPlanteRepository;
    private final PlanteRepository planteRepository;

    @Override
    @Transactional(readOnly = true)
    public List<AgentNomVernaculaireResponse> getNomsByPlanteId(Long planteId) {
        log.info("Récupération des noms vernaculaires pour la plante ID {}", planteId);
        Plante plante = findPlanteOrThrow(planteId);

        List<NomPlante> noms = nomPlanteRepository.findByPlanteId(plante.getId());
        return noms.stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public AgentNomVernaculaireResponse addNomVernaculaire(Long planteId, AgentNomVernaculaireRequest request) {
        Plante plante = findPlanteOrThrow(planteId);

        String nomTrim = request.getNom().trim();
        String langueTrim = request.getLangue().trim();
        log.info("Ajout du nom vernaculaire '{}' ({}) pour la plante ID {}", nomTrim, langueTrim, planteId);

        // Règle anti-doublon stricte par plante et langue
        if (nomPlanteRepository.existsByPlanteIdAndNomIgnoreCaseAndLangueIgnoreCase(planteId, nomTrim, langueTrim)) {
            log.warn("Nom vernaculaire en doublon : '{}' ({}) pour la plante ID {}", nomTrim, langueTrim, planteId);
            throw new ConflictException("Le nom vernaculaire '" + nomTrim + "' existe déjà pour cette plante dans la langue '" + langueTrim + "'.");
        }

        NomPlante nomPlante = NomPlante.builder()
                .nom(nomTrim)
                .langue(langueTrim)
                .pays(request.getPays() != null && !request.getPays().trim().isEmpty() ? request.getPays().trim() : "Mali")
                .plante(plante)
                .build();

        NomPlante saved = nomPlanteRepository.save(nomPlante);
        log.info("Nom vernaculaire ID {} ajouté avec succès pour la plante ID {}", saved.getId(), planteId);

        return mapToResponse(saved);
    }

    @Override
    public AgentNomVernaculaireResponse updateNomVernaculaire(Long planteId, Long nomId, AgentNomVernaculaireRequest request) {
        findPlanteOrThrow(planteId);
        NomPlante nomPlante = nomPlanteRepository.findByIdAndPlanteId(nomId, planteId)
                .orElseThrow(() -> new ResourceNotFoundException("Nom vernaculaire ID " + nomId + " introuvable pour la plante ID " + planteId));

        String nomTrim = request.getNom().trim();
        String langueTrim = request.getLangue().trim();
        log.info("Mise à jour du nom vernaculaire ID {} vers '{}' ({})", nomId, nomTrim, langueTrim);

        // Vérification de doublon si le nom ou la langue ont été modifiés
        boolean nameChanged = !nomPlante.getNom().equalsIgnoreCase(nomTrim);
        boolean langChanged = !nomPlante.getLangue().equalsIgnoreCase(langueTrim);
        if ((nameChanged || langChanged) && nomPlanteRepository.existsByPlanteIdAndNomIgnoreCaseAndLangueIgnoreCase(planteId, nomTrim, langueTrim)) {
            log.warn("Modification refusée : doublon détecté pour '{}' ({}) sur la plante ID {}", nomTrim, langueTrim, planteId);
            throw new ConflictException("Le nom vernaculaire '" + nomTrim + "' existe déjà pour cette plante dans la langue '" + langueTrim + "'.");
        }

        nomPlante.setNom(nomTrim);
        nomPlante.setLangue(langueTrim);
        if (request.getPays() != null && !request.getPays().trim().isEmpty()) {
            nomPlante.setPays(request.getPays().trim());
        }

        NomPlante saved = nomPlanteRepository.save(nomPlante);
        return mapToResponse(saved);
    }

    @Override
    public void deleteNomVernaculaire(Long planteId, Long nomId) {
        findPlanteOrThrow(planteId);
        NomPlante nomPlante = nomPlanteRepository.findByIdAndPlanteId(nomId, planteId)
                .orElseThrow(() -> new ResourceNotFoundException("Nom vernaculaire ID " + nomId + " introuvable pour la plante ID " + planteId));

        log.info("Suppression du nom vernaculaire ID {} pour la plante ID {}", nomId, planteId);
        nomPlanteRepository.delete(nomPlante);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AgentNomVernaculaireResponse> searchNomsVernaculaires(String query, String langue) {
        log.info("Recherche de noms vernaculaires (query='{}', langue='{}')", query, langue);

        boolean hasQuery = (query != null && !query.trim().isEmpty());
        boolean hasLangue = (langue != null && !langue.trim().isEmpty());

        List<NomPlante> list;
        if (hasQuery && hasLangue) {
            list = nomPlanteRepository.findByNomContainingIgnoreCaseAndLangueIgnoreCase(query.trim(), langue.trim());
        } else if (hasQuery) {
            list = nomPlanteRepository.findByNomContainingIgnoreCase(query.trim());
        } else if (hasLangue) {
            list = nomPlanteRepository.findByLangueIgnoreCase(langue.trim());
        } else {
            list = nomPlanteRepository.findAll();
        }

        return list.stream()
                .map(this::mapToResponse)
                .toList();
    }

    private Plante findPlanteOrThrow(Long planteId) {
        return planteRepository.findById(planteId)
                .orElseThrow(() -> new ResourceNotFoundException("Plante", "id", planteId));
    }

    private AgentNomVernaculaireResponse mapToResponse(NomPlante nomPlante) {
        return AgentNomVernaculaireResponse.builder()
                .id(nomPlante.getId())
                .planteId(nomPlante.getPlante() != null ? nomPlante.getPlante().getId() : null)
                .nomScientifiquePlante(nomPlante.getPlante() != null ? nomPlante.getPlante().getNomScientifique() : null)
                .nom(nomPlante.getNom())
                .langue(nomPlante.getLangue())
                .pays(nomPlante.getPays())
                .build();
    }
}
