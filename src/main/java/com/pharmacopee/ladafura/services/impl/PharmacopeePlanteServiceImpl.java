package com.pharmacopee.ladafura.services.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pharmacopee.ladafura.Models.NomPlante;
import com.pharmacopee.ladafura.Models.Plante;
import com.pharmacopee.ladafura.dto.pharmacopee.plante.CreatePharmacopeePlanteRequest;
import com.pharmacopee.ladafura.dto.pharmacopee.plante.PharmacopeePlanteDto;
import com.pharmacopee.ladafura.enums.StatutPlante;
import com.pharmacopee.ladafura.repository.PlanteRepository;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeAuthService;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeePlanteService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PharmacopeePlanteServiceImpl implements IPharmacopeePlanteService {

    private final PlanteRepository planteRepository;
    private final IPharmacopeeAuthService pharmacopeeAuthService;

    public PharmacopeePlanteServiceImpl(
            PlanteRepository planteRepository,
            IPharmacopeeAuthService pharmacopeeAuthService) {
        this.planteRepository = planteRepository;
        this.pharmacopeeAuthService = pharmacopeeAuthService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PharmacopeePlanteDto> getAllPlantes() {
        return planteRepository.findByStatut(StatutPlante.VALIDE).stream()
                .map(p -> PharmacopeePlanteDto.builder()
                        .id(p.getId())
                        .nomScientifique(p.getNomScientifique())
                        .nomVulgaire(p.getNomsPlante() != null ? p.getNomsPlante().stream().map(NomPlante::getNom).collect(Collectors.joining(", ")) : null)
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PharmacopeePlanteDto> searchPlantes(String query) {
        if (query == null || query.trim().length() < 2) {
            return List.of();
        }
        
        List<Plante> plantes = planteRepository.searchByNomScientifiqueOrNomVernaculaire(query.trim());
        
        return plantes.stream()
                .limit(20) // limiter les suggestions
                .map(p -> PharmacopeePlanteDto.builder()
                        .id(p.getId())
                        .nomScientifique(p.getNomScientifique())
                        .nomVulgaire(p.getNomsPlante().stream().map(NomPlante::getNom).collect(Collectors.joining(", ")))
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PharmacopeePlanteDto createPlante(CreatePharmacopeePlanteRequest request) {
        pharmacopeeAuthService.verifyPraticienPrincipal();
        
        String nom = request.getNom().trim();
        
        if (planteRepository.existsByNomScientifiqueIgnoreCase(nom)) {
            throw new IllegalArgumentException("Une plante avec ce nom existe déjà.");
        }
        
        Plante plante = new Plante();
        plante.setNomScientifique(nom);
        // On considère que le praticien principal crée des plantes qui sont automatiquement validées
        plante.setStatut(StatutPlante.VALIDE);
        
        // On ajoute aussi ce nom comme nom vernaculaire par défaut (sans langue spécifique)
        NomPlante nomPlante = new NomPlante();
        nomPlante.setNom(nom);
        nomPlante.setLangue("Non précisée");
        nomPlante.setPlante(plante);
        
        plante.getNomsPlante().add(nomPlante);
        
        Plante saved = planteRepository.save(plante);
        log.info("Nouvelle plante validée créée par le praticien principal: {}", saved.getNomScientifique());
        
        return PharmacopeePlanteDto.builder()
                .id(saved.getId())
                .nomScientifique(saved.getNomScientifique())
                .nomVulgaire(nomPlante.getNom())
                .build();
    }
}
