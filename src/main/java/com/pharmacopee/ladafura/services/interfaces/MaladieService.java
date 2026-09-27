package com.pharmacopee.ladafura.services.interfaces;

import com.pharmacopee.ladafura.Models.Maladie;

import java.util.List;

public interface MaladieService {

    List<Maladie> getAllMaladies();

    Maladie getMaladieById(Long id);

    Maladie createMaladie(Maladie maladie);

    Maladie updateMaladie(Long id, Maladie maladie);

    void deleteMaladie(Long id);
}