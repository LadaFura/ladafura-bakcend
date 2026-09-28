package com.pharmacopee.ladafura.services;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.pharmacopee.ladafura.Models.AgentCollecte;
import com.pharmacopee.ladafura.Models.Collecte;
import com.pharmacopee.ladafura.Models.Localisation;
import com.pharmacopee.ladafura.dto.agent.localisation.AgentLocalisationRequest;
import com.pharmacopee.ladafura.dto.agent.localisation.AgentLocalisationResponse;
import com.pharmacopee.ladafura.enums.StatutCollecte;
import com.pharmacopee.ladafura.exceptions.BadRequestException;
import com.pharmacopee.ladafura.exceptions.ForbiddenException;
import com.pharmacopee.ladafura.exceptions.ResourceNotFoundException;
import com.pharmacopee.ladafura.repository.CollecteRepository;
import com.pharmacopee.ladafura.repository.LocalisationRepository;
import com.pharmacopee.ladafura.services.impl.AgentLocalisationServiceImpl;
import com.pharmacopee.ladafura.services.interfaces.IAgentAuthService;

@ExtendWith(MockitoExtension.class)
class AgentLocalisationServiceImplTest {

    @Mock
    private CollecteRepository collecteRepository;

    @Mock
    private LocalisationRepository localisationRepository;

    @Mock
    private IAgentAuthService agentAuthService;

    @InjectMocks
    private AgentLocalisationServiceImpl agentLocalisationService;

    private AgentCollecte agentConnecte;
    private Collecte collecteBrouillon;
    private Localisation localisation;

    @BeforeEach
    void setUp() {
        agentConnecte = new AgentCollecte();
        agentConnecte.setId(10L);
        agentConnecte.setNom("Coulibaly");
        agentConnecte.setPrenom("Oumar");

        localisation = Localisation.builder()
                .id(1L)
                .region("Sikasso")
                .cercle("Koutiala")
                .commune("Finkolo")
                .localite("Finkolo Centre")
                .latitude(12.3812)
                .longitude(-5.4590)
                .build();

        collecteBrouillon = Collecte.builder()
                .id(100L)
                .statut(StatutCollecte.BROUILLON)
                .agentCollecte(agentConnecte)
                .localisation(null)
                .build();
    }

    @Test
    @DisplayName("Enregistrer une nouvelle localisation pour une collecte (1 ── 1)")
    void saveOrUpdateLocalisation_Succes_NouvelleLocalisation() {
        AgentLocalisationRequest request = AgentLocalisationRequest.builder()
                .region("Sikasso")
                .cercle("Koutiala")
                .commune("Finkolo")
                .localite("Finkolo Centre")
                .latitude(12.3812)
                .longitude(-5.4590)
                .build();

        when(agentAuthService.getCurrentAgent()).thenReturn(agentConnecte);
        when(collecteRepository.findById(100L)).thenReturn(Optional.of(collecteBrouillon));
        when(localisationRepository.save(any(Localisation.class))).thenReturn(localisation);
        when(collecteRepository.save(any(Collecte.class))).thenReturn(collecteBrouillon);

        AgentLocalisationResponse response = agentLocalisationService.saveOrUpdateLocalisation(100L, request);

        assertThat(response).isNotNull();
        assertThat(response.getRegion()).isEqualTo("Sikasso");
        assertThat(response.getCercle()).isEqualTo("Koutiala");
        assertThat(response.getCommune()).isEqualTo("Finkolo");
        assertThat(response.getLocalite()).isEqualTo("Finkolo Centre");
        assertThat(response.getLatitude()).isEqualTo(12.3812);
        assertThat(response.getLongitude()).isEqualTo(-5.4590);
        assertThat(response.isCoordonneesGpsPresentes()).isTrue();
        assertThat(response.getAdresseFormatee()).isEqualTo("Finkolo Centre, Finkolo, Koutiala, Sikasso");

        verify(localisationRepository).save(any(Localisation.class));
        verify(collecteRepository).save(collecteBrouillon);
    }

    @Test
    @DisplayName("Mettre à jour une localisation existante sur une collecte")
    void saveOrUpdateLocalisation_Succes_MiseAJourExistante() {
        collecteBrouillon.setLocalisation(localisation);

        AgentLocalisationRequest request = AgentLocalisationRequest.builder()
                .region("Sikasso")
                .cercle("Koutiala")
                .commune("Finkolo")
                .localite("Finkolo Hameau Nord")
                .latitude(12.3900)
                .longitude(-5.4600)
                .build();

        when(agentAuthService.getCurrentAgent()).thenReturn(agentConnecte);
        when(collecteRepository.findById(100L)).thenReturn(Optional.of(collecteBrouillon));
        when(localisationRepository.save(localisation)).thenReturn(localisation);
        when(collecteRepository.save(any(Collecte.class))).thenReturn(collecteBrouillon);

        AgentLocalisationResponse response = agentLocalisationService.saveOrUpdateLocalisation(100L, request);

        assertThat(response).isNotNull();
        assertThat(localisation.getLocalite()).isEqualTo("Finkolo Hameau Nord");
        assertThat(localisation.getLatitude()).isEqualTo(12.3900);
        verify(localisationRepository).save(localisation);
    }

    @Test
    @DisplayName("Localisation : rejet si la collecte appartient à un autre agent (403)")
    void saveOrUpdateLocalisation_NonProprietaire_ForbiddenException() {
        AgentCollecte autreAgent = new AgentCollecte();
        autreAgent.setId(99L);
        collecteBrouillon.setAgentCollecte(autreAgent);

        AgentLocalisationRequest request = AgentLocalisationRequest.builder()
                .region("Sikasso")
                .cercle("Koutiala")
                .commune("Finkolo")
                .localite("Finkolo")
                .build();

        when(agentAuthService.getCurrentAgent()).thenReturn(agentConnecte);
        when(collecteRepository.findById(100L)).thenReturn(Optional.of(collecteBrouillon));

        assertThatThrownBy(() -> agentLocalisationService.saveOrUpdateLocalisation(100L, request))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("cette collecte ne vous appartient pas");

        verify(localisationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Localisation : rejet si la collecte n'est plus modifiable (400)")
    void saveOrUpdateLocalisation_CollecteVerrouillee_BadRequestException() {
        collecteBrouillon.setStatut(StatutCollecte.SOUMISE);

        AgentLocalisationRequest request = AgentLocalisationRequest.builder()
                .region("Sikasso")
                .cercle("Koutiala")
                .commune("Finkolo")
                .localite("Finkolo")
                .build();

        when(agentAuthService.getCurrentAgent()).thenReturn(agentConnecte);
        when(collecteRepository.findById(100L)).thenReturn(Optional.of(collecteBrouillon));

        assertThatThrownBy(() -> agentLocalisationService.saveOrUpdateLocalisation(100L, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Impossible de modifier la localisation d'une collecte avec le statut SOUMISE");

        verify(localisationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Consulter la localisation d'une collecte")
    void getLocalisationByCollecte_Succes() {
        collecteBrouillon.setLocalisation(localisation);

        when(agentAuthService.getCurrentAgent()).thenReturn(agentConnecte);
        when(collecteRepository.findById(100L)).thenReturn(Optional.of(collecteBrouillon));

        AgentLocalisationResponse response = agentLocalisationService.getLocalisationByCollecte(100L);

        assertThat(response).isNotNull();
        assertThat(response.getRegion()).isEqualTo("Sikasso");
        assertThat(response.getLocalite()).isEqualTo("Finkolo Centre");
    }

    @Test
    @DisplayName("Consulter la localisation : 404 si non définie")
    void getLocalisationByCollecte_NonExistant_ResourceNotFoundException() {
        collecteBrouillon.setLocalisation(null);

        when(agentAuthService.getCurrentAgent()).thenReturn(agentConnecte);
        when(collecteRepository.findById(100L)).thenReturn(Optional.of(collecteBrouillon));

        assertThatThrownBy(() -> agentLocalisationService.getLocalisationByCollecte(100L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Localisation");
    }

    @Test
    @DisplayName("Dissocier la localisation d'une collecte")
    void supprimerLocalisation_Succes() {
        collecteBrouillon.setLocalisation(localisation);

        when(agentAuthService.getCurrentAgent()).thenReturn(agentConnecte);
        when(collecteRepository.findById(100L)).thenReturn(Optional.of(collecteBrouillon));

        agentLocalisationService.supprimerLocalisation(100L);

        assertThat(collecteBrouillon.getLocalisation()).isNull();
        verify(collecteRepository).save(collecteBrouillon);
    }

    @Test
    @DisplayName("Rechercher des localisations de terrain")
    void rechercherLocalisations_Succes() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Localisation> page = new PageImpl<>(List.of(localisation), pageable, 1);

        when(localisationRepository.searchLocalisations("Finkolo", pageable)).thenReturn(page);

        Page<AgentLocalisationResponse> result = agentLocalisationService.rechercherLocalisations("Finkolo", pageable);

        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getLocalite()).isEqualTo("Finkolo Centre");
    }
}
