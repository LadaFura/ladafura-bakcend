package com.pharmacopee.ladafura.services.implementations;

import com.pharmacopee.ladafura.Models.Maladie;
import com.pharmacopee.ladafura.repository.MaladieRepository;
import com.pharmacopee.ladafura.services.interfaces.MaladieService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MaladieServiceImpl implements MaladieService {

    private final MaladieRepository maladieRepository;

    @Override
    public List<Maladie> getAllMaladies() {
        return maladieRepository.findAll();
    }

    @Override
    public Maladie getMaladieById(Long id) {
        return maladieRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Maladie introuvable"));
    }

    @Override
    public Maladie createMaladie(Maladie maladie) {
        return maladieRepository.save(maladie);
    }

    @Override
    public Maladie updateMaladie(Long id, Maladie maladie) {

        Maladie maladieExistante =
                maladieRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Maladie introuvable"));

        maladieExistante.setNom(maladie.getNom());
        maladieExistante.setDescription(maladie.getDescription());

        return maladieRepository.save(maladieExistante);
    }

    @Override
    public void deleteMaladie(Long id) {

        if (!maladieRepository.existsById(id)) {
            throw new RuntimeException("Maladie introuvable");
        }

        maladieRepository.deleteById(id);
    }
}