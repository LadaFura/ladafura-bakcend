package com.pharmacopee.ladafura.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.pharmacopee.ladafura.controllers.population.PopulationProfileController;
import com.pharmacopee.ladafura.dto.population.profil.PopulationProfileResponse;
import com.pharmacopee.ladafura.dto.population.profil.PopulationUpdateProfileRequest;
import com.pharmacopee.ladafura.enums.Role;
import com.pharmacopee.ladafura.enums.StatutUtilisateur;
import com.pharmacopee.ladafura.services.interfaces.IPopulationProfileService;

@ExtendWith(MockitoExtension.class)
class PopulationProfileControllerTest {

    @Mock
    private IPopulationProfileService populationProfileService;

    @InjectMocks
    private PopulationProfileController populationProfileController;

    @Test
    @DisplayName("GET /api/v1/population/profile - 200 OK")
    void getProfile_200OK() {
        PopulationProfileResponse mockResponse = PopulationProfileResponse.builder()
                .id(15L)
                .nom("Diarra")
                .prenom("Fatoumata")
                .email("fatoumata.diarra@gmail.com")
                .telephone("+223 70 12 34 56")
                .role(Role.POPULATION)
                .statut(StatutUtilisateur.ACTIF)
                .nombreTotalCommandes(2L)
                .nombreTotalFavoris(4L)
                .build();

        when(populationProfileService.getProfile()).thenReturn(mockResponse);

        ResponseEntity<PopulationProfileResponse> response = populationProfileController.getProfile();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(15L);
        assertThat(response.getBody().getNom()).isEqualTo("Diarra");
        assertThat(response.getBody().getRole()).isEqualTo(Role.POPULATION);

        verify(populationProfileService).getProfile();
    }

    @Test
    @DisplayName("PUT /api/v1/population/profile - 200 OK")
    void updateProfile_200OK() {
        PopulationUpdateProfileRequest request = PopulationUpdateProfileRequest.builder()
                .nom("Traoré")
                .prenom("Awa")
                .telephone("+223 75 99 88 77")
                .build();

        PopulationProfileResponse mockResponse = PopulationProfileResponse.builder()
                .id(15L)
                .nom("Traoré")
                .prenom("Awa")
                .email("fatoumata.diarra@gmail.com")
                .telephone("+223 75 99 88 77")
                .role(Role.POPULATION)
                .statut(StatutUtilisateur.ACTIF)
                .nombreTotalCommandes(2L)
                .nombreTotalFavoris(4L)
                .build();

        when(populationProfileService.updateProfile(request)).thenReturn(mockResponse);

        ResponseEntity<PopulationProfileResponse> response = populationProfileController.updateProfile(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getNom()).isEqualTo("Traoré");
        assertThat(response.getBody().getPrenom()).isEqualTo("Awa");

        verify(populationProfileService).updateProfile(request);
    }
}
