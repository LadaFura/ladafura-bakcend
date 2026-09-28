package com.pharmacopee.ladafura.services.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pharmacopee.ladafura.Models.EtudeScientifique;
import com.pharmacopee.ladafura.Models.Plante;
import com.pharmacopee.ladafura.dto.admin.etude.AdminEtudeRequest;
import com.pharmacopee.ladafura.dto.admin.etude.AdminEtudeResponse;
import com.pharmacopee.ladafura.exceptions.ResourceNotFoundException;
import com.pharmacopee.ladafura.mappers.AdminEtudeMapper;
import com.pharmacopee.ladafura.repository.EtudeScientifiqueRepository;
import com.pharmacopee.ladafura.repository.PlanteRepository;
import com.pharmacopee.ladafura.services.interfaces.IAdminEtudeScientifiqueService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AdminEtudeScientifiqueServiceImpl implements IAdminEtudeScientifiqueService {

    private final EtudeScientifiqueRepository etudeScientifiqueRepository;
    private final PlanteRepository planteRepository;
    private final AdminEtudeMapper etudeMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<AdminEtudeResponse> getAllEtudes(Pageable pageable) {
        log.info("Récupération paginée de toutes les études scientifiques");
        return etudeScientifiqueRepository.findAll(pageable)
                .map(etudeMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminEtudeResponse> getEtudesByPlanteId(Long planteId) {
        log.info("Récupération des études scientifiques pour la plante {}", planteId);
        if (!planteRepository.existsById(planteId)) {
            throw new ResourceNotFoundException("Plante", "id", planteId);
        }

        return etudeScientifiqueRepository.findByPlanteId(planteId).stream()
                .map(etudeMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public AdminEtudeResponse getEtudeById(Long id) {
        log.info("Consultation de l'étude scientifique {}", id);
        EtudeScientifique etude = etudeScientifiqueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Étude Scientifique", "id", id));

        return etudeMapper.toDto(etude);
    }

    @Override
    public AdminEtudeResponse createEtude(AdminEtudeRequest request) {
        log.info("Création d'une étude scientifique pour la plante {}", request.getPlanteId());
        Plante plante = planteRepository.findById(request.getPlanteId())
                .orElseThrow(() -> new ResourceNotFoundException("Plante", "id", request.getPlanteId()));

        EtudeScientifique etude = etudeMapper.toEntity(request);
        etude.setPlante(plante);

        EtudeScientifique saved = etudeScientifiqueRepository.save(etude);
        return etudeMapper.toDto(saved);
    }

    @Override
    public AdminEtudeResponse updateEtude(Long id, AdminEtudeRequest request) {
        log.info("Mise à jour de l'étude scientifique {}", id);
        EtudeScientifique etude = etudeScientifiqueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Étude Scientifique", "id", id));

        if (!etude.getPlante().getId().equals(request.getPlanteId())) {
            Plante nouvellePlante = planteRepository.findById(request.getPlanteId())
                    .orElseThrow(() -> new ResourceNotFoundException("Plante", "id", request.getPlanteId()));
            etude.setPlante(nouvellePlante);
        }

        etude.setTitre(request.getTitre());
        etude.setAuteurs(request.getAuteurs());
        etude.setAnnee(request.getAnnee());
        etude.setReference(request.getReference());
        etude.setResume(request.getResume());
        etude.setDocumentUrl(request.getDocumentUrl());

        EtudeScientifique updated = etudeScientifiqueRepository.save(etude);
        return etudeMapper.toDto(updated);
    }

    @Override
    public void deleteEtude(Long id) {
        log.info("Suppression de l'étude scientifique {}", id);
        EtudeScientifique etude = etudeScientifiqueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Étude Scientifique", "id", id));

        etudeScientifiqueRepository.delete(etude);
    }
}
