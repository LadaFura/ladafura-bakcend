package com.pharmacopee.ladafura.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.pharmacopee.ladafura.Models.AgentCollecte;
import com.pharmacopee.ladafura.Models.Collecte;
import com.pharmacopee.ladafura.dto.agent.collecte.AgentCollecteDetailResponse;
import com.pharmacopee.ladafura.dto.agent.collecte.AgentCollecteSummaryResponse;
import com.pharmacopee.ladafura.dto.agent.collecte.AgentCreateCollecteRequest;
import com.pharmacopee.ladafura.dto.agent.collecte.AgentUpdateCollecteRequest;
import com.pharmacopee.ladafura.enums.Role;
import com.pharmacopee.ladafura.enums.StatutCollecte;
import com.pharmacopee.ladafura.enums.StatutUtilisateur;
import com.pharmacopee.ladafura.exceptions.BadRequestException;
import com.pharmacopee.ladafura.exceptions.ForbiddenException;
import com.pharmacopee.ladafura.exceptions.ResourceNotFoundException;
import com.pharmacopee.ladafura.mappers.AgentCollecteMapper;
import com.pharmacopee.ladafura.repository.CollecteRepository;
import com.pharmacopee.ladafura.repository.LocalisationRepository;
import com.pharmacopee.ladafura.repository.SourceRepository;
import com.pharmacopee.ladafura.services.impl.AgentCollecteServiceImpl;
import com.pharmacopee.ladafura.services.interfaces.IAgentAuthService;

@ExtendWith(MockitoExtension.class)
class AgentCollecteServiceImplTest {

    @Mock
    private CollecteRepository collecteRepository;

    @Mock
    private IAgentAuthService agentAuthService;

    @Spy
    private AgentCollecteMapper agentCollecteMapper = new AgentCollecteMapper();

    @Mock
    private SourceRepository sourceRepository;

    @Mock
    private LocalisationRepository localisationRepository;

    @InjectMocks
    private AgentCollecteServiceImpl collecteService;

    private AgentCollecte currentAgent;
    private Collecte testCollecte;

    @BeforeEach
    void setUp() {
        currentAgent = new AgentCollecte();
        currentAgent.setId(10L);
        currentAgent.setNom("Coulibaly");
        currentAgent.setPrenom("Oumar");
        currentAgent.setMatricule("AGT-2026-001");
        currentAgent.setRole(Role.AGENT_COLLECTE);
        currentAgent.setStatut(StatutUtilisateur.ACTIF);

        testCollecte = Collecte.builder()
                .id(100L)
                .dateCollecte(LocalDateTime.now())
                .description("Collecte initiale")
                .statut(StatutCollecte.BROUILLON)
                .agentCollecte(currentAgent)
                .build();
    }

    @Test
    @DisplayName("createCollecte - Crée une collecte en BROUILLON par défaut")
    void createCollecte_Draft_Success() {
        // GIVEN
        when(agentAuthService.getCurrentAgent()).thenReturn(currentAgent);
        when(collecteRepository.save(any(Collecte.class))).thenAnswer(inv -> {
            Collecte c = inv.getArgument(0);
            c.setId(101L);
            return c;
        });

        AgentCreateCollecteRequest request = AgentCreateCollecteRequest.builder()
                .description("Nouvelle collecte terrain")
                .soumettre(false)
                .build();

        // WHEN
        AgentCollecteDetailResponse response = collecteService.createCollecte(request);

        // THEN
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(101L);
        assertThat(response.getStatut()).isEqualTo(StatutCollecte.BROUILLON);
        assertThat(response.isModifiable()).isTrue();
        assertThat(response.isSoumissible()).isTrue();
    }

    @Test
    @DisplayName("createCollecte - Crée une collecte directement soumise si soumettre=true")
    void createCollecte_SubmitImmediately_Success() {
        // GIVEN
        when(agentAuthService.getCurrentAgent()).thenReturn(currentAgent);
        when(collecteRepository.save(any(Collecte.class))).thenAnswer(inv -> {
            Collecte c = inv.getArgument(0);
            c.setId(102L);
            return c;
        });

        AgentCreateCollecteRequest request = AgentCreateCollecteRequest.builder()
                .description("Collecte complète terrain")
                .soumettre(true)
                .build();

        // WHEN
        AgentCollecteDetailResponse response = collecteService.createCollecte(request);

        // THEN
        assertThat(response).isNotNull();
        assertThat(response.getStatut()).isEqualTo(StatutCollecte.SOUMISE);
        assertThat(response.getDateSoumission()).isNotNull();
        assertThat(response.isModifiable()).isFalse();
    }

    @Test
    @DisplayName("updateCollecte - Met à jour une collecte en statut BROUILLON")
    void updateCollecte_Draft_Success() {
        // GIVEN
        when(agentAuthService.getCurrentAgent()).thenReturn(currentAgent);
        when(collecteRepository.findById(100L)).thenReturn(Optional.of(testCollecte));
        when(collecteRepository.save(any(Collecte.class))).thenAnswer(inv -> inv.getArgument(0));

        AgentUpdateCollecteRequest request = AgentUpdateCollecteRequest.builder()
                .description("Description révisée")
                .photoUrl("https://storage.ladafura.ml/photo1.jpg")
                .build();

        // WHEN
        AgentCollecteDetailResponse response = collecteService.updateCollecte(100L, request);

        // THEN
        assertThat(response).isNotNull();
        assertThat(response.getDescription()).isEqualTo("Description révisée");
        assertThat(response.getPhotoUrl()).isEqualTo("https://storage.ladafura.ml/photo1.jpg");
    }

    @Test
    @DisplayName("updateCollecte - Échec si la collecte a déjà été soumise ou validée")
    void updateCollecte_ForbiddenStatus() {
        // GIVEN
        testCollecte.setStatut(StatutCollecte.SOUMISE);
        when(agentAuthService.getCurrentAgent()).thenReturn(currentAgent);
        when(collecteRepository.findById(100L)).thenReturn(Optional.of(testCollecte));

        AgentUpdateCollecteRequest request = AgentUpdateCollecteRequest.builder()
                .description("Tentative de modification")
                .build();

        // WHEN & THEN
        assertThatThrownBy(() -> collecteService.updateCollecte(100L, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Impossible de modifier une collecte avec le statut SOUMISE");
    }

    @Test
    @DisplayName("updateCollecte - Échec si la collecte appartient à un autre agent")
    void updateCollecte_WrongAgent() {
        // GIVEN
        AgentCollecte otherAgent = new AgentCollecte();
        otherAgent.setId(99L);
        testCollecte.setAgentCollecte(otherAgent);

        when(agentAuthService.getCurrentAgent()).thenReturn(currentAgent);
        when(collecteRepository.findById(100L)).thenReturn(Optional.of(testCollecte));

        AgentUpdateCollecteRequest request = AgentUpdateCollecteRequest.builder()
                .description("Modification interdite")
                .build();

        // WHEN & THEN
        assertThatThrownBy(() -> collecteService.updateCollecte(100L, request))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("cette collecte ne vous appartient pas");
    }

    @Test
    @DisplayName("getMyCollectes - Retourne la liste paginée filtrée par statut")
    void getMyCollectes_FilteredByStatus() {
        // GIVEN
        when(agentAuthService.getCurrentAgent()).thenReturn(currentAgent);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Collecte> page = new PageImpl<>(List.of(testCollecte), pageable, 1);
        when(collecteRepository.findByAgentCollecteIdAndStatut(10L, StatutCollecte.BROUILLON, pageable)).thenReturn(page);

        // WHEN
        Page<AgentCollecteSummaryResponse> result = collecteService.getMyCollectes(StatutCollecte.BROUILLON, pageable);

        // THEN
        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getId()).isEqualTo(100L);
    }

    @Test
    @DisplayName("submitCollecte - Soumet une collecte en BROUILLON vers SOUMISE")
    void submitCollecte_Success() {
        // GIVEN
        when(agentAuthService.getCurrentAgent()).thenReturn(currentAgent);
        when(collecteRepository.findById(100L)).thenReturn(Optional.of(testCollecte));
        when(collecteRepository.save(any(Collecte.class))).thenAnswer(inv -> inv.getArgument(0));

        // WHEN
        AgentCollecteDetailResponse response = collecteService.submitCollecte(100L);

        // THEN
        assertThat(response).isNotNull();
        assertThat(response.getStatut()).isEqualTo(StatutCollecte.SOUMISE);
        assertThat(response.getDateSoumission()).isNotNull();
    }

    @Test
    @DisplayName("deleteDraft - Supprime un brouillon de collecte avec succès")
    void deleteDraft_Success() {
        // GIVEN
        when(agentAuthService.getCurrentAgent()).thenReturn(currentAgent);
        when(collecteRepository.findById(100L)).thenReturn(Optional.of(testCollecte));

        // WHEN
        collecteService.deleteDraft(100L);

        // THEN
        verify(collecteRepository).delete(testCollecte);
    }

    @Test
    @DisplayName("deleteDraft - Échec si la collecte n'est pas un brouillon")
    void deleteDraft_NotDraft_ThrowsBadRequest() {
        // GIVEN
        testCollecte.setStatut(StatutCollecte.VALIDEE);
        when(agentAuthService.getCurrentAgent()).thenReturn(currentAgent);
        when(collecteRepository.findById(100L)).thenReturn(Optional.of(testCollecte));

        // WHEN & THEN
        assertThatThrownBy(() -> collecteService.deleteDraft(100L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Seules les collectes au statut BROUILLON peuvent être supprimées");
    }
}
