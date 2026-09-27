package com.pharmacopee.ladafura.services.interfaces;

import com.pharmacopee.ladafura.Models.Source;

import java.util.List;

public interface SourceService {

    List<Source> getAllSources();

    Source getSourceById(Long id);

    Source createSource(Source source);

    Source updateSource(Long id, Source source);

    void deleteSource(Long id);
}
