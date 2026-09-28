package com.pharmacopee.ladafura.services;

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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
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
import com.pharmacopee.ladafura.Models.Source;
import com.pharmacopee.ladafura.dto.agent.source.AgentCreateSourceRequest;
import com.pharmacopee.ladafura.dto.agent.source.AgentSourceResponse;
import com.pharmacopee.ladafura.dto.agent.source.AgentUpdateSourceRequest;
import com.pharmacopee.ladafura.enums.Role;
import com.pharmacopee.ladafura.enums.StatutCollecte;
import com.pharmacopee.ladafura.enums.StatutUtilisateur;
import com.pharmacopee.ladafura.exceptions.BadRequestException;
import com.pharmacopee.ladafura.exceptions.ConflictException;
import com.pharmacopee.ladafura.exceptions.ForbiddenException;
import com.pharmacopee.ladafura.repository.CollecteRepository;
import com.pharmacopee.ladafura.repository.SourceRepository;
import com.pharmacopee.ladafura.repository.UtilisateurRepository;
import com.pharmacopee.ladafura.services.impl.AgentSourceServiceImpl;
import com.pharmacopee.ladafura.services.interfaces.IAgentAuthService;

@ExtendWith(MockitoExtension.class)
class AgentSourceServiceImplTest {

    @Mock
    private SourceRepository sourceRepository;

    @Mock
    private CollecteRepository collecteRepository;

    @Mock
    private UtilisateurRepository utilisateurRepository;

    @Mock
    private IAgentAuthService agentAuthService;

    @InjectMocks
    private AgentSourceServiceImpl agentSourceService;

    private AgentCollecte agentConnecte;
    private Collecte collecteBrouillon;
    private Source source;

    @BeforeEach
    void setUp() {
        agentConnecte = new AgentCollecte();
        agentConnecte.setId(10L);
        agentConnecte.setNom("Coulibaly");
        agentConnecte.setPrenom("Oumar");

        collecteBrouillon = Collecte.builder()
                .id(100L)
                .statut(StatutCollecte.BROUILLON)
                .agentCollecte(agentConnecte)
                .vertus(new ArrayList<>())
                .build();

        source = new Source();
        source.setId(5L);
        source.setNom("Diarra");
        source.setPrenom("Amadou");
        source.setEmail("amadou.diarra@village.ml");
        source.setTelephone("+223 70 22 33 44");
        source.setAdresse("Finkolo");
        source.setSpecialite("Herboriste");
        source.setAnneesExperience(25);
        source.setRole(Role.HERBORISTE);
        source.setStatut(StatutUtilisateur.ACTIF);
        source.setCollectes(new ArrayList<>());
    }

    @Test
    @DisplayName("Créer une source avec génération d'email traçable unique (aucun compte Firebase)")
    void creerSource_Succes_SansEmailFourni() {
        AgentCreateSourceRequest request = AgentCreateSourceRequest.builder()
                .nom("Traoré")
                .prenom("Bakary")
                .telephone("+223 76 11 22 33")
                .adresse("Village de Finkolo")
                .specialite("Thérapeute traditionnel")
                .anneesExperience(30)
                .role(Role.THERAPEUTE)
                .build();

        when(sourceRepository.save(any(Source.class))).thenAnswer(invocation -> {
            Source s = invocation.getArgument(0);
            s.setId(15L);
            return s;
        });

        AgentSourceResponse response = agentSourceService.creerSource(request);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(15L);
        assertThat(response.getNomComplet()).isEqualTo("Bakary Traoré");
        assertThat(response.getEmail()).contains("source.bakary.traor");
        assertThat(response.getEmail()).endsWith("@terrain.ladafura.ml");
        assertThat(response.isEstCompteUtilisateur()).isFalse();

        verify(sourceRepository).save(any(Source.class));
    }

    @Test
    @DisplayName("Créer une source avec email fourni : rejet si conflit (409)")
    void creerSource_EmailExistant_ConflictException() {
        AgentCreateSourceRequest request = AgentCreateSourceRequest.builder()
                .nom("Diarra")
                .prenom("Amadou")
                .email("existant@ladafura.ml")
                .build();

        when(utilisateurRepository.existsByEmail("existant@ladafura.ml")).thenReturn(true);

        assertThatThrownBy(() -> agentSourceService.creerSource(request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("existe déjà");

        verify(sourceRepository, never()).save(any());
    }

    @Test
    @DisplayName("Créer une source et l'associer immédiatement à une collecte")
    void creerSource_AvecCollecteId_Succes() {
        AgentCreateSourceRequest request = AgentCreateSourceRequest.builder()
                .nom("Keita")
                .prenom("Salif")
                .collecteId(100L)
                .build();

        when(agentAuthService.getCurrentAgent()).thenReturn(agentConnecte);
        when(collecteRepository.findById(100L)).thenReturn(Optional.of(collecteBrouillon));
        when(sourceRepository.findById(15L)).thenReturn(Optional.of(source));

        when(sourceRepository.save(any(Source.class))).thenAnswer(invocation -> {
            Source s = invocation.getArgument(0);
            s.setId(15L);
            return s;
        });

        AgentSourceResponse response = agentSourceService.creerSource(request);

        assertThat(response).isNotNull();
        assertThat(collecteBrouillon.getSource()).isEqualTo(source);
        verify(collecteRepository).save(collecteBrouillon);
    }

    @Test
    @DisplayName("Modifier une source existante avec succès")
    void modifierSource_Succes() {
        AgentUpdateSourceRequest request = AgentUpdateSourceRequest.builder()
                .specialite("Tradipraticien en chef")
                .anneesExperience(35)
                .build();

        when(sourceRepository.findById(5L)).thenReturn(Optional.of(source));
        when(sourceRepository.save(source)).thenReturn(source);

        AgentSourceResponse response = agentSourceService.modifierSource(5L, request);

        assertThat(response).isNotNull();
        assertThat(source.getSpecialite()).isEqualTo("Tradipraticien en chef");
        assertThat(source.getAnneesExperience()).isEqualTo(35);
        verify(sourceRepository).save(source);
    }

    @Test
    @DisplayName("Consulter le détail d'une source par ID")
    void getSourceById_Succes() {
        when(sourceRepository.findById(5L)).thenReturn(Optional.of(source));

        AgentSourceResponse response = agentSourceService.getSourceById(5L);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(5L);
        assertThat(response.getNomComplet()).isEqualTo("Amadou Diarra");
        assertThat(response.isEstCompteUtilisateur()).isFalse();
    }

    @Test
    @DisplayName("Rechercher des sources de terrain avec pagination")
    void rechercherSources_Succes() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Source> page = new PageImpl<>(List.of(source), pageable, 1);

        when(sourceRepository.searchSources("Diarra", Role.HERBORISTE, pageable)).thenReturn(page);

        Page<AgentSourceResponse> result = agentSourceService.rechercherSources("Diarra", Role.HERBORISTE, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getNom()).isEqualTo("Diarra");
    }

    @Test
    @DisplayName("Associer une source à une collecte avec succès")
    void associerSourceACollecte_Succes() {
        when(agentAuthService.getCurrentAgent()).thenReturn(agentConnecte);
        when(collecteRepository.findById(100L)).thenReturn(Optional.of(collecteBrouillon));
        when(sourceRepository.findById(5L)).thenReturn(Optional.of(source));

        agentSourceService.associerSourceACollecte(100L, 5L);

        assertThat(collecteBrouillon.getSource()).isEqualTo(source);
        verify(collecteRepository).save(collecteBrouillon);
    }

    @Test
    @DisplayName("Associer une source : rejet si collecte d'un autre agent (403)")
    void associerSourceACollecte_NonProprietaire_ForbiddenException() {
        AgentCollecte autreAgent = new AgentCollecte();
        autreAgent.setId(99L);
        collecteBrouillon.setAgentCollecte(autreAgent);

        when(agentAuthService.getCurrentAgent()).thenReturn(agentConnecte);
        when(collecteRepository.findById(100L)).thenReturn(Optional.of(collecteBrouillon));

        assertThatThrownBy(() -> agentSourceService.associerSourceACollecte(100L, 5L))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("cette collecte ne vous appartient pas");

        verify(collecteRepository, never()).save(any());
    }

    @Test
    @DisplayName("Associer une source : rejet si collecte non modifiable (400)")
    void associerSourceACollecte_CollecteVerrouillee_BadRequestException() {
        collecteBrouillon.setStatut(StatutCollecte.VALIDEE);

        when(agentAuthService.getCurrentAgent()).thenReturn(agentConnecte);
        when(collecteRepository.findById(100L)).thenReturn(Optional.of(collecteBrouillon));

        assertThatThrownBy(() -> agentSourceService.associerSourceACollecte(100L, 5L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Impossible de modifier la source d'une collecte avec le statut VALIDEE");

        verify(collecteRepository, never()).save(any());
    }

    @Test
    @DisplayName("Dissocier la source d'une collecte avec succès")
    void dissocierSourceDeCollecte_Succes() {
        collecteBrouillon.setSource(source);

        when(agentAuthService.getCurrentAgent()).thenReturn(agentConnecte);
        when(collecteRepository.findById(100L)).thenReturn(Optional.of(collecteBrouillon));

        agentSourceService.dissocierSourceDeCollecte(100L);

        assertThat(collecteBrouillon.getSource()).isNull();
        verify(collecteRepository).save(collecteBrouillon);
    }
}
