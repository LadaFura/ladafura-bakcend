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

import com.pharmacopee.ladafura.controllers.population.PopulationProduitController;
import com.pharmacopee.ladafura.dto.population.produit.PopulationCompositionItemDto;
import com.pharmacopee.ladafura.dto.population.produit.PopulationModeRetraitDto;
import com.pharmacopee.ladafura.dto.population.produit.PopulationOffrePharmacopeeDto;
import com.pharmacopee.ladafura.dto.population.produit.PopulationProduitDetailResponse;
import com.pharmacopee.ladafura.dto.population.produit.PopulationProduitMaladieDto;
import com.pharmacopee.ladafura.dto.population.produit.PopulationProduitSummaryResponse;
import com.pharmacopee.ladafura.services.interfaces.IPopulationProduitService;

@ExtendWith(MockitoExtension.class)
class PopulationProduitControllerTest {

    @Mock
    private IPopulationProduitService produitService;

    @InjectMocks
    private PopulationProduitController produitController;

    @Test
    @DisplayName("GET /api/v1/population/produits - 200 OK")
    void listerProduits_200OK() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<PopulationProduitSummaryResponse> page = new PageImpl<>(List.of(
                PopulationProduitSummaryResponse.builder()
                        .id(5L)
                        .nom("Tisane Kinkéliba Bio")
                        .forme("Sachet 100g")
                        .prixIndicatif(2500.0)
                        .disponibleEnPharmacie(true)
                        .nombrePharmacopees(2)
                        .noteMoyenne(4.8)
                        .nombreAvis(15L)
                        .build()
        ));

        when(produitService.listerProduits("tisane", 1L, 5000.0, pageable))
                .thenReturn(page);

        ResponseEntity<Page<PopulationProduitSummaryResponse>> response =
                produitController.listerProduits("tisane", 1L, 5000.0, pageable);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).hasSize(1);
        assertThat(response.getBody().getContent().get(0).getNom()).isEqualTo("Tisane Kinkéliba Bio");

        verify(produitService).listerProduits("tisane", 1L, 5000.0, pageable);
    }

    @Test
    @DisplayName("GET /api/v1/population/produits/{id} - 200 OK")
    void getProduitDetail_200OK() {
        PopulationProduitDetailResponse detail = PopulationProduitDetailResponse.builder()
                .id(5L)
                .nom("Tisane Kinkéliba Bio")
                .forme("Sachet 100g")
                .compositionTexte("100% feuilles séchées de kinkéliba")
                .prixIndicatif(2500.0)
                .compositions(List.of(
                        PopulationCompositionItemDto.builder()
                                .planteId(1L)
                                .nomScientifique("Combretum micranthum")
                                .quantite(100.0)
                                .unite("g")
                                .build()
                ))
                .maladies(List.of(
                        PopulationProduitMaladieDto.builder()
                                .id(10L)
                                .nom("Paludisme")
                                .build()
                ))
                .offresPharmacopees(List.of(
                        PopulationOffrePharmacopeeDto.builder()
                                .pharmacopeeId(3L)
                                .nomPharmacopee("Pharmacie Mandé")
                                .prix(2500.0)
                                .disponible(true)
                                .quantiteStock(10)
                                .modesRetrait(List.of(
                                        PopulationModeRetraitDto.builder().id(1L).type("PICKUP").actif(true).frais(0.0).build()
                                ))
                                .build()
                ))
                .build();

        when(produitService.getProduitDetail(5L)).thenReturn(detail);

        ResponseEntity<PopulationProduitDetailResponse> response = produitController.getProduitDetail(5L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(5L);
        assertThat(response.getBody().getNom()).isEqualTo("Tisane Kinkéliba Bio");
        assertThat(response.getBody().getCompositions()).hasSize(1);
        assertThat(response.getBody().getMaladies()).hasSize(1);
        assertThat(response.getBody().getOffresPharmacopees()).hasSize(1);

        verify(produitService).getProduitDetail(5L);
    }

    @Test
    @DisplayName("GET /api/v1/population/produits/{id}/offres - 200 OK")
    void getOffresByProduit_200OK() {
        List<PopulationOffrePharmacopeeDto> offres = List.of(
                PopulationOffrePharmacopeeDto.builder()
                        .pharmacopeeId(3L)
                        .nomPharmacopee("Pharmacie Mandé")
                        .prix(2500.0)
                        .disponible(true)
                        .build()
        );

        when(produitService.getOffresByProduit(5L)).thenReturn(offres);

        ResponseEntity<List<PopulationOffrePharmacopeeDto>> response = produitController.getOffresByProduit(5L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).getNomPharmacopee()).isEqualTo("Pharmacie Mandé");

        verify(produitService).getOffresByProduit(5L);
    }
}
