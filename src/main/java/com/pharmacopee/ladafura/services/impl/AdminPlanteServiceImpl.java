package com.pharmacopee.ladafura.services.impl;

import java.util.HashSet;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pharmacopee.ladafura.Models.Maladie;
import com.pharmacopee.ladafura.Models.NomPlante;
import com.pharmacopee.ladafura.Models.Plante;
import com.pharmacopee.ladafura.Models.VertuDeLaPlante;
import com.pharmacopee.ladafura.dto.admin.plante.AdminModerateVertuRequest;
import com.pharmacopee.ladafura.dto.admin.plante.AdminPlanteRequest;
import com.pharmacopee.ladafura.dto.admin.plante.AdminPlanteResponse;
import com.pharmacopee.ladafura.dto.admin.plante.AdminVertuResponse;
import com.pharmacopee.ladafura.enums.StatutPlante;
import com.pharmacopee.ladafura.exceptions.ConflictException;
import com.pharmacopee.ladafura.exceptions.ResourceNotFoundException;
import com.pharmacopee.ladafura.mappers.AdminPlanteMapper;
import com.pharmacopee.ladafura.repository.MaladieRepository;
import com.pharmacopee.ladafura.repository.PlanteRepository;
import com.pharmacopee.ladafura.repository.VertuDeLaPlanteRepository;
import com.pharmacopee.ladafura.services.interfaces.IAdminPlanteService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AdminPlanteServiceImpl implements IAdminPlanteService {

    private final PlanteRepository planteRepository;
    private final MaladieRepository maladieRepository;
    private final VertuDeLaPlanteRepository vertuDeLaPlanteRepository;
    private final AdminPlanteMapper planteMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<AdminPlanteResponse> getAllPlantes(StatutPlante statut, Pageable pageable) {
        log.info("Récupération paginée des plantes (statut={})", statut);
        Page<Plante> page = (statut != null)
                ? planteRepository.findByStatut(statut, pageable)
                : planteRepository.findAll(pageable);

        return page.map(planteMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminPlanteResponse getPlanteById(Long id) {
        log.info("Consultation de la plante {}", id);
        Plante plante = planteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Plante", "id", id));

        return planteMapper.toDto(plante);
    }

    @Override
    public AdminPlanteResponse createPlante(AdminPlanteRequest request) {
        log.info("Création d'une nouvelle plante : {}", request.getNomScientifique());

        if (planteRepository.existsByNomScientifiqueIgnoreCase(request.getNomScientifique())) {
            throw new ConflictException("Plante", "nomScientifique", request.getNomScientifique());
        }

        String imageVal = request.getImage() != null ? request.getImage() : request.getPhotoUrl();
        Plante plante = Plante.builder()
                .nomScientifique(request.getNomScientifique().trim())
                .description(request.getDescription())
                .photoUrl(request.getPhotoUrl() != null ? request.getPhotoUrl() : imageVal)
                .image(imageVal)
                .statut(request.getStatut() != null ? request.getStatut() : StatutPlante.BROUILLON)
                .build();

        if (request.getMaladieIds() != null && !request.getMaladieIds().isEmpty()) {
            List<Maladie> maladies = maladieRepository.findAllById(request.getMaladieIds());
            plante.setMaladies(new HashSet<>(maladies));
        }

        if (request.getNomsVernaculaires() != null) {
            request.getNomsVernaculaires().forEach(dto -> {
                NomPlante np = planteMapper.toNomPlanteEntity(dto);
                np.setPlante(plante);
                plante.getNomsPlante().add(np);
            });
        }

        Plante saved = planteRepository.save(plante);
        return planteMapper.toDto(saved);
    }

    @Override
    public AdminPlanteResponse updatePlante(Long id, AdminPlanteRequest request) {
        log.info("Mise à jour de la plante {}", id);
        Plante plante = planteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Plante", "id", id));

        if (!plante.getNomScientifique().equalsIgnoreCase(request.getNomScientifique())
                && planteRepository.existsByNomScientifiqueIgnoreCase(request.getNomScientifique())) {
            throw new ConflictException("Plante", "nomScientifique", request.getNomScientifique());
        }

        plante.setNomScientifique(request.getNomScientifique().trim());
        plante.setDescription(request.getDescription());
        if (request.getImage() != null) {
            plante.setImage(request.getImage());
        }
        if (request.getPhotoUrl() != null) {
            plante.setPhotoUrl(request.getPhotoUrl());
            if (plante.getImage() == null) {
                plante.setImage(request.getPhotoUrl());
            }
        }
        if (request.getStatut() != null) {
            plante.setStatut(request.getStatut());
        }

        if (request.getMaladieIds() != null) {
            List<Maladie> maladies = maladieRepository.findAllById(request.getMaladieIds());
            plante.setMaladies(new HashSet<>(maladies));
        }

        if (request.getNomsVernaculaires() != null) {
            plante.getNomsPlante().clear();
            request.getNomsVernaculaires().forEach(dto -> {
                NomPlante np = planteMapper.toNomPlanteEntity(dto);
                np.setPlante(plante);
                plante.getNomsPlante().add(np);
            });
        }

        Plante updated = planteRepository.save(plante);
        return planteMapper.toDto(updated);
    }

    @Override
    public AdminPlanteResponse moderatePlante(Long id, StatutPlante statut) {
        log.info("Modération de la plante {} avec le statut {}", id, statut);
        Plante plante = planteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Plante", "id", id));

        plante.setStatut(statut);
        Plante saved = planteRepository.save(plante);
        return planteMapper.toDto(saved);
    }

    @Override
    public AdminVertuResponse moderateVertu(Long vertuId, AdminModerateVertuRequest request) {
        log.info("Modération de la vertu {} avec l'action {}", vertuId, request.getAction());
        VertuDeLaPlante vertu = vertuDeLaPlanteRepository.findById(vertuId)
                .orElseThrow(() -> new ResourceNotFoundException("VertuDeLaPlante", "id", vertuId));

        vertu.setStatut(request.getAction());
        VertuDeLaPlante saved = vertuDeLaPlanteRepository.save(vertu);
        return planteMapper.toVertuDto(saved);
    }

    @Override
    public void deletePlante(Long id) {
        log.info("Suppression de la plante {}", id);
        Plante plante = planteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Plante", "id", id));

        planteRepository.delete(plante);
    }
}
