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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.pharmacopee.ladafura.Models.NomPlante;
import com.pharmacopee.ladafura.Models.Plante;
import com.pharmacopee.ladafura.dto.agent.vernaculaire.AgentNomVernaculaireRequest;
import com.pharmacopee.ladafura.dto.agent.vernaculaire.AgentNomVernaculaireResponse;
import com.pharmacopee.ladafura.exceptions.ConflictException;
import com.pharmacopee.ladafura.repository.NomPlanteRepository;
import com.pharmacopee.ladafura.repository.PlanteRepository;
import com.pharmacopee.ladafura.services.impl.AgentNomVernaculaireServiceImpl;

@ExtendWith(MockitoExtension.class)
class AgentNomVernaculaireServiceImplTest {

    @Mock
    private NomPlanteRepository nomPlanteRepository;

    @Mock
    private PlanteRepository planteRepository;

    @InjectMocks
    private AgentNomVernaculaireServiceImpl service;

    private Plante testPlante;
    private NomPlante testNom;

    @BeforeEach
    void setUp() {
        testPlante = Plante.builder()
                .id(1L)
                .nomScientifique("Combretum micranthum")
                .nomsPlante(new ArrayList<>())
                .build();

        testNom = NomPlante.builder()
                .id(10L)
                .nom("Kinkéliba")
                .langue("Bambara")
                .pays("Mali")
                .plante(testPlante)
                .build();
    }

    @Test
    @DisplayName("getNomsByPlanteId - Retourne la liste des noms vernaculaires")
    void getNomsByPlanteId_Success() {
        // GIVEN
        when(planteRepository.findById(1L)).thenReturn(Optional.of(testPlante));
        when(nomPlanteRepository.findByPlanteId(1L)).thenReturn(List.of(testNom));

        // WHEN
        List<AgentNomVernaculaireResponse> result = service.getNomsByPlanteId(1L);

        // THEN
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getNom()).isEqualTo("Kinkéliba");
        assertThat(result.get(0).getLangue()).isEqualTo("Bambara");
        assertThat(result.get(0).getNomScientifiquePlante()).isEqualTo("Combretum micranthum");
    }

    @Test
    @DisplayName("addNomVernaculaire - Ajoute avec succès un nom vernaculaire")
    void addNomVernaculaire_Success() {
        // GIVEN
        when(planteRepository.findById(1L)).thenReturn(Optional.of(testPlante));
        when(nomPlanteRepository.existsByPlanteIdAndNomIgnoreCaseAndLangueIgnoreCase(1L, "Sekeu", "Soninké")).thenReturn(false);
        when(nomPlanteRepository.save(any(NomPlante.class))).thenAnswer(inv -> {
            NomPlante n = inv.getArgument(0);
            n.setId(11L);
            return n;
        });

        AgentNomVernaculaireRequest request = AgentNomVernaculaireRequest.builder()
                .nom("Sekeu")
                .langue("Soninké")
                .pays("Mali")
                .build();

        // WHEN
        AgentNomVernaculaireResponse response = service.addNomVernaculaire(1L, request);

        // THEN
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(11L);
        assertThat(response.getNom()).isEqualTo("Sekeu");
        assertThat(response.getLangue()).isEqualTo("Soninké");
    }

    @Test
    @DisplayName("addNomVernaculaire - Échec en cas de doublon pour la même plante et même langue")
    void addNomVernaculaire_Conflict() {
        // GIVEN
        when(planteRepository.findById(1L)).thenReturn(Optional.of(testPlante));
        when(nomPlanteRepository.existsByPlanteIdAndNomIgnoreCaseAndLangueIgnoreCase(1L, "Kinkéliba", "Bambara")).thenReturn(true);

        AgentNomVernaculaireRequest request = AgentNomVernaculaireRequest.builder()
                .nom("Kinkéliba")
                .langue("Bambara")
                .build();

        // WHEN & THEN
        assertThatThrownBy(() -> service.addNomVernaculaire(1L, request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("existe déjà");
    }

    @Test
    @DisplayName("updateNomVernaculaire - Modifie le nom vernaculaire")
    void updateNomVernaculaire_Success() {
        // GIVEN
        when(planteRepository.findById(1L)).thenReturn(Optional.of(testPlante));
        when(nomPlanteRepository.findByIdAndPlanteId(10L, 1L)).thenReturn(Optional.of(testNom));
        when(nomPlanteRepository.save(any(NomPlante.class))).thenAnswer(inv -> inv.getArgument(0));

        AgentNomVernaculaireRequest request = AgentNomVernaculaireRequest.builder()
                .nom("Kinkeliba Rectifie")
                .langue("Bambara")
                .pays("Mali")
                .build();

        // WHEN
        AgentNomVernaculaireResponse response = service.updateNomVernaculaire(1L, 10L, request);

        // THEN
        assertThat(response).isNotNull();
        assertThat(response.getNom()).isEqualTo("Kinkeliba Rectifie");
    }

    @Test
    @DisplayName("deleteNomVernaculaire - Supprime le nom vernaculaire avec succès")
    void deleteNomVernaculaire_Success() {
        // GIVEN
        when(planteRepository.findById(1L)).thenReturn(Optional.of(testPlante));
        when(nomPlanteRepository.findByIdAndPlanteId(10L, 1L)).thenReturn(Optional.of(testNom));

        // WHEN
        service.deleteNomVernaculaire(1L, 10L);

        // THEN
        verify(nomPlanteRepository).delete(testNom);
    }

    @Test
    @DisplayName("searchNomsVernaculaires - Recherche combinée par nom et langue")
    void searchNomsVernaculaires_Success() {
        // GIVEN
        when(nomPlanteRepository.findByNomContainingIgnoreCaseAndLangueIgnoreCase("kink", "Bambara"))
                .thenReturn(List.of(testNom));

        // WHEN
        List<AgentNomVernaculaireResponse> result = service.searchNomsVernaculaires("kink", "Bambara");

        // THEN
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getNom()).isEqualTo("Kinkéliba");
    }
}
