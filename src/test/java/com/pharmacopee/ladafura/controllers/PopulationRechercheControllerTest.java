package com.pharmacopee.ladafura.controllers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.pharmacopee.ladafura.controllers.population.PopulationRechercheController;
import com.pharmacopee.ladafura.dto.population.recherche.PopulationGlobalSearchResponse;
import com.pharmacopee.ladafura.dto.population.recherche.PopulationMaladieSearchItem;
import com.pharmacopee.ladafura.dto.population.recherche.PopulationPharmacopeeSearchItem;
import com.pharmacopee.ladafura.dto.population.recherche.PopulationPlanteSearchItem;
import com.pharmacopee.ladafura.dto.population.recherche.PopulationProduitSearchItem;
import com.pharmacopee.ladafura.dto.population.recherche.PopulationVernaculaireSearchItem;
import com.pharmacopee.ladafura.services.interfaces.IPopulationRechercheService;

@ExtendWith(MockitoExtension.class)
class PopulationRechercheControllerTest {

    @Mock
    private IPopulationRechercheService rechercheService;

    @InjectMocks
    private PopulationRechercheController rechercheController;

    @Test
    @DisplayName("GET /api/v1/population/recherche - 200 OK")
    void rechercheGlobale_200OK() {
        PopulationGlobalSearchResponse mockResponse = PopulationGlobalSearchResponse.builder()
                .query("kinkeliba")
                .totalResultats(5)
                .plantes(List.of())
                .nomsVernaculaires(List.of())
                .maladies(List.of())
                .produits(List.of())
                .pharmacopees(List.of())
                .build();

        when(rechercheService.rechercheGlobale("kinkeliba")).thenReturn(mockResponse);

        ResponseEntity<PopulationGlobalSearchResponse> response = rechercheController.rechercheGlobale("kinkeliba");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getQuery()).isEqualTo("kinkeliba");
        assertThat(response.getBody().getTotalResultats()).isEqualTo(5);

        verify(rechercheService).rechercheGlobale("kinkeliba");
    }

    @Test
    @DisplayName("GET /api/v1/population/recherche/plantes - 200 OK")
    void rechercherPlantes_200OK() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<PopulationPlanteSearchItem> page = new PageImpl<>(List.of(
                PopulationPlanteSearchItem.builder().id(1L).nomScientifique("Combretum micranthum").build()
        ));

        when(rechercheService.rechercherPlantes("combretum", pageable)).thenReturn(page);

        ResponseEntity<Page<PopulationPlanteSearchItem>> response = rechercheController.rechercherPlantes("combretum", pageable);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).hasSize(1);
        assertThat(response.getBody().getContent().get(0).getNomScientifique()).isEqualTo("Combretum micranthum");

        verify(rechercheService).rechercherPlantes("combretum", pageable);
    }

    @Test
    @DisplayName("GET /api/v1/population/recherche/vernaculaires - 200 OK")
    void rechercherNomsVernaculaires_200OK() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<PopulationVernaculaireSearchItem> page = new PageImpl<>(List.of(
                PopulationVernaculaireSearchItem.builder().id(10L).nom("Kinkéliba").langue("Bambara").build()
        ));

        when(rechercheService.rechercherNomsVernaculaires("Kinkéliba", "Bambara", pageable)).thenReturn(page);

        ResponseEntity<Page<PopulationVernaculaireSearchItem>> response =
                rechercheController.rechercherNomsVernaculaires("Kinkéliba", "Bambara", pageable);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).hasSize(1);
        assertThat(response.getBody().getContent().get(0).getNom()).isEqualTo("Kinkéliba");

        verify(rechercheService).rechercherNomsVernaculaires("Kinkéliba", "Bambara", pageable);
    }

    @Test
    @DisplayName("GET /api/v1/population/recherche/maladies - 200 OK")
    void rechercherMaladies_200OK() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<PopulationMaladieSearchItem> page = new PageImpl<>(List.of(
                PopulationMaladieSearchItem.builder().id(3L).nom("Paludisme").build()
        ));

        when(rechercheService.rechercherMaladies("paludisme", pageable)).thenReturn(page);

        ResponseEntity<Page<PopulationMaladieSearchItem>> response = rechercheController.rechercherMaladies("paludisme", pageable);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).hasSize(1);
        assertThat(response.getBody().getContent().get(0).getNom()).isEqualTo("Paludisme");

        verify(rechercheService).rechercherMaladies("paludisme", pageable);
    }

    @Test
    @DisplayName("GET /api/v1/population/recherche/produits - 200 OK")
    void rechercherProduits_200OK() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<PopulationProduitSearchItem> page = new PageImpl<>(List.of(
                PopulationProduitSearchItem.builder().id(7L).nom("Tisane Kinkéliba").prix(2500.0).build()
        ));

        when(rechercheService.rechercherProduits("tisane", 1L, 5000.0, pageable)).thenReturn(page);

        ResponseEntity<Page<PopulationProduitSearchItem>> response =
                rechercheController.rechercherProduits("tisane", 1L, 5000.0, pageable);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).hasSize(1);
        assertThat(response.getBody().getContent().get(0).getNom()).isEqualTo("Tisane Kinkéliba");

        verify(rechercheService).rechercherProduits("tisane", 1L, 5000.0, pageable);
    }

    @Test
    @DisplayName("GET /api/v1/population/recherche/pharmacopees - 200 OK")
    void rechercherPharmacopees_200OK() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<PopulationPharmacopeeSearchItem> page = new PageImpl<>(List.of(
                PopulationPharmacopeeSearchItem.builder().id(2L).nom("Pharmacie Mandé").region("Koulikoro").build()
        ));

        when(rechercheService.rechercherPharmacopees("mandé", "Koulikoro", "Kati", "Siby", pageable)).thenReturn(page);

        ResponseEntity<Page<PopulationPharmacopeeSearchItem>> response =
                rechercheController.rechercherPharmacopees("mandé", "Koulikoro", "Kati", "Siby", pageable);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).hasSize(1);
        assertThat(response.getBody().getContent().get(0).getNom()).isEqualTo("Pharmacie Mandé");

        verify(rechercheService).rechercherPharmacopees("mandé", "Koulikoro", "Kati", "Siby", pageable);
    }
}
