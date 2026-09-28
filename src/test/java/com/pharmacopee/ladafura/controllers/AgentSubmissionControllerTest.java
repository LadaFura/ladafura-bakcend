package com.pharmacopee.ladafura.controllers;

import java.time.LocalDateTime;
import java.util.List;

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

import com.pharmacopee.ladafura.controllers.agent.AgentSubmissionController;
import com.pharmacopee.ladafura.dto.agent.submission.AgentCollecteRecapitulatifResponse;
import com.pharmacopee.ladafura.dto.agent.submission.AgentSubmissionResultResponse;
import com.pharmacopee.ladafura.enums.StatutCollecte;
import com.pharmacopee.ladafura.enums.StatutValidation;
import com.pharmacopee.ladafura.services.interfaces.IAgentSubmissionService;

@ExtendWith(MockitoExtension.class)
class AgentSubmissionControllerTest {

    @Mock
    private IAgentSubmissionService agentSubmissionService;

    @InjectMocks
    private AgentSubmissionController agentSubmissionController;

    @Test
    @DisplayName("GET /api/v1/agent/collectes/{collecteId}/recapitulatif - 200 OK")
    void getRecapitulatif_Succes() {
        AgentCollecteRecapitulatifResponse responseDto = AgentCollecteRecapitulatifResponse.builder()
                .collecteId(100L)
                .planteAssociee(true)
                .planteNomScientifique("Combretum micranthum")
                .nomsVernaculaires(List.of("Kinkéliba (Bambara)"))
                .connaissanceRenseignee(true)
                .sourceRattachee(true)
                .mediasPresents(true)
                .localisationRenseignee(true)
                .conformePourSoumission(true)
                .erreursBloquantes(List.of())
                .pointsDeVigilance(List.of())
                .prochaineEtape("SOUMISSION_AUTORISEE")
                .build();

        when(agentSubmissionService.getRecapitulatif(100L)).thenReturn(responseDto);

        ResponseEntity<AgentCollecteRecapitulatifResponse> response = agentSubmissionController.getRecapitulatif(100L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCollecteId()).isEqualTo(100L);
        assertThat(response.getBody().isConformePourSoumission()).isTrue();
        assertThat(response.getBody().getProchaineEtape()).isEqualTo("SOUMISSION_AUTORISEE");

        verify(agentSubmissionService).getRecapitulatif(100L);
    }

    @Test
    @DisplayName("POST /api/v1/agent/collectes/{collecteId}/soumettre - 200 OK")
    void soumettreCollecte_Succes() {
        AgentSubmissionResultResponse resultDto = AgentSubmissionResultResponse.builder()
                .collecteId(100L)
                .statut(StatutCollecte.SOUMISE)
                .dateSoumission(LocalDateTime.now())
                .statutConnaissances(StatutValidation.EN_ATTENTE)
                .verrouillee(true)
                .message("Collecte soumise avec succès pour validation.")
                .build();

        when(agentSubmissionService.soumettreCollecteVerifiee(100L)).thenReturn(resultDto);

        ResponseEntity<AgentSubmissionResultResponse> response = agentSubmissionController.soumettreCollecte(100L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCollecteId()).isEqualTo(100L);
        assertThat(response.getBody().getStatut()).isEqualTo(StatutCollecte.SOUMISE);
        assertThat(response.getBody().isVerrouillee()).isTrue();

        verify(agentSubmissionService).soumettreCollecteVerifiee(100L);
    }
}
