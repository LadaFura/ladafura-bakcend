package com.pharmacopee.ladafura.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
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

import com.pharmacopee.ladafura.Models.AgentCollecte;
import com.pharmacopee.ladafura.Models.Collecte;
import com.pharmacopee.ladafura.Models.NomPlante;
import com.pharmacopee.ladafura.Models.Plante;
import com.pharmacopee.ladafura.Models.VertuDeLaPlante;
import com.pharmacopee.ladafura.dto.admin.plante.NomPlanteDto;
import com.pharmacopee.ladafura.dto.agent.plante.AgentCreatePlanteRequest;
import com.pharmacopee.ladafura.dto.agent.plante.AgentPlanteAssociationResponse;
import com.pharmacopee.ladafura.dto.agent.plante.AgentPlanteResponse;
import com.pharmacopee.ladafura.enums.Role;
import com.pharmacopee.ladafura.enums.StatutCollecte;
import com.pharmacopee.ladafura.enums.StatutPlante;
import com.pharmacopee.ladafura.enums.StatutUtilisateur;
import com.pharmacopee.ladafura.enums.StatutValidation;
import com.pharmacopee.ladafura.exceptions.BadRequestException;
import com.pharmacopee.ladafura.exceptions.ConflictException;
import com.pharmacopee.ladafura.exceptions.ForbiddenException;
import com.pharmacopee.ladafura.exceptions.ResourceNotFoundException;
import com.pharmacopee.ladafura.repository.CollecteRepository;
import com.pharmacopee.ladafura.repository.NomPlanteRepository;
import com.pharmacopee.ladafura.repository.PlanteRepository;
import com.pharmacopee.ladafura.repository.VertuDeLaPlanteRepository;
import com.pharmacopee.ladafura.services.impl.AgentPlanteServiceImpl;
import com.pharmacopee.ladafura.services.interfaces.IAgentAuthService;

@ExtendWith(MockitoExtension.class)
class AgentPlanteServiceImplTest {

    @Mock
    private PlanteRepository planteRepository;

    @Mock
    private NomPlanteRepository nomPlanteRepository;

    @Mock
    private CollecteRepository collecteRepository;

    @Mock
    private VertuDeLaPlanteRepository vertuDeLaPlanteRepository;

    @Mock
    private IAgentAuthService agentAuthService;

    @InjectMocks
    private AgentPlanteServiceImpl agentPlanteService;

    private AgentCollecte currentAgent;
    private Plante testPlante;
    private Collecte testCollecte;

    @BeforeEach
    void setUp() {
        currentAgent = new AgentCollecte();
        currentAgent.setId(10L);
        currentAgent.setNom("Coulibaly");
        currentAgent.setMatricule("AGT-2026-001");
        currentAgent.setRole(Role.AGENT_COLLECTE);
        currentAgent.setStatut(StatutUtilisateur.ACTIF);

        testPlante = Plante.builder()
                .id(1L)
                .nomScientifique("Combretum micranthum")
                .description("Arbuste buissonnant")
                .photoUrl("https://storage.ladafura.ml/kinkeliba.jpg")
                .statut(StatutPlante.VALIDE)
                .nomsPlante(new ArrayList<>())
                .vertus(new ArrayList<>())
                .build();

        NomPlante nomVernaculaire = NomPlante.builder()
                .id(101L)
                .nom("Kinkéliba")
                .langue("Bambara")
                .pays("Mali")
                .plante(testPlante)
                .build();
        testPlante.getNomsPlante().add(nomVernaculaire);

        testCollecte = Collecte.builder()
                .id(50L)
                .statut(StatutCollecte.BROUILLON)
                .agentCollecte(currentAgent)
                .vertus(new ArrayList<>())
                .build();
    }

    @Test
    @DisplayName("searchPlantes - Recherche par mot-clé avec pagination")
    void searchPlantes_WithKeyword() {
        // GIVEN
        Pageable pageable = PageRequest.of(0, 10);
        Page<Plante> page = new PageImpl<>(List.of(testPlante), pageable, 1);
        when(planteRepository.searchByKeyword("kinkeliba", pageable)).thenReturn(page);

        // WHEN
        Page<AgentPlanteResponse> result = agentPlanteService.searchPlantes("kinkeliba", pageable);

        // THEN
        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getNomScientifique()).isEqualTo("Combretum micranthum");
        assertThat(result.getContent().get(0).getNomsVernaculaires()).hasSize(1);
    }

    @Test
    @DisplayName("getPlanteById - Trouve la plante avec succès")
    void getPlanteById_Success() {
        // GIVEN
        when(planteRepository.findById(1L)).thenReturn(Optional.of(testPlante));

        // WHEN
        AgentPlanteResponse response = agentPlanteService.getPlanteById(1L);

        // THEN
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getNomScientifique()).isEqualTo("Combretum micranthum");
    }

    @Test
    @DisplayName("getPlanteById - Lève une exception si plante introuvable")
    void getPlanteById_NotFound() {
        // GIVEN
        when(planteRepository.findById(999L)).thenReturn(Optional.empty());

        // WHEN & THEN
        assertThatThrownBy(() -> agentPlanteService.getPlanteById(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("createPlante - Crée une nouvelle plante avec noms vernaculaires et statut BROUILLON")
    void createPlante_Success() {
        // GIVEN
        when(planteRepository.existsByNomScientifiqueIgnoreCase("Ziziphus mauritiana")).thenReturn(false);
        when(planteRepository.save(any(Plante.class))).thenAnswer(inv -> {
            Plante p = inv.getArgument(0);
            p.setId(2L);
            return p;
        });

        AgentCreatePlanteRequest request = AgentCreatePlanteRequest.builder()
                .nomScientifique("Ziziphus mauritiana")
                .description("Jujubier sauvage")
                .nomsVernaculaires(List.of(
                        NomPlanteDto.builder().nom("Tomon").langue("Bambara").pays("Mali").build()
                ))
                .build();

        // WHEN
        AgentPlanteResponse response = agentPlanteService.createPlante(request);

        // THEN
        assertThat(response).isNotNull();
        assertThat(response.getNomScientifique()).isEqualTo("Ziziphus mauritiana");
        assertThat(response.getStatut()).isEqualTo(StatutPlante.BROUILLON);
        verify(nomPlanteRepository).save(any(NomPlante.class));
    }

    @Test
    @DisplayName("createPlante - Échec en cas de doublon sur le nom scientifique")
    void createPlante_ConflictDuplicate() {
        // GIVEN
        when(planteRepository.existsByNomScientifiqueIgnoreCase("Combretum micranthum")).thenReturn(true);

        AgentCreatePlanteRequest request = AgentCreatePlanteRequest.builder()
                .nomScientifique("Combretum micranthum")
                .build();

        // WHEN & THEN
        assertThatThrownBy(() -> agentPlanteService.createPlante(request))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    @DisplayName("associatePlanteToCollecte - Associe une plante à une collecte au statut BROUILLON")
    void associatePlanteToCollecte_Success() {
        // GIVEN
        when(agentAuthService.getCurrentAgent()).thenReturn(currentAgent);
        when(collecteRepository.findById(50L)).thenReturn(Optional.of(testCollecte));
        when(planteRepository.findById(1L)).thenReturn(Optional.of(testPlante));
        when(vertuDeLaPlanteRepository.findByCollecteIdAndPlanteId(50L, 1L)).thenReturn(Optional.empty());

        when(vertuDeLaPlanteRepository.save(any(VertuDeLaPlante.class))).thenAnswer(inv -> {
            VertuDeLaPlante v = inv.getArgument(0);
            v.setId(201L);
            return v;
        });

        // WHEN
        AgentPlanteAssociationResponse response = agentPlanteService.associatePlanteToCollecte(50L, 1L);

        // THEN
        assertThat(response).isNotNull();
        assertThat(response.getCollecteId()).isEqualTo(50L);
        assertThat(response.getPlanteId()).isEqualTo(1L);
        assertThat(response.getStatutAssociation()).isEqualTo(StatutValidation.BROUILLON);
    }

    @Test
    @DisplayName("associatePlanteToCollecte - Idempotent si la plante est déjà associée")
    void associatePlanteToCollecte_AlreadyAssociated() {
        // GIVEN
        when(agentAuthService.getCurrentAgent()).thenReturn(currentAgent);
        when(collecteRepository.findById(50L)).thenReturn(Optional.of(testCollecte));
        when(planteRepository.findById(1L)).thenReturn(Optional.of(testPlante));

        VertuDeLaPlante existingVertu = VertuDeLaPlante.builder()
                .id(201L)
                .collecte(testCollecte)
                .plante(testPlante)
                .statut(StatutValidation.BROUILLON)
                .build();
        when(vertuDeLaPlanteRepository.findByCollecteIdAndPlanteId(50L, 1L)).thenReturn(Optional.of(existingVertu));

        // WHEN
        AgentPlanteAssociationResponse response = agentPlanteService.associatePlanteToCollecte(50L, 1L);

        // THEN
        assertThat(response).isNotNull();
        assertThat(response.getVertuId()).isEqualTo(201L);
    }

    @Test
    @DisplayName("associatePlanteToCollecte - Échec si la collecte appartient à un autre agent")
    void associatePlanteToCollecte_WrongAgent() {
        // GIVEN
        AgentCollecte other = new AgentCollecte();
        other.setId(99L);
        testCollecte.setAgentCollecte(other);

        when(agentAuthService.getCurrentAgent()).thenReturn(currentAgent);
        when(collecteRepository.findById(50L)).thenReturn(Optional.of(testCollecte));

        // WHEN & THEN
        assertThatThrownBy(() -> agentPlanteService.associatePlanteToCollecte(50L, 1L))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("cette collecte ne vous appartient pas");
    }

    @Test
    @DisplayName("associatePlanteToCollecte - Échec si la collecte n'est plus modifiable")
    void associatePlanteToCollecte_NotModifiable() {
        // GIVEN
        testCollecte.setStatut(StatutCollecte.VALIDEE);

        when(agentAuthService.getCurrentAgent()).thenReturn(currentAgent);
        when(collecteRepository.findById(50L)).thenReturn(Optional.of(testCollecte));

        // WHEN & THEN
        assertThatThrownBy(() -> agentPlanteService.associatePlanteToCollecte(50L, 1L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Impossible de modifier une collecte avec le statut VALIDEE");
    }

    @Test
    @DisplayName("dissociatePlanteFromCollecte - Dissocie la plante avec succès")
    void dissociatePlanteFromCollecte_Success() {
        // GIVEN
        when(agentAuthService.getCurrentAgent()).thenReturn(currentAgent);
        when(collecteRepository.findById(50L)).thenReturn(Optional.of(testCollecte));

        VertuDeLaPlante vertu = VertuDeLaPlante.builder()
                .id(201L)
                .collecte(testCollecte)
                .plante(testPlante)
                .build();
        testCollecte.getVertus().add(vertu);
        when(vertuDeLaPlanteRepository.findByCollecteIdAndPlanteId(50L, 1L)).thenReturn(Optional.of(vertu));

        // WHEN
        agentPlanteService.dissociatePlanteFromCollecte(50L, 1L);

        // THEN
        verify(vertuDeLaPlanteRepository).delete(vertu);
        assertThat(testCollecte.getVertus()).doesNotContain(vertu);
    }
}
