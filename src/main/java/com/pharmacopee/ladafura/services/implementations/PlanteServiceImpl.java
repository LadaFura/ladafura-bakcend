package com.pharmacopee.ladafura.services.implementations;

import com.pharmacopee.ladafura.Models.Plante;
import com.pharmacopee.ladafura.repository.PlanteRepository;
import com.pharmacopee.ladafura.services.interfaces.PlanteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PlanteServiceImpl implements PlanteService {

    private final PlanteRepository planteRepository;

    @Override
    public List<Plante> getAllPlantes() {
        return planteRepository.findAll();
    }

    @Override
    public Plante getPlanteById(Long id) {
        return planteRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Plante introuvable"));
    }

    @Override
    public Plante createPlante(Plante plante) {
        return planteRepository.save(plante);
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