package com.pharmacopee.ladafura.services.interfaces;

import java.util.List;

import org.jspecify.annotations.Nullable;

import com.pharmacopee.ladafura.Models.Plante;

public interface PlanteService {

    List<Plante> getAllPlantes();

    Plante getPlanteById(Long id);

    Plante updatePlante(Long id, Plante plante);

    void deletePlante(Long id);

    @Nullable
    Object createPlante(Plante plante);

}
