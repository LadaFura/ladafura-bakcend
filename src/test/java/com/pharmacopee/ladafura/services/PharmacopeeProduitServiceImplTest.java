package com.pharmacopee.ladafura.services;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.pharmacopee.ladafura.Models.Pharmacopee;
import com.pharmacopee.ladafura.repository.DisponibiliteProduitRepository;
import com.pharmacopee.ladafura.services.impl.PharmacopeeProduitServiceImpl;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeContexteService;

/** Recherche du comptoir et de « Mes produits » : les filtres reçus doivent atteindre la requête. */
@ExtendWith(MockitoExtension.class)
class PharmacopeeProduitServiceImplTest {

    private static final Long PHARMACOPEE_ID = 7L;
    private final Pageable pageable = PageRequest.of(0, 24);

    @Mock
    private IPharmacopeeContexteService contexteService;

    @Mock
    private DisponibiliteProduitRepository disponibiliteProduitRepository;

    @InjectMocks
    private PharmacopeeProduitServiceImpl service;

    @BeforeEach
    void setUp() {
        Pharmacopee pharmacopee = new Pharmacopee();
        pharmacopee.setId(PHARMACOPEE_ID);
        when(contexteService.verifierPermission(IPharmacopeeContexteService.PRODUITS_CONSULTER))
                .thenReturn(new IPharmacopeeContexteService.ContexteMembre(null, pharmacopee, Set.of()));
        when(disponibiliteProduitRepository.rechercherParPharmacopee(any(), any(), any(), any(), any()))
                .thenReturn(Page.empty(pageable));
    }

    @Test
    @DisplayName("GET /api/v1/pharmacopee/produits - le mot-clé et la catégorie filtrent la requête")
    void getMesProduits_transmetMotCleEtCategorie() {
        service.getMesProduits("  Kinkéliba ", 3L, null, pageable);

        verify(disponibiliteProduitRepository)
                .rechercherParPharmacopee(eq(PHARMACOPEE_ID), eq("Kinkéliba"), eq(3L), isNull(), eq(pageable));
    }

    @Test
    @DisplayName("GET /api/v1/pharmacopee/produits - un mot-clé vide ne filtre pas")
    void getMesProduits_motCleVide_neFiltrePas() {
        service.getMesProduits("   ", null, true, pageable);

        verify(disponibiliteProduitRepository)
                .rechercherParPharmacopee(eq(PHARMACOPEE_ID), isNull(), isNull(), eq(true), eq(pageable));
    }
}
