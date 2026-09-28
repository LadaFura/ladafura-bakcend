package com.pharmacopee.ladafura.services;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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

import com.pharmacopee.ladafura.Models.AgentCollecte;
import com.pharmacopee.ladafura.Models.Collecte;
import com.pharmacopee.ladafura.Models.Maladie;
import com.pharmacopee.ladafura.Models.Plante;
import com.pharmacopee.ladafura.Models.Source;
import com.pharmacopee.ladafura.Models.VertuDeLaPlante;
import com.pharmacopee.ladafura.dto.agent.connaissance.AgentConnaissanceRequest;
import com.pharmacopee.ladafura.dto.agent.connaissance.AgentConnaissanceResponse;
import com.pharmacopee.ladafura.dto.agent.connaissance.AgentConnaissanceUpdateRequest;
import com.pharmacopee.ladafura.enums.StatutCollecte;
import com.pharmacopee.ladafura.enums.StatutValidation;
import com.pharmacopee.ladafura.exceptions.BadRequestException;
import com.pharmacopee.ladafura.exceptions.ForbiddenException;
import com.pharmacopee.ladafura.exceptions.ResourceNotFoundException;
import com.pharmacopee.ladafura.repository.CollecteRepository;
import com.pharmacopee.ladafura.repository.MaladieRepository;
import com.pharmacopee.ladafura.repository.PlanteRepository;
import com.pharmacopee.ladafura.repository.SourceRepository;
import com.pharmacopee.ladafura.repository.VertuDeLaPlanteRepository;
import com.pharmacopee.ladafura.services.impl.AgentConnaissanceServiceImpl;
import com.pharmacopee.ladafura.services.interfaces.IAgentAuthService;

@ExtendWith(MockitoExtension.class)
class AgentConnaissanceServiceImplTest {

    @Mock
    private VertuDeLaPlanteRepository vertuDeLaPlanteRepository;

    @Mock
    private CollecteRepository collecteRepository;

    @Mock
    private PlanteRepository planteRepository;

    @Mock
    private SourceRepository sourceRepository;

    @Mock
    private MaladieRepository maladieRepository;

    @Mock
    private IAgentAuthService agentAuthService;

    @InjectMocks
    private AgentConnaissanceServiceImpl agentConnaissanceService;

    private AgentCollecte agentConnecte;
    private Collecte collecteBrouillon;
    private Plante plante;
    private Source source;
    private Maladie maladie;

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

        plante = Plante.builder()
                .id(1L)
                .nomScientifique("Combretum micranthum")
                .nomsPlante(new ArrayList<>())
                .maladies(new HashSet<>())
                .vertus(new ArrayList<>())
                .build();

        source = new Source();
        source.setId(5L);
        source.setNom("Diarra");
        source.setPrenom("Amadou");
        source.setSpecialite("Herboriste traditionnel");

        maladie = Maladie.builder()
                .id(1L)
                .nom("Paludisme")
                .build();
    }

    @Test
    @DisplayName("Enregistrer une nouvelle connaissance traditionnelle avec succès")
    void enregistrerConnaissance_Succes_NouvelleVertu() {
        AgentConnaissanceRequest request = AgentConnaissanceRequest.builder()
                .collecteId(100L)
                .planteId(1L)
                .usageRapporte("Traitement traditionnel des fièvres palustres")
                .partieUtilisee("Feuilles séchées")
                .preparation("Décoction 15 min")
                .precaution("Déconseillé aux femmes enceintes")
                .description("Récolté au lever du soleil")
                .sourceId(5L)
                .maladieIds(Set.of(1L))
                .build();

        when(agentAuthService.getCurrentAgent()).thenReturn(agentConnecte);
        when(collecteRepository.findById(100L)).thenReturn(Optional.of(collecteBrouillon));
        when(planteRepository.findById(1L)).thenReturn(Optional.of(plante));
        when(sourceRepository.findById(5L)).thenReturn(Optional.of(source));
        when(maladieRepository.findAllById(Set.of(1L))).thenReturn(List.of(maladie));
        when(vertuDeLaPlanteRepository.findByCollecteIdAndPlanteId(100L, 1L)).thenReturn(Optional.empty());

        VertuDeLaPlante savedVertu = VertuDeLaPlante.builder()
                .id(20L)
                .collecte(collecteBrouillon)
                .plante(plante)
                .usageRapporte(request.getUsageRapporte())
                .partieUtilisee(request.getPartieUtilisee())
                .preparation(request.getPreparation())
                .precaution(request.getPrecaution())
                .description(request.getDescription())
                .statut(StatutValidation.BROUILLON)
                .build();

        when(vertuDeLaPlanteRepository.save(any(VertuDeLaPlante.class))).thenReturn(savedVertu);

        AgentConnaissanceResponse response = agentConnaissanceService.enregistrerConnaissance(request);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(20L);
        assertThat(response.getUsageRapporte()).isEqualTo("Traitement traditionnel des fièvres palustres");
        assertThat(response.getPartieUtilisee()).isEqualTo("Feuilles séchées");
        assertThat(response.isPreuveScientifique()).isFalse();
        assertThat(response.getTypeInformation()).isEqualTo("CONNAISSANCE_TRADITIONNELLE");
        assertThat(response.getSourceNomComplet()).isEqualTo("Amadou Diarra");

        verify(vertuDeLaPlanteRepository).save(any(VertuDeLaPlante.class));
        verify(collecteRepository).save(collecteBrouillon);
    }

    @Test
    @DisplayName("Enregistrer une connaissance : mise à jour si vertu déjà existante")
    void enregistrerConnaissance_Succes_MiseAJourVertuExistante() {
        AgentConnaissanceRequest request = AgentConnaissanceRequest.builder()
                .collecteId(100L)
                .planteId(1L)
                .usageRapporte("Nouvel usage complété")
                .partieUtilisee("Écorce")
                .preparation("Infusion")
                .build();

        VertuDeLaPlante existing = VertuDeLaPlante.builder()
                .id(20L)
                .collecte(collecteBrouillon)
                .plante(plante)
                .statut(StatutValidation.BROUILLON)
                .build();

        when(agentAuthService.getCurrentAgent()).thenReturn(agentConnecte);
        when(collecteRepository.findById(100L)).thenReturn(Optional.of(collecteBrouillon));
        when(planteRepository.findById(1L)).thenReturn(Optional.of(plante));
        when(vertuDeLaPlanteRepository.findByCollecteIdAndPlanteId(100L, 1L)).thenReturn(Optional.of(existing));
        when(vertuDeLaPlanteRepository.save(existing)).thenReturn(existing);

        AgentConnaissanceResponse response = agentConnaissanceService.enregistrerConnaissance(request);

        assertThat(response).isNotNull();
        assertThat(existing.getUsageRapporte()).isEqualTo("Nouvel usage complété");
        assertThat(existing.getPartieUtilisee()).isEqualTo("Écorce");
        verify(vertuDeLaPlanteRepository).save(existing);
    }

    @Test
    @DisplayName("Enregistrer une connaissance : rejeter si la collecte appartient à un autre agent (403)")
    void enregistrerConnaissance_CollecteNonProprietaire_ForbiddenException() {
        AgentCollecte autreAgent = new AgentCollecte();
        autreAgent.setId(99L);

        collecteBrouillon.setAgentCollecte(autreAgent);

        AgentConnaissanceRequest request = AgentConnaissanceRequest.builder()
                .collecteId(100L)
                .planteId(1L)
                .usageRapporte("Usage")
                .build();

        when(agentAuthService.getCurrentAgent()).thenReturn(agentConnecte);
        when(collecteRepository.findById(100L)).thenReturn(Optional.of(collecteBrouillon));

        assertThatThrownBy(() -> agentConnaissanceService.enregistrerConnaissance(request))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("cette collecte ne vous appartient pas");

        verify(vertuDeLaPlanteRepository, never()).save(any());
    }

    @Test
    @DisplayName("Enregistrer une connaissance : rejeter si la collecte est déjà soumise (400)")
    void enregistrerConnaissance_CollecteNonModifiable_BadRequestException() {
        collecteBrouillon.setStatut(StatutCollecte.SOUMISE);

        AgentConnaissanceRequest request = AgentConnaissanceRequest.builder()
                .collecteId(100L)
                .planteId(1L)
                .usageRapporte("Usage")
                .build();

        when(agentAuthService.getCurrentAgent()).thenReturn(agentConnecte);
        when(collecteRepository.findById(100L)).thenReturn(Optional.of(collecteBrouillon));

        assertThatThrownBy(() -> agentConnaissanceService.enregistrerConnaissance(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Impossible de modifier une collecte avec le statut SOUMISE");

        verify(vertuDeLaPlanteRepository, never()).save(any());
    }

    @Test
    @DisplayName("Modifier une connaissance traditionnelle avec succès")
    void modifierConnaissance_Succes() {
        VertuDeLaPlante vertu = VertuDeLaPlante.builder()
                .id(20L)
                .collecte(collecteBrouillon)
                .plante(plante)
                .usageRapporte("Ancien usage")
                .statut(StatutValidation.BROUILLON)
                .build();

        AgentConnaissanceUpdateRequest request = AgentConnaissanceUpdateRequest.builder()
                .usageRapporte("Usage révisé et précisé")
                .partieUtilisee("Feuilles fraîches")
                .preparation("Macération aqueuse")
                .build();

        when(agentAuthService.getCurrentAgent()).thenReturn(agentConnecte);
        when(vertuDeLaPlanteRepository.findById(20L)).thenReturn(Optional.of(vertu));
        when(vertuDeLaPlanteRepository.save(vertu)).thenReturn(vertu);

        AgentConnaissanceResponse response = agentConnaissanceService.modifierConnaissance(20L, request);

        assertThat(response).isNotNull();
        assertThat(vertu.getUsageRapporte()).isEqualTo("Usage révisé et précisé");
        assertThat(vertu.getPartieUtilisee()).isEqualTo("Feuilles fraîches");
        verify(vertuDeLaPlanteRepository).save(vertu);
    }

    @Test
    @DisplayName("Consulter une connaissance traditionnelle par son identifiant")
    void getConnaissanceById_Succes() {
        VertuDeLaPlante vertu = VertuDeLaPlante.builder()
                .id(20L)
                .collecte(collecteBrouillon)
                .plante(plante)
                .usageRapporte("Usage traditionnel documenté")
                .statut(StatutValidation.BROUILLON)
                .build();

        when(vertuDeLaPlanteRepository.findById(20L)).thenReturn(Optional.of(vertu));

        AgentConnaissanceResponse response = agentConnaissanceService.getConnaissanceById(20L);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(20L);
        assertThat(response.getPlanteNomScientifique()).isEqualTo("Combretum micranthum");
    }

    @Test
    @DisplayName("Lister les connaissances traditionnelles d'une collecte")
    void getConnaissancesByCollecte_Succes() {
        VertuDeLaPlante v1 = VertuDeLaPlante.builder().id(1L).collecte(collecteBrouillon).plante(plante).usageRapporte("U1").build();
        VertuDeLaPlante v2 = VertuDeLaPlante.builder().id(2L).collecte(collecteBrouillon).plante(plante).usageRapporte("U2").build();

        when(agentAuthService.getCurrentAgent()).thenReturn(agentConnecte);
        when(collecteRepository.findById(100L)).thenReturn(Optional.of(collecteBrouillon));
        when(vertuDeLaPlanteRepository.findByCollecteId(100L)).thenReturn(List.of(v1, v2));

        List<AgentConnaissanceResponse> result = agentConnaissanceService.getConnaissancesByCollecte(100L);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getUsageRapporte()).isEqualTo("U1");
        assertThat(result.get(1).getUsageRapporte()).isEqualTo("U2");
    }

    @Test
    @DisplayName("Supprimer une connaissance traditionnelle d'une collecte en brouillon")
    void supprimerConnaissance_Succes() {
        VertuDeLaPlante vertu = VertuDeLaPlante.builder()
                .id(20L)
                .collecte(collecteBrouillon)
                .plante(plante)
                .build();

        when(agentAuthService.getCurrentAgent()).thenReturn(agentConnecte);
        when(vertuDeLaPlanteRepository.findById(20L)).thenReturn(Optional.of(vertu));

        agentConnaissanceService.supprimerConnaissance(20L);

        verify(vertuDeLaPlanteRepository).delete(vertu);
    }

    @Test
    @DisplayName("Supprimer une connaissance : rejeter si la collecte n'est plus en brouillon")
    void supprimerConnaissance_CollecteNonBrouillon_BadRequestException() {
        collecteBrouillon.setStatut(StatutCollecte.SOUMISE);
        VertuDeLaPlante vertu = VertuDeLaPlante.builder()
                .id(20L)
                .collecte(collecteBrouillon)
                .plante(plante)
                .build();

        when(agentAuthService.getCurrentAgent()).thenReturn(agentConnecte);
        when(vertuDeLaPlanteRepository.findById(20L)).thenReturn(Optional.of(vertu));

        assertThatThrownBy(() -> agentConnaissanceService.supprimerConnaissance(20L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Impossible de supprimer une connaissance d'une collecte qui n'est plus en statut BROUILLON");

        verify(vertuDeLaPlanteRepository, never()).delete(any());
    }
}
