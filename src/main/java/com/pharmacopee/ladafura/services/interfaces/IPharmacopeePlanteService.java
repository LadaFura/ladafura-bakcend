package com.pharmacopee.ladafura.services.interfaces;

import java.util.List;

import com.pharmacopee.ladafura.dto.pharmacopee.plante.CreatePharmacopeePlanteRequest;
import com.pharmacopee.ladafura.dto.pharmacopee.plante.PharmacopeePlanteDto;

public interface IPharmacopeePlanteService {
    List<PharmacopeePlanteDto> getAllPlantes();
    List<PharmacopeePlanteDto> searchPlantes(String query);
    PharmacopeePlanteDto createPlante(CreatePharmacopeePlanteRequest request);
}
