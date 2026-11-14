package com.pharmacopee.ladafura.services.interfaces;

import java.time.LocalDate;

import com.pharmacopee.ladafura.dto.pharmacopee.tresorerie.SyntheseTresorerieResponse;

/** Synthèse de trésorerie : chiffres séparés, jamais additionnés entre eux. */
public interface IPharmacopeeTresorerieService {

    /** @throws com.pharmacopee.ladafura.exceptions.BadRequestException si la période est invalide */
    SyntheseTresorerieResponse getSynthese(LocalDate debut, LocalDate fin);
}
