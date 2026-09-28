package com.pharmacopee.ladafura.services;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
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
import com.pharmacopee.ladafura.Models.Plante;
import com.pharmacopee.ladafura.Models.Source;
import com.pharmacopee.ladafura.Models.VertuDeLaPlante;
import com.pharmacopee.ladafura.dto.agent.collecte.AgentCollecteSummaryResponse;
import com.pharmacopee.ladafura.dto.agent.suivi.AgentCollecteRejetDetailResponse;
import com.pharmacopee.ladafura.dto.agent.suivi.AgentSuiviStatsResponse;
import com.pharmacopee.ladafura.enums.StatutCollecte;
import com.pharmacopee.ladafura.exceptions.BadRequestException;
import com.pharmacopee.ladafura.exceptions.ForbiddenException;
import com.pharmacopee.ladafura.exceptions.ResourceNotFoundException;
import com.pharmacopee.ladafura.mappers.AgentCollecteMapper;
import com.pharmacopee.ladafura.repository.CollecteRepository;
import com.pharmacopee.ladafura.services.impl.AgentSuiviCollecteServiceImpl;
import com.pharmacopee.ladafura.services.interfaces.IAgentAuthService;

@ExtendWith(MockitoExtension.class)
class AgentSuiviCollecteServiceImplTest {

    @Mock
    private CollecteRepository collecteRepository;

    @Spy
    private AgentCollecteMapper agentCollecteMapper = new AgentCollecteMapper();

    @Mock
    private IAgentAuthService agentAuthService;

    @InjectMocks
    private AgentSuiviCollecteServiceImpl agentSuiviCollecteService;

    private AgentCollecte agentConnecte;
    private Collecte collecteRejetee;
    private Plante plante;
    private VertuDeLaPlante vertu;
    private Source source;
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
                .build();

        source = new Source();
        source.setId(5L);
        source.setNom("Diarra");
        source.setPrenom("Amadou");
        source.setSpecialite("Herboriste traditionnel");

        plante = Plante.builder()
                .id(3L)
                .nomScientifique("Combretum micranthum")
                .build();

        vertu = VertuDeLaPlante.builder()
                .id(50L)
                .usageRapporte("Traitement des fièvres")
                .plante(plante)
                .build();

        collecteRejetee = Collecte.builder()
                .id(100L)
                .dateCollecte(LocalDateTime.of(2026, 9, 28, 10, 0))
                .dateSoumission(LocalDateTime.of(2026, 9, 28, 14, 0))
                .statut(StatutCollecte.REJETEE)
                .motifRejet("Photos floues ne permettant pas d'identifier les nervures foliaires.")
                .agentCollecte(agentConnecte)
                .source(source)
                .localisation(localisation)
                .vertus(new ArrayList<>(List.of(vertu)))
                .build();
    }

    @Test
    @DisplayName("Statistiques de suivi : calculs réels et taux de validation")
    void getStatsSuivi_Succes() {
        when(agentAuthService.getCurrentAgent()).thenReturn(agentConnecte);
        when(collecteRepository.countByAgentCollecteId(10L)).thenReturn(16L);
        when(collecteRepository.countByAgentCollecteIdAndStatut(10L, StatutCollecte.BROUILLON)).thenReturn(2L);
        when(collecteRepository.countByAgentCollecteIdAndStatut(10L, StatutCollecte.SOUMISE)).thenReturn(3L);
        when(collecteRepository.countByAgentCollecteIdAndStatut(10L, StatutCollecte.EN_EXAMEN)).thenReturn(1L);
        when(collecteRepository.countByAgentCollecteIdAndStatut(10L, StatutCollecte.VALIDEE)).thenReturn(8L);
        when(collecteRepository.countByAgentCollecteIdAndStatut(10L, StatutCollecte.REJETEE)).thenReturn(2L);

        AgentSuiviStatsResponse stats = agentSuiviCollecteService.getStatsSuivi();

        assertThat(stats).isNotNull();
        assertThat(stats.getTotal()).isEqualTo(16L);
        assertThat(stats.getBrouillons()).isEqualTo(2L);
        assertThat(stats.getSoumises()).isEqualTo(3L);
        assertThat(stats.getEnExamen()).isEqualTo(1L);
        assertThat(stats.getValidees()).isEqualTo(8L);
        assertThat(stats.getRejetees()).isEqualTo(2L);
        assertThat(stats.getNecessitantCorrection()).isEqualTo(2L);
        assertThat(stats.getTauxValidation()).isEqualTo(80.0);
    }

    @Test
    @DisplayName("Statistiques de suivi : taux zéro si aucune collecte instruite")
    void getStatsSuivi_ZeroInstruites() {
        when(agentAuthService.getCurrentAgent()).thenReturn(agentConnecte);
        when(collecteRepository.countByAgentCollecteId(10L)).thenReturn(5L);
        when(collecteRepository.countByAgentCollecteIdAndStatut(10L, StatutCollecte.BROUILLON)).thenReturn(5L);
        when(collecteRepository.countByAgentCollecteIdAndStatut(10L, StatutCollecte.SOUMISE)).thenReturn(0L);
        when(collecteRepository.countByAgentCollecteIdAndStatut(10L, StatutCollecte.EN_EXAMEN)).thenReturn(0L);
        when(collecteRepository.countByAgentCollecteIdAndStatut(10L, StatutCollecte.VALIDEE)).thenReturn(0L);
        when(collecteRepository.countByAgentCollecteIdAndStatut(10L, StatutCollecte.REJETEE)).thenReturn(0L);

        AgentSuiviStatsResponse stats = agentSuiviCollecteService.getStatsSuivi();

        assertThat(stats.getTauxValidation()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("Consulter les collectes par statut (BROUILLON)")
    void getCollectesParStatut_Succes() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Collecte> page = new PageImpl<>(List.of(collecteRejetee), pageable, 1);

        when(agentAuthService.getCurrentAgent()).thenReturn(agentConnecte);
        when(collecteRepository.findByAgentCollecteIdAndStatut(10L, StatutCollecte.REJETEE, pageable)).thenReturn(page);

        Page<AgentCollecteSummaryResponse> result = agentSuiviCollecteService.getCollectesParStatut(StatutCollecte.REJETEE, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getId()).isEqualTo(100L);
        assertThat(result.getContent().get(0).getStatut()).isEqualTo(StatutCollecte.REJETEE);
        assertThat(result.getContent().get(0).getNomScientifiquePlante()).isEqualTo("Combretum micranthum");
    }

    @Test
    @DisplayName("Consulter les brouillons")
    void getBrouillons_Succes() {
        Pageable pageable = PageRequest.of(0, 10);
        collecteRejetee.setStatut(StatutCollecte.BROUILLON);
        Page<Collecte> page = new PageImpl<>(List.of(collecteRejetee), pageable, 1);

        when(agentAuthService.getCurrentAgent()).thenReturn(agentConnecte);
        when(collecteRepository.findByAgentCollecteIdAndStatut(10L, StatutCollecte.BROUILLON, pageable)).thenReturn(page);

        Page<AgentCollecteSummaryResponse> result = agentSuiviCollecteService.getBrouillons(pageable);

        assertThat(result.getContent().get(0).getStatut()).isEqualTo(StatutCollecte.BROUILLON);
    }

    @Test
    @DisplayName("Consulter le détail du motif de rejet d'une collecte")
    void getDetailRejet_Succes() {
        when(agentAuthService.getCurrentAgent()).thenReturn(agentConnecte);
        when(collecteRepository.findById(100L)).thenReturn(Optional.of(collecteRejetee));

        AgentCollecteRejetDetailResponse detail = agentSuiviCollecteService.getDetailRejet(100L);

        assertThat(detail).isNotNull();
        assertThat(detail.getCollecteId()).isEqualTo(100L);
        assertThat(detail.getStatut()).isEqualTo(StatutCollecte.REJETEE);
        assertThat(detail.getMotifRejet()).isEqualTo("Photos floues ne permettant pas d'identifier les nervures foliaires.");
        assertThat(detail.getPlanteNomScientifique()).isEqualTo("Combretum micranthum");
        assertThat(detail.getLocalite()).isEqualTo("Finkolo Centre");
        assertThat(detail.getRegion()).isEqualTo("Sikasso");
        assertThat(detail.getSourceNomComplet()).isEqualTo("Amadou Diarra");
        assertThat(detail.isModifiable()).isTrue();
        assertThat(detail.getGuideCorrection()).hasSize(4);
    }

    @Test
    @DisplayName("Détail de rejet : 400 si la collecte n'est pas au statut REJETEE")
    void getDetailRejet_CollecteNonRejetee_BadRequestException() {
        collecteRejetee.setStatut(StatutCollecte.VALIDEE);

        when(agentAuthService.getCurrentAgent()).thenReturn(agentConnecte);
        when(collecteRepository.findById(100L)).thenReturn(Optional.of(collecteRejetee));

        assertThatThrownBy(() -> agentSuiviCollecteService.getDetailRejet(100L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Cette collecte n'est pas au statut REJETEE mais au statut VALIDEE");
    }

    @Test
    @DisplayName("Détail de rejet : 403 si la collecte appartient à un autre agent")
    void getDetailRejet_NonProprietaire_ForbiddenException() {
        AgentCollecte autreAgent = new AgentCollecte();
        autreAgent.setId(99L);
        collecteRejetee.setAgentCollecte(autreAgent);

        when(agentAuthService.getCurrentAgent()).thenReturn(agentConnecte);
        when(collecteRepository.findById(100L)).thenReturn(Optional.of(collecteRejetee));

        assertThatThrownBy(() -> agentSuiviCollecteService.getDetailRejet(100L))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("cette collecte ne vous appartient pas");
    }

    @Test
    @DisplayName("Détail de rejet : 404 si la collecte est introuvable")
    void getDetailRejet_Introuvable_ResourceNotFoundException() {
        when(agentAuthService.getCurrentAgent()).thenReturn(agentConnecte);
        when(collecteRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> agentSuiviCollecteService.getDetailRejet(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Recherche multi-critères dans le suivi des collectes")
    void searchSuiviCollectes_Succes() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Collecte> page = new PageImpl<>(List.of(collecteRejetee), pageable, 1);

        when(agentAuthService.getCurrentAgent()).thenReturn(agentConnecte);
        when(collecteRepository.searchSuiviCollectes(10L, StatutCollecte.REJETEE, "kinkeliba", pageable)).thenReturn(page);

        Page<AgentCollecteSummaryResponse> result = agentSuiviCollecteService.searchSuiviCollectes(StatutCollecte.REJETEE, "kinkeliba", pageable);

        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(1);
    }
}
