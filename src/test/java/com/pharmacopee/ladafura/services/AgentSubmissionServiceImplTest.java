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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.pharmacopee.ladafura.Models.AgentCollecte;
import com.pharmacopee.ladafura.Models.Collecte;
import com.pharmacopee.ladafura.Models.Localisation;
import com.pharmacopee.ladafura.Models.Maladie;
import com.pharmacopee.ladafura.Models.NomPlante;
import com.pharmacopee.ladafura.Models.Plante;
import com.pharmacopee.ladafura.Models.Source;
import com.pharmacopee.ladafura.Models.VertuDeLaPlante;
import com.pharmacopee.ladafura.dto.agent.submission.AgentCollecteRecapitulatifResponse;
import com.pharmacopee.ladafura.dto.agent.submission.AgentSubmissionResultResponse;
import com.pharmacopee.ladafura.enums.StatutCollecte;
import com.pharmacopee.ladafura.enums.StatutValidation;
import com.pharmacopee.ladafura.exceptions.BadRequestException;
import com.pharmacopee.ladafura.exceptions.ForbiddenException;
import com.pharmacopee.ladafura.exceptions.ResourceNotFoundException;
import com.pharmacopee.ladafura.repository.CollecteRepository;
import com.pharmacopee.ladafura.repository.VertuDeLaPlanteRepository;
import com.pharmacopee.ladafura.services.impl.AgentSubmissionServiceImpl;
import com.pharmacopee.ladafura.services.interfaces.IAgentAuthService;

@ExtendWith(MockitoExtension.class)
class AgentSubmissionServiceImplTest {

    @Mock
    private CollecteRepository collecteRepository;

    @Mock
    private VertuDeLaPlanteRepository vertuDeLaPlanteRepository;

    @Mock
    private IAgentAuthService agentAuthService;

    @InjectMocks
    private AgentSubmissionServiceImpl agentSubmissionService;

    private AgentCollecte agentConnecte;
    private Collecte collecteBrouillon;
    private Plante plante;
    private VertuDeLaPlante vertu;
    private Source source;
    private Localisation localisation;

    @BeforeEach
    void setUp() {
        agentConnecte = new AgentCollecte();
        agentConnecte.setId(1L);
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

        source = new Source();
        source.setId(5L);
        source.setNom("Diarra");
        source.setPrenom("Amadou");
        source.setSpecialite("Herboriste traditionnel");

        NomPlante nomVernaculaire = NomPlante.builder()
                .id(10L)
                .nom("Kinkéliba")
                .langue("Bambara")
                .pays("Mali")
                .build();

        Maladie maladie = new Maladie();
        maladie.setId(20L);
        maladie.setNom("Paludisme");

        plante = Plante.builder()
                .id(3L)
                .nomScientifique("Combretum micranthum")
                .nomsPlante(new ArrayList<>(List.of(nomVernaculaire)))
                .maladies(new java.util.HashSet<>(List.of(maladie)))
                .build();

        vertu = VertuDeLaPlante.builder()
                .id(50L)
                .usageRapporte("Traitement traditionnel de la fièvre et du paludisme")
                .partieUtilisee("Feuilles séchées")
                .preparation("Décoction 15 min")
                .precaution("Déconseillé aux femmes enceintes")
                .plante(plante)
                .statut(StatutValidation.BROUILLON)
                .build();

        collecteBrouillon = Collecte.builder()
                .id(100L)
                .dateCollecte(LocalDateTime.of(2026, 9, 28, 10, 0))
                .description("Mission de terrain cercle de Koutiala")
                .statut(StatutCollecte.BROUILLON)
                .photoUrl("/uploads/collectes/photos/kinkeliba.jpg")
                .audioUrl("/uploads/collectes/audios/temoignage.mp3")
                .agentCollecte(agentConnecte)
                .source(source)
                .localisation(localisation)
                .build();
    }

    @Test
    @DisplayName("Récapitulatif : dossier complet conforme pour soumission")
    void getRecapitulatif_Succes_DossierComplet() {
        when(agentAuthService.getCurrentAgent()).thenReturn(agentConnecte);
        when(collecteRepository.findById(100L)).thenReturn(Optional.of(collecteBrouillon));
        when(vertuDeLaPlanteRepository.findByCollecteId(100L)).thenReturn(List.of(vertu));

        AgentCollecteRecapitulatifResponse response = agentSubmissionService.getRecapitulatif(100L);

        assertThat(response).isNotNull();
        assertThat(response.getCollecteId()).isEqualTo(100L);
        assertThat(response.isPlanteAssociee()).isTrue();
        assertThat(response.getPlanteNomScientifique()).isEqualTo("Combretum micranthum");
        assertThat(response.getNomsVernaculaires()).containsExactly("Kinkéliba (Bambara)");
        assertThat(response.isConnaissanceRenseignee()).isTrue();
        assertThat(response.getNombreConnaissances()).isEqualTo(1);
        assertThat(response.isSourceRattachee()).isTrue();
        assertThat(response.getSourceNomComplet()).isEqualTo("Amadou Diarra");
        assertThat(response.isMediasPresents()).isTrue();
        assertThat(response.isPhotoPresente()).isTrue();
        assertThat(response.isAudioPresent()).isTrue();
        assertThat(response.isLocalisationRenseignee()).isTrue();
        assertThat(response.getAdresseFormatee()).isEqualTo("Finkolo Centre, Finkolo, Koutiala, Sikasso");

        assertThat(response.isConformePourSoumission()).isTrue();
        assertThat(response.getErreursBloquantes()).isEmpty();
        assertThat(response.getPointsDeVigilance()).isEmpty();
        assertThat(response.getProchaineEtape()).isEqualTo("SOUMISSION_AUTORISEE");
    }

    @Test
    @DisplayName("Récapitulatif : dossier incomplet avec erreurs bloquantes et points de vigilance")
    void getRecapitulatif_Succes_DossierIncomplet() {
        collecteBrouillon.setPhotoUrl(null);
        collecteBrouillon.setAudioUrl(null);
        collecteBrouillon.setSource(null);
        collecteBrouillon.setLocalisation(null);

        when(agentAuthService.getCurrentAgent()).thenReturn(agentConnecte);
        when(collecteRepository.findById(100L)).thenReturn(Optional.of(collecteBrouillon));
        when(vertuDeLaPlanteRepository.findByCollecteId(100L)).thenReturn(List.of());

        AgentCollecteRecapitulatifResponse response = agentSubmissionService.getRecapitulatif(100L);

        assertThat(response).isNotNull();
        assertThat(response.isPlanteAssociee()).isFalse();
        assertThat(response.isConnaissanceRenseignee()).isFalse();
        assertThat(response.isSourceRattachee()).isFalse();
        assertThat(response.isMediasPresents()).isFalse();
        assertThat(response.isLocalisationRenseignee()).isFalse();

        assertThat(response.isConformePourSoumission()).isFalse();
        assertThat(response.getErreursBloquantes()).hasSize(3); // Plante, Connaissance, Localisation
        assertThat(response.getPointsDeVigilance()).hasSize(3); // Source, Photo, Audio
        assertThat(response.getProchaineEtape()).isEqualTo("COMPLETER_DOSSIER");
    }

    @Test
    @DisplayName("Récapitulatif : rejet si la collecte appartient à un autre agent (403)")
    void getRecapitulatif_NonProprietaire_ForbiddenException() {
        AgentCollecte autreAgent = new AgentCollecte();
        autreAgent.setId(999L);
        collecteBrouillon.setAgentCollecte(autreAgent);

        when(agentAuthService.getCurrentAgent()).thenReturn(agentConnecte);
        when(collecteRepository.findById(100L)).thenReturn(Optional.of(collecteBrouillon));

        assertThatThrownBy(() -> agentSubmissionService.getRecapitulatif(100L))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("cette collecte ne vous appartient pas");
    }

    @Test
    @DisplayName("Récapitulatif : 404 si la collecte est inexistante")
    void getRecapitulatif_NonExistant_ResourceNotFoundException() {
        when(agentAuthService.getCurrentAgent()).thenReturn(agentConnecte);
        when(collecteRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> agentSubmissionService.getRecapitulatif(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Soumettre collecte : passage en SOUMISE, vertus en EN_ATTENTE et verrouillage")
    void soumettreCollecteVerifiee_Succes_Brouillon() {
        when(agentAuthService.getCurrentAgent()).thenReturn(agentConnecte);
        when(collecteRepository.findById(100L)).thenReturn(Optional.of(collecteBrouillon));
        when(vertuDeLaPlanteRepository.findByCollecteId(100L)).thenReturn(List.of(vertu));
        when(collecteRepository.save(any(Collecte.class))).thenReturn(collecteBrouillon);

        AgentSubmissionResultResponse response = agentSubmissionService.soumettreCollecteVerifiee(100L);

        assertThat(response).isNotNull();
        assertThat(response.getCollecteId()).isEqualTo(100L);
        assertThat(response.getStatut()).isEqualTo(StatutCollecte.SOUMISE);
        assertThat(response.getDateSoumission()).isNotNull();
        assertThat(response.getStatutConnaissances()).isEqualTo(StatutValidation.EN_ATTENTE);
        assertThat(response.isVerrouillee()).isTrue();

        assertThat(collecteBrouillon.getStatut()).isEqualTo(StatutCollecte.SOUMISE);
        assertThat(vertu.getStatut()).isEqualTo(StatutValidation.EN_ATTENTE);
        verify(collecteRepository).save(collecteBrouillon);
        verify(vertuDeLaPlanteRepository).save(vertu);
    }

    @Test
    @DisplayName("Soumettre collecte : nouvelle soumission autorisée depuis le statut REJETEE")
    void soumettreCollecteVerifiee_Succes_DepuisRejetee() {
        collecteBrouillon.setStatut(StatutCollecte.REJETEE);
        collecteBrouillon.setMotifRejet("Précisions manquantes sur le mode de préparation");

        when(agentAuthService.getCurrentAgent()).thenReturn(agentConnecte);
        when(collecteRepository.findById(100L)).thenReturn(Optional.of(collecteBrouillon));
        when(vertuDeLaPlanteRepository.findByCollecteId(100L)).thenReturn(List.of(vertu));
        when(collecteRepository.save(any(Collecte.class))).thenReturn(collecteBrouillon);

        AgentSubmissionResultResponse response = agentSubmissionService.soumettreCollecteVerifiee(100L);

        assertThat(response).isNotNull();
        assertThat(response.getStatut()).isEqualTo(StatutCollecte.SOUMISE);
        assertThat(collecteBrouillon.getMotifRejet()).isNull();
        verify(collecteRepository).save(collecteBrouillon);
    }

    @Test
    @DisplayName("Soumettre collecte : rejet si déjà soumise ou en cours de validation (400)")
    void soumettreCollecteVerifiee_DejaSoumise_BadRequestException() {
        collecteBrouillon.setStatut(StatutCollecte.SOUMISE);

        when(agentAuthService.getCurrentAgent()).thenReturn(agentConnecte);
        when(collecteRepository.findById(100L)).thenReturn(Optional.of(collecteBrouillon));

        assertThatThrownBy(() -> agentSubmissionService.soumettreCollecteVerifiee(100L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Impossible de soumettre cette collecte car son statut actuel est SOUMISE");

        verify(collecteRepository, never()).save(any());
        verify(vertuDeLaPlanteRepository, never()).save(any());
    }

    @Test
    @DisplayName("Soumettre collecte : rejet si le dossier comporte des erreurs bloquantes (400)")
    void soumettreCollecteVerifiee_DossierIncomplet_BadRequestException() {
        // Collecte sans plante ni localisation
        collecteBrouillon.setLocalisation(null);

        when(agentAuthService.getCurrentAgent()).thenReturn(agentConnecte);
        when(collecteRepository.findById(100L)).thenReturn(Optional.of(collecteBrouillon));
        when(vertuDeLaPlanteRepository.findByCollecteId(100L)).thenReturn(List.of());

        assertThatThrownBy(() -> agentSubmissionService.soumettreCollecteVerifiee(100L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Soumission impossible : le dossier de collecte est incomplet");

        verify(collecteRepository, never()).save(any());
    }

    @Test
    @DisplayName("Soumettre collecte : rejet si la collecte appartient à un autre agent (403)")
    void soumettreCollecteVerifiee_NonProprietaire_ForbiddenException() {
        AgentCollecte autreAgent = new AgentCollecte();
        autreAgent.setId(888L);
        collecteBrouillon.setAgentCollecte(autreAgent);

        when(agentAuthService.getCurrentAgent()).thenReturn(agentConnecte);
        when(collecteRepository.findById(100L)).thenReturn(Optional.of(collecteBrouillon));

        assertThatThrownBy(() -> agentSubmissionService.soumettreCollecteVerifiee(100L))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("cette collecte ne vous appartient pas");
    }
}
