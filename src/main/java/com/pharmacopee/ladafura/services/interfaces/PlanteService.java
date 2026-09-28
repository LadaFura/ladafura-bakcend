package com.pharmacopee.ladafura.services.interfaces;

import java.util.List;

import org.jspecify.annotations.Nullable;

import com.pharmacopee.ladafura.Models.Plante;
import com.pharmacopee.ladafura.dto.requestes.PlanteDtoRequeste;
import com.pharmacopee.ladafura.dto.responses.PlanteDtoResponse;

public interface PlanteService {

    List<PlanteDtoResponse> getAllPlantes();

    Plante getPlanteById(Long id);

    Plante updatePlante(Long id, Plante plante);

    void deletePlante(Long id);

    @Nullable
    PlanteDtoResponse createPlante(PlanteDtoRequeste planteDto);

}
