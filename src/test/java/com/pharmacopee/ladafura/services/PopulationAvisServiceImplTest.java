package com.pharmacopee.ladafura.services;

import java.time.LocalDateTime;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.pharmacopee.ladafura.Models.Avis;
import com.pharmacopee.ladafura.Models.Pharmacopee;
import com.pharmacopee.ladafura.Models.Utilisateur;
import com.pharmacopee.ladafura.dto.population.avis.PopulationAvisResponse;
import com.pharmacopee.ladafura.dto.population.avis.PopulationCreateAvisRequest;
import com.pharmacopee.ladafura.dto.population.avis.PopulationEligibiliteAvisResponse;
import com.pharmacopee.ladafura.dto.population.avis.PopulationUpdateAvisRequest;
import com.pharmacopee.ladafura.enums.Role;
import com.pharmacopee.ladafura.enums.StatutAvis;
import com.pharmacopee.ladafura.enums.StatutCommande;
import com.pharmacopee.ladafura.enums.StatutPharmacopee;
import com.pharmacopee.ladafura.enums.StatutUtilisateur;
import com.pharmacopee.ladafura.exceptions.BadRequestException;
import com.pharmacopee.ladafura.mappers.PopulationAvisMapper;
import com.pharmacopee.ladafura.repository.AvisRepository;
import com.pharmacopee.ladafura.repository.CommandeRepository;
import com.pharmacopee.ladafura.repository.PharmacopeeRepository;
import com.pharmacopee.ladafura.services.impl.PopulationAvisServiceImpl;
import com.pharmacopee.ladafura.services.interfaces.IPopulationAuthService;

@ExtendWith(MockitoExtension.class)
class PopulationAvisServiceImplTest {

    @Mock
    private IPopulationAuthService populationAuthService;

    @Mock
    private AvisRepository avisRepository;

    @Mock
    private PharmacopeeRepository pharmacopeeRepository;

    @Mock
    private CommandeRepository commandeRepository;

    @Spy
    private PopulationAvisMapper mapper = new PopulationAvisMapper();

    @InjectMocks
    private PopulationAvisServiceImpl avisService;

    private Utilisateur currentUser;
    private Pharmacopee pharmacopee;
    private Avis avis;

    @BeforeEach
    void setUp() {
        currentUser = new Utilisateur();
        currentUser.setId(10L);
        currentUser.setEmail("client@ladafura.ml");
        currentUser.setNom("Coulibaly");
        currentUser.setPrenom("Awa");
        currentUser.setRole(Role.POPULATION);
        currentUser.setStatut(StatutUtilisateur.ACTIF);

        pharmacopee = Pharmacopee.builder()
                .id(5L)
                .nom("Danaya Tradithérapie")
                .statut(StatutPharmacopee.VALIDEE)
                .build();

        avis = Avis.builder()
                .id(1L)
                .pharmacopee(pharmacopee)
                .utilisateur(currentUser)
                .note(5)
                .commentaire("Excellente expérience et accueil chaleureux.")
                .statut(StatutAvis.EN_ATTENTE)
                .dateAvis(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("verifierEligibiliteAvis - Éligible et pas encore d'avis")
    void verifierEligibiliteAvis_Eligible() {
        when(populationAuthService.getCurrentPopulationUser()).thenReturn(currentUser);
        when(pharmacopeeRepository.findById(5L)).thenReturn(Optional.of(pharmacopee));
        when(commandeRepository.existsByUtilisateurIdAndPharmacopeeIdAndStatutIn(
                eq(10L), eq(5L), any())).thenReturn(true);
        when(avisRepository.findByUtilisateurIdAndPharmacopeeId(10L, 5L)).thenReturn(Optional.empty());

        PopulationEligibiliteAvisResponse response = avisService.verifierEligibiliteAvis(5L);

        assertThat(response).isNotNull();
        assertThat(response.getEligible()).isTrue();
        assertThat(response.getDejaEvalue()).isFalse();
        assertThat(response.getMessage()).contains("éligible");
    }

    @Test
    @DisplayName("verifierEligibiliteAvis - Non éligible (jamais commandé auprès de cette pharmacopée)")
    void verifierEligibiliteAvis_NotEligible() {
        when(populationAuthService.getCurrentPopulationUser()).thenReturn(currentUser);
        when(pharmacopeeRepository.findById(5L)).thenReturn(Optional.of(pharmacopee));
        when(commandeRepository.existsByUtilisateurIdAndPharmacopeeIdAndStatutIn(
                eq(10L), eq(5L), any())).thenReturn(false);
        when(avisRepository.findByUtilisateurIdAndPharmacopeeId(10L, 5L)).thenReturn(Optional.empty());

        PopulationEligibiliteAvisResponse response = avisService.verifierEligibiliteAvis(5L);

        assertThat(response).isNotNull();
        assertThat(response.getEligible()).isFalse();
        assertThat(response.getMessage()).contains("Vous devez avoir commandé auprès de cette pharmacopée");
    }

    @Test
    @DisplayName("creerAvis - Création avec succès au statut EN_ATTENTE")
    void creerAvis_Success() {
        PopulationCreateAvisRequest request = PopulationCreateAvisRequest.builder()
                .pharmacopeeId(5L)
                .note(5)
                .commentaire("Très bon accueil et service")
                .build();

        when(populationAuthService.getCurrentPopulationUser()).thenReturn(currentUser);
        when(pharmacopeeRepository.findById(5L)).thenReturn(Optional.of(pharmacopee));
        when(commandeRepository.existsByUtilisateurIdAndPharmacopeeIdAndStatutIn(
                eq(10L), eq(5L), any())).thenReturn(true);
        when(avisRepository.existsByUtilisateurIdAndPharmacopeeId(10L, 5L)).thenReturn(false);
        when(avisRepository.save(any(Avis.class))).thenAnswer(invocation -> {
            Avis a = invocation.getArgument(0);
            a.setId(100L);
            return a;
        });

        PopulationAvisResponse response = avisService.creerAvis(request);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(100L);
        assertThat(response.getNote()).isEqualTo(5);
        assertThat(response.getStatut()).isEqualTo(StatutAvis.EN_ATTENTE);
        assertThat(response.getStatutLibelle()).contains("modération");
        verify(avisRepository).save(any(Avis.class));
    }

    @Test
    @DisplayName("creerAvis - Non éligible lance BadRequestException")
    void creerAvis_NotEligible() {
        PopulationCreateAvisRequest request = PopulationCreateAvisRequest.builder()
                .pharmacopeeId(5L)
                .note(5)
                .commentaire("Super")
                .build();

        when(populationAuthService.getCurrentPopulationUser()).thenReturn(currentUser);
        when(pharmacopeeRepository.findById(5L)).thenReturn(Optional.of(pharmacopee));
        when(commandeRepository.existsByUtilisateurIdAndPharmacopeeIdAndStatutIn(
                eq(10L), eq(5L), any())).thenReturn(false);

        assertThatThrownBy(() -> avisService.creerAvis(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Vous devez avoir passé au moins une commande livrée auprès de cette pharmacopée");
    }

    @Test
    @DisplayName("creerAvis - Avis déjà existant lance BadRequestException")
    void creerAvis_AlreadyExists() {
        PopulationCreateAvisRequest request = PopulationCreateAvisRequest.builder()
                .pharmacopeeId(5L)
                .note(5)
                .build();

        when(populationAuthService.getCurrentPopulationUser()).thenReturn(currentUser);
        when(pharmacopeeRepository.findById(5L)).thenReturn(Optional.of(pharmacopee));
        when(commandeRepository.existsByUtilisateurIdAndPharmacopeeIdAndStatutIn(
                eq(10L), eq(5L), any())).thenReturn(true);
        when(avisRepository.existsByUtilisateurIdAndPharmacopeeId(10L, 5L)).thenReturn(true);

        assertThatThrownBy(() -> avisService.creerAvis(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("déjà déposé un avis");
    }

    @Test
    @DisplayName("modifierAvis - Modifie note et commentaire et remet statut à EN_ATTENTE")
    void modifierAvis_Success() {
        avis.setStatut(StatutAvis.PUBLIE);
        PopulationUpdateAvisRequest request = PopulationUpdateAvisRequest.builder()
                .note(4)
                .commentaire("Mise à jour du commentaire")
                .build();

        when(populationAuthService.getCurrentPopulationUser()).thenReturn(currentUser);
        when(avisRepository.findByIdAndUtilisateurId(1L, 10L)).thenReturn(Optional.of(avis));
        when(avisRepository.save(any(Avis.class))).thenReturn(avis);

        PopulationAvisResponse response = avisService.modifierAvis(1L, request);

        assertThat(response).isNotNull();
        assertThat(avis.getNote()).isEqualTo(4);
        assertThat(avis.getCommentaire()).isEqualTo("Mise à jour du commentaire");
        assertThat(avis.getStatut()).isEqualTo(StatutAvis.EN_ATTENTE);
        verify(avisRepository).save(avis);
    }

    @Test
    @DisplayName("supprimerAvis - Supprime l'avis appartenant à l'utilisateur")
    void supprimerAvis_Success() {
        when(populationAuthService.getCurrentPopulationUser()).thenReturn(currentUser);
        when(avisRepository.findByIdAndUtilisateurId(1L, 10L)).thenReturn(Optional.of(avis));

        avisService.supprimerAvis(1L);

        verify(avisRepository).delete(avis);
    }

    @Test
    @DisplayName("getMesAvis - Récupère la liste paginée de ses avis")
    void getMesAvis_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        when(populationAuthService.getCurrentPopulationUser()).thenReturn(currentUser);
        when(avisRepository.findByUtilisateurId(10L, pageable))
                .thenReturn(new PageImpl<>(List.of(avis)));

        Page<PopulationAvisResponse> page = avisService.getMesAvis(null, pageable);

        assertThat(page).isNotNull();
        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent().get(0).getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("getAvisDetail - Récupère le détail d'un avis")
    void getAvisDetail_Success() {
        when(populationAuthService.getCurrentPopulationUser()).thenReturn(currentUser);
        when(avisRepository.findByIdAndUtilisateurId(1L, 10L)).thenReturn(Optional.of(avis));

        PopulationAvisResponse response = avisService.getAvisDetail(1L);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getNomPharmacopee()).isEqualTo("Danaya Tradithérapie");
    }
}
