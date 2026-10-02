package com.pharmacopee.ladafura.services.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pharmacopee.ladafura.Models.Maladie;
import com.pharmacopee.ladafura.Models.NomPlante;
import com.pharmacopee.ladafura.Models.Plante;
import com.pharmacopee.ladafura.dto.admin.maladie.AdminMaladiePlanteDto;
import com.pharmacopee.ladafura.dto.admin.maladie.AdminMaladieRequest;
import com.pharmacopee.ladafura.dto.admin.maladie.AdminMaladieResponse;
import com.pharmacopee.ladafura.exceptions.ConflictException;
import com.pharmacopee.ladafura.exceptions.ResourceNotFoundException;
import com.pharmacopee.ladafura.repository.MaladieRepository;
import com.pharmacopee.ladafura.repository.PlanteRepository;
import com.pharmacopee.ladafura.services.interfaces.IAdminMaladieService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AdminMaladieServiceImpl implements IAdminMaladieService {

    private final MaladieRepository maladieRepository;
    private final PlanteRepository planteRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<AdminMaladieResponse> getAllMaladies(String search, Pageable pageable) {
        log.info("Récupération paginée des maladies (search={})", search);
        Page<Maladie> page = (search != null && !search.trim().isEmpty())
                ? maladieRepository.findByNomContainingIgnoreCase(search.trim(), pageable)
                : maladieRepository.findAll(pageable);

        return page.map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminMaladieResponse getMaladieById(Long id) {
        log.info("Consultation de la maladie ID {}", id);
        Maladie maladie = maladieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Maladie", "id", id));
        return toResponse(maladie);
    }

    @Override
    public AdminMaladieResponse createMaladie(AdminMaladieRequest request) {
        log.info("Création d'une nouvelle maladie : {}", request.getNom());

        if (maladieRepository.existsByNomIgnoreCase(request.getNom().trim())) {
            throw new ConflictException("Maladie", "nom", request.getNom().trim());
        }

        Maladie maladie = Maladie.builder()
                .nom(request.getNom().trim())
                .description(request.getDescription())
                .plantes(new HashSet<>())
                .build();

        maladie = maladieRepository.save(maladie);

        if (request.getPlanteIds() != null && !request.getPlanteIds().isEmpty()) {
            List<Plante> plantes = planteRepository.findAllById(request.getPlanteIds());
            for (Plante p : plantes) {
                p.getMaladies().add(maladie);
                planteRepository.save(p);
            }
            maladie.setPlantes(new HashSet<>(plantes));
        }

        return toResponse(maladie);
    }

    @Override
    public AdminMaladieResponse updateMaladie(Long id, AdminMaladieRequest request) {
        log.info("Mise à jour de la maladie ID {}", id);

        Maladie maladie = maladieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Maladie", "id", id));

        if (!maladie.getNom().equalsIgnoreCase(request.getNom().trim())
                && maladieRepository.existsByNomIgnoreCase(request.getNom().trim())) {
            throw new ConflictException("Maladie", "nom", request.getNom().trim());
        }

        maladie.setNom(request.getNom().trim());
        maladie.setDescription(request.getDescription());

        if (request.getPlanteIds() != null) {
            // Détacher les anciennes
            for (Plante p : new ArrayList<>(maladie.getPlantes())) {
                p.getMaladies().remove(maladie);
                planteRepository.save(p);
            }

            // Attacher les nouvelles
            List<Plante> nouvelles = planteRepository.findAllById(request.getPlanteIds());
            for (Plante p : nouvelles) {
                p.getMaladies().add(maladie);
                planteRepository.save(p);
            }
            maladie.setPlantes(new HashSet<>(nouvelles));
        }

        maladie = maladieRepository.save(maladie);
        return toResponse(maladie);
    }

    @Override
    public void deleteMaladie(Long id) {
        log.info("Suppression de la maladie ID {}", id);
        Maladie maladie = maladieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Maladie", "id", id));

        for (Plante p : new ArrayList<>(maladie.getPlantes())) {
            p.getMaladies().remove(maladie);
            planteRepository.save(p);
        }

        maladieRepository.delete(maladie);
    }

    private AdminMaladieResponse toResponse(Maladie maladie) {
        List<AdminMaladiePlanteDto> planteDtos = (maladie.getPlantes() != null)
                ? maladie.getPlantes().stream()
                        .map(p -> {
                            String nomVern = "";
                            if (p.getNomsPlante() != null && !p.getNomsPlante().isEmpty()) {
                                nomVern = p.getNomsPlante().iterator().next().getNom();
                            }
                            return AdminMaladiePlanteDto.builder()
                                    .id(p.getId())
                                    .nomScientifique(p.getNomScientifique())
                                    .nomVernaculaire(nomVern)
                                    .build();
                        })
                        .collect(Collectors.toList())
                : new ArrayList<>();

        return AdminMaladieResponse.builder()
                .id(maladie.getId())
                .nom(maladie.getNom())
                .description(maladie.getDescription())
                .nbPlantes(planteDtos.size())
                .plantes(planteDtos)
                .build();
    }
}
