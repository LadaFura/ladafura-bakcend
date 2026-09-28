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

import com.pharmacopee.ladafura.controllers.population.PopulationAuthController;
import com.pharmacopee.ladafura.dto.population.auth.PopulationAuthResponse;
import com.pharmacopee.ladafura.dto.population.auth.PopulationRegisterRequest;
import com.pharmacopee.ladafura.dto.population.auth.PopulationSyncRequest;
import com.pharmacopee.ladafura.enums.Role;
import com.pharmacopee.ladafura.enums.StatutUtilisateur;
import com.pharmacopee.ladafura.services.interfaces.IPopulationAuthService;

@ExtendWith(MockitoExtension.class)
class PopulationAuthControllerTest {

    @Mock
    private IPopulationAuthService populationAuthService;

    @InjectMocks
    private PopulationAuthController populationAuthController;

    @Test
    @DisplayName("POST /api/v1/population/auth/register - 201 CREATED")
    void register_201Created() {
        PopulationRegisterRequest request = PopulationRegisterRequest.builder()
                .nom("Diarra")
                .prenom("Fatoumata")
                .email("fatoumata.diarra@gmail.com")
                .motDePasse("Mali2026!")
                .telephone("+223 70 12 34 56")
                .build();

        PopulationAuthResponse mockResponse = PopulationAuthResponse.builder()
                .id(15L)
                .nom("Diarra")
                .prenom("Fatoumata")
                .email("fatoumata.diarra@gmail.com")
                .role(Role.POPULATION)
                .statut(StatutUtilisateur.ACTIF)
                .build();

        when(populationAuthService.register(request)).thenReturn(mockResponse);

        ResponseEntity<PopulationAuthResponse> response = populationAuthController.register(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(15L);
        assertThat(response.getBody().getEmail()).isEqualTo("fatoumata.diarra@gmail.com");
        assertThat(response.getBody().getRole()).isEqualTo(Role.POPULATION);

        verify(populationAuthService).register(request);
    }

    @Test
    @DisplayName("POST /api/v1/population/auth/sync - 200 OK")
    void syncFirebaseUser_200OK() {
        PopulationSyncRequest request = PopulationSyncRequest.builder()
                .nom("Diarra")
                .prenom("Fatoumata")
                .telephone("+223 70 12 34 56")
                .build();

        PopulationAuthResponse mockResponse = PopulationAuthResponse.builder()
                .id(15L)
                .nom("Diarra")
                .prenom("Fatoumata")
                .role(Role.POPULATION)
                .build();

        when(populationAuthService.syncFirebaseUser(request)).thenReturn(mockResponse);

        ResponseEntity<PopulationAuthResponse> response = populationAuthController.syncFirebaseUser(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getNom()).isEqualTo("Diarra");

        verify(populationAuthService).syncFirebaseUser(request);
    }

    @Test
    @DisplayName("GET /api/v1/population/auth/me - 200 OK")
    void getMe_200OK() {
        PopulationAuthResponse mockResponse = PopulationAuthResponse.builder()
                .id(15L)
                .nom("Diarra")
                .prenom("Fatoumata")
                .email("fatoumata.diarra@gmail.com")
                .role(Role.POPULATION)
                .statut(StatutUtilisateur.ACTIF)
                .build();

        when(populationAuthService.getMe()).thenReturn(mockResponse);

        ResponseEntity<PopulationAuthResponse> response = populationAuthController.getMe();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getEmail()).isEqualTo("fatoumata.diarra@gmail.com");
        assertThat(response.getBody().getRole()).isEqualTo(Role.POPULATION);

        verify(populationAuthService).getMe();
    }
}
