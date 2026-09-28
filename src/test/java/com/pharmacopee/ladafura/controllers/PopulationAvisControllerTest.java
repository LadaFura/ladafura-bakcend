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

import com.pharmacopee.ladafura.controllers.population.PopulationAvisController;
import com.pharmacopee.ladafura.dto.population.avis.PopulationAvisResponse;
import com.pharmacopee.ladafura.dto.population.avis.PopulationCreateAvisRequest;
import com.pharmacopee.ladafura.dto.population.avis.PopulationEligibiliteAvisResponse;
import com.pharmacopee.ladafura.dto.population.avis.PopulationUpdateAvisRequest;
import com.pharmacopee.ladafura.enums.StatutAvis;
import com.pharmacopee.ladafura.services.interfaces.IPopulationAvisService;

@ExtendWith(MockitoExtension.class)
class PopulationAvisControllerTest {

    @Mock
    private IPopulationAvisService avisService;

    @InjectMocks
    private PopulationAvisController avisController;

    @Test
    @DisplayName("GET /api/v1/population/avis/eligibilite/{produitId} - 200 OK")
    void verifierEligibiliteAvis_200OK() {
        PopulationEligibiliteAvisResponse responseDto = PopulationEligibiliteAvisResponse.builder()
                .produitId(5L)
                .nomProduit("Sirop d'Artemisia")
                .eligible(true)
                .dejaEvalue(false)
                .message("Éligible")
                .build();

        when(avisService.verifierEligibiliteAvis(5L)).thenReturn(responseDto);

        ResponseEntity<PopulationEligibiliteAvisResponse> response = avisController.verifierEligibiliteAvis(5L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getEligible()).isTrue();
        verify(avisService).verifierEligibiliteAvis(5L);
    }

    @Test
    @DisplayName("POST /api/v1/population/avis - 201 Created")
    void creerAvis_201Created() {
        PopulationCreateAvisRequest request = PopulationCreateAvisRequest.builder()
                .produitId(5L)
                .note(5)
                .commentaire("Excellent")
                .build();

        PopulationAvisResponse responseDto = PopulationAvisResponse.builder()
                .id(1L)
                .produitId(5L)
                .note(5)
                .statut(StatutAvis.EN_ATTENTE)
                .build();

        when(avisService.creerAvis(request)).thenReturn(responseDto);

        ResponseEntity<PopulationAvisResponse> response = avisController.creerAvis(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(1L);
        verify(avisService).creerAvis(request);
    }

    @Test
    @DisplayName("PUT /api/v1/population/avis/{avisId} - 200 OK")
    void modifierAvis_200OK() {
        PopulationUpdateAvisRequest request = PopulationUpdateAvisRequest.builder()
                .note(4)
                .commentaire("Mis à jour")
                .build();

        PopulationAvisResponse responseDto = PopulationAvisResponse.builder()
                .id(1L)
                .note(4)
                .statut(StatutAvis.EN_ATTENTE)
                .build();

        when(avisService.modifierAvis(1L, request)).thenReturn(responseDto);

        ResponseEntity<PopulationAvisResponse> response = avisController.modifierAvis(1L, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getNote()).isEqualTo(4);
        verify(avisService).modifierAvis(1L, request);
    }

    @Test
    @DisplayName("DELETE /api/v1/population/avis/{avisId} - 204 No Content")
    void supprimerAvis_204NoContent() {
        ResponseEntity<Void> response = avisController.supprimerAvis(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(avisService).supprimerAvis(1L);
    }

    @Test
    @DisplayName("GET /api/v1/population/avis - 200 OK")
    void getMesAvis_200OK() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<PopulationAvisResponse> page = new PageImpl<>(List.of(
                PopulationAvisResponse.builder()
                        .id(1L)
                        .note(5)
                        .build()
        ));

        when(avisService.getMesAvis(null, pageable)).thenReturn(page);

        ResponseEntity<Page<PopulationAvisResponse>> response = avisController.getMesAvis(null, pageable);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getTotalElements()).isEqualTo(1);
        verify(avisService).getMesAvis(null, pageable);
    }

    @Test
    @DisplayName("GET /api/v1/population/avis/{avisId} - 200 OK")
    void getAvisDetail_200OK() {
        PopulationAvisResponse responseDto = PopulationAvisResponse.builder()
                .id(1L)
                .note(5)
                .nomProduit("Artemisia")
                .build();

        when(avisService.getAvisDetail(1L)).thenReturn(responseDto);

        ResponseEntity<PopulationAvisResponse> response = avisController.getAvisDetail(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(1L);
        verify(avisService).getAvisDetail(1L);
    }
}
