package com.pharmacopee.ladafura.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.pharmacopee.ladafura.controllers.population.PopulationPlanteController;
import com.pharmacopee.ladafura.dto.population.plante.PopulationConnaissanceTraditionnelleDto;
import com.pharmacopee.ladafura.dto.population.plante.PopulationEtudeScientifiqueDto;
import com.pharmacopee.ladafura.dto.population.plante.PopulationNomVernaculaireDto;
import com.pharmacopee.ladafura.dto.population.plante.PopulationPlanteDetailResponse;
import com.pharmacopee.ladafura.dto.population.plante.PopulationPlanteSummaryResponse;
import com.pharmacopee.ladafura.services.interfaces.IPopulationPlanteService;

@ExtendWith(MockitoExtension.class)
class PopulationPlanteControllerTest {

    @Mock
    private IPopulationPlanteService planteService;

    @InjectMocks
    private PopulationPlanteController planteController;

    @Test
    @DisplayName("GET /api/v1/population/plantes - 200 OK")
    void listerPlantes_200OK() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<PopulationPlanteSummaryResponse> page = new PageImpl<>(List.of(
                PopulationPlanteSummaryResponse.builder()
                        .id(1L)
                        .nomScientifique("Combretum micranthum")
                        .nomsVernaculaires(List.of("Kinkéliba"))
                        .nombreConnaissances(2)
                        .nombreEtudesScientifiques(1)
                        .build()
        ));

        when(planteService.listerPlantes(pageable)).thenReturn(page);

        ResponseEntity<Page<PopulationPlanteSummaryResponse>> response = planteController.listerPlantes(pageable);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).hasSize(1);
        assertThat(response.getBody().getContent().get(0).getNomScientifique()).isEqualTo("Combretum micranthum");

        verify(planteService).listerPlantes(pageable);
    }

    @Test
    @DisplayName("GET /api/v1/population/plantes/{id} - 200 OK")
    void getPlanteDetail_200OK() {
        PopulationPlanteDetailResponse detail = PopulationPlanteDetailResponse.builder()
                .id(1L)
                .nomScientifique("Combretum micranthum")
                .avertissementMedical("Avertissement de non substitution médicale")
                .nomsVernaculaires(List.of(
                        PopulationNomVernaculaireDto.builder().id(1L).nom("Kinkéliba").langue("Bambara").build()
                ))
                .connaissancesTraditionnelles(List.of(
                        PopulationConnaissanceTraditionnelleDto.builder().id(10L).usageRapporte("Traitement fébrifuge").build()
                ))
                .etudesScientifiques(List.of(
                        PopulationEtudeScientifiqueDto.builder().id(20L).titre("Étude clinique").annee(2021).build()
                ))
                .build();

        when(planteService.getPlanteDetail(1L)).thenReturn(detail);

        ResponseEntity<PopulationPlanteDetailResponse> response = planteController.getPlanteDetail(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(1L);
        assertThat(response.getBody().getNomScientifique()).isEqualTo("Combretum micranthum");
        assertThat(response.getBody().getAvertissementMedical()).isNotBlank();
        assertThat(response.getBody().getConnaissancesTraditionnelles()).hasSize(1);
        assertThat(response.getBody().getEtudesScientifiques()).hasSize(1);

        verify(planteService).getPlanteDetail(1L);
    }

    @Test
    @DisplayName("GET /api/v1/population/plantes/{id}/connaissances - 200 OK")
    void getConnaissancesByPlante_200OK() {
        List<PopulationConnaissanceTraditionnelleDto> list = List.of(
                PopulationConnaissanceTraditionnelleDto.builder().id(10L).usageRapporte("Infusion matinale").build()
        );

        when(planteService.getConnaissancesByPlante(1L)).thenReturn(list);

        ResponseEntity<List<PopulationConnaissanceTraditionnelleDto>> response =
                planteController.getConnaissancesByPlante(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).getUsageRapporte()).isEqualTo("Infusion matinale");

        verify(planteService).getConnaissancesByPlante(1L);
    }

    @Test
    @DisplayName("GET /api/v1/population/plantes/{id}/etudes - 200 OK")
    void getEtudesByPlante_200OK() {
        List<PopulationEtudeScientifiqueDto> list = List.of(
                PopulationEtudeScientifiqueDto.builder().id(20L).titre("Recherche phytochimique").build()
        );

        when(planteService.getEtudesByPlante(1L)).thenReturn(list);

        ResponseEntity<List<PopulationEtudeScientifiqueDto>> response =
                planteController.getEtudesByPlante(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).getTitre()).isEqualTo("Recherche phytochimique");

        verify(planteService).getEtudesByPlante(1L);
    }
}
