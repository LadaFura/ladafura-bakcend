package com.pharmacopee.ladafura.services.impl;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pharmacopee.ladafura.Models.Maladie;
import com.pharmacopee.ladafura.dto.pharmacopee.maladie.CreatePharmacopeeMaladieRequest;
import com.pharmacopee.ladafura.dto.pharmacopee.maladie.PharmacopeeMaladieDto;
import com.pharmacopee.ladafura.repository.MaladieRepository;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeAuthService;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeMaladieService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PharmacopeeMaladieServiceImpl implements IPharmacopeeMaladieService {

    private final MaladieRepository maladieRepository;
    private final IPharmacopeeAuthService pharmacopeeAuthService;

    @Override
    @Transactional(readOnly = true)
    public List<PharmacopeeMaladieDto> getAllMaladies() {
        return maladieRepository.findAll().stream()
                .sorted(Comparator.comparing(Maladie::getNom, String.CASE_INSENSITIVE_ORDER))
                .map(m -> PharmacopeeMaladieDto.builder()
                        .id(m.getId())
                        .nom(m.getNom())
                        .description(m.getDescription())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PharmacopeeMaladieDto> searchMaladies(String query) {
        if (query == null || query.trim().length() < 2) {
            return List.of();
        }
        return maladieRepository.findByNomContainingIgnoreCase(query.trim()).stream()
                .limit(20)
                .map(m -> PharmacopeeMaladieDto.builder()
                        .id(m.getId())
                        .nom(m.getNom())
                        .description(m.getDescription())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PharmacopeeMaladieDto createMaladie(CreatePharmacopeeMaladieRequest request) {
        pharmacopeeAuthService.verifyPraticienPrincipal();

        String nom = request.getNom().trim();

        Optional<Maladie> existing = maladieRepository.findByNomIgnoreCase(nom);
        if (existing.isPresent()) {
            Maladie m = existing.get();
            return PharmacopeeMaladieDto.builder()
                    .id(m.getId())
                    .nom(m.getNom())
                    .description(m.getDescription())
                    .build();
        }

        Maladie maladie = Maladie.builder()
                .nom(nom)
                .description(request.getDescription())
                .build();

        Maladie saved = maladieRepository.save(maladie);
        log.info("Nouvelle maladie créée par le praticien principal : {}", saved.getNom());

        return PharmacopeeMaladieDto.builder()
                .id(saved.getId())
                .nom(saved.getNom())
                .description(saved.getDescription())
                .build();
    }
}

