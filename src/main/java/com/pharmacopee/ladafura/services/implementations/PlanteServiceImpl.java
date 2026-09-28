package com.pharmacopee.ladafura.services.implementations;

import java.util.List;

import org.springframework.stereotype.Service;

import com.pharmacopee.ladafura.Models.Plante;
import com.pharmacopee.ladafura.dto.requestes.PlanteDtoRequeste;
import com.pharmacopee.ladafura.dto.responses.PlanteDtoResponse;
import com.pharmacopee.ladafura.mappers.PlanteMapper;
import com.pharmacopee.ladafura.repository.PlanteRepository;
import com.pharmacopee.ladafura.services.interfaces.PlanteService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PlanteServiceImpl implements PlanteService {

    private final PlanteRepository planteRepository;
    private final PlanteMapper planteMapper;

    @Override
    public List<PlanteDtoResponse> getAllPlantes() {
        return planteRepository.findAll().stream()
                .map(planteMapper::toDto)
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public Plante getPlanteById(Long id) {
        return planteRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Plante introuvable"));
    }

    @Override
    public PlanteDtoResponse createPlante(PlanteDtoRequeste planteDto ) {
        Plante plante = planteMapper.toEntity(planteDto);
        Plante p=planteRepository.save(plante);
        return planteMapper.toDto(p);
    } 
 
    @Override
    public Plante updatePlante(Long id, Plante plante) {

        Plante planteExistante =
                planteRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Plante introuvable"));

        planteExistante.setId(plante.getId());
        planteExistante.setDescription(plante.getDescription());

        return planteRepository.save(planteExistante);
    }

    @Override
    public void deletePlante(Long id) {

        if (!planteRepository.existsById(id)) {
            throw new RuntimeException("Plante introuvable");
        }

        planteRepository.deleteById(id);
    }
}