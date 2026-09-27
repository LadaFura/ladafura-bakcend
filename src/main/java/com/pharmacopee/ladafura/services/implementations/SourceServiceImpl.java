package com.pharmacopee.ladafura.services.implementations;

import com.pharmacopee.ladafura.Models.Source;
import com.pharmacopee.ladafura.repository.SourceRepository;
import com.pharmacopee.ladafura.services.interfaces.SourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SourceServiceImpl implements SourceService {

    private final SourceRepository sourceRepository;

    @Override
    public List<Source> getAllSources() {
        return sourceRepository.findAll();
    }

    @Override
    public Source getSourceById(Long id) {
        return sourceRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Source introuvable"));
    }

    @Override
    public Source createSource(Source source) {
        return sourceRepository.save(source);
    }

    @Override
    public Source updateSource(Long id, Source source) {

        Source sourceExistante =
                sourceRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Source introuvable"));

        sourceExistante.setNom(source.getNom());
        sourceExistante.setDescription(source.getDescription());

        return sourceRepository.save(sourceExistante);
    }

    @Override
    public void deleteSource(Long id) {

        if (!sourceRepository.existsById(id)) {
            throw new RuntimeException("Source introuvable");
        }

        sourceRepository.deleteById(id);
    }
}
