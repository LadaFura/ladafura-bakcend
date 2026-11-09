package com.pharmacopee.ladafura.services.interfaces;

import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.pharmacopee.ladafura.dto.pharmacopee.depense.DepenseResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.depense.EnregistrerDepenseRequest;

/** Dépenses d'exploitation de la pharmacopée courante, toujours payées depuis une caisse ou un compte. */
public interface IPharmacopeeDepenseService {

    Page<DepenseResponse> getDepenses(LocalDate debut, LocalDate fin, Pageable pageable);

    DepenseResponse getDepense(Long id);

    DepenseResponse creer(EnregistrerDepenseRequest request);

    DepenseResponse annuler(Long id, String motif);
}
