package com.pharmacopee.ladafura.services.interfaces;

import java.util.List;

import com.pharmacopee.ladafura.dto.pharmacopee.maladie.CreatePharmacopeeMaladieRequest;
import com.pharmacopee.ladafura.dto.pharmacopee.maladie.PharmacopeeMaladieDto;

public interface IPharmacopeeMaladieService {

    List<PharmacopeeMaladieDto> getAllMaladies();

    List<PharmacopeeMaladieDto> searchMaladies(String query);

    PharmacopeeMaladieDto createMaladie(CreatePharmacopeeMaladieRequest request);
}

