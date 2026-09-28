package com.pharmacopee.ladafura.services;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import com.pharmacopee.ladafura.Models.Localisation;
import com.pharmacopee.ladafura.Models.ModeRetrait;
import com.pharmacopee.ladafura.Models.Pharmacopee;
import com.pharmacopee.ladafura.dto.population.retrait.PopulationEstimationRetraitRequest;
import com.pharmacopee.ladafura.dto.population.retrait.PopulationEstimationRetraitResponse;
import com.pharmacopee.ladafura.dto.population.retrait.PopulationPharmacopeeRetraitOptionsResponse;
import com.pharmacopee.ladafura.enums.StatutPharmacopee;
import com.pharmacopee.ladafura.enums.TypeModeRetrait;
import com.pharmacopee.ladafura.exceptions.BadRequestException;
import com.pharmacopee.ladafura.exceptions.ResourceNotFoundException;
import com.pharmacopee.ladafura.mappers.PopulationRetraitMapper;
import com.pharmacopee.ladafura.repository.ModeRetraitRepository;
import com.pharmacopee.ladafura.repository.PharmacopeeRepository;
import com.pharmacopee.ladafura.services.impl.PopulationRetraitServiceImpl;

@ExtendWith(MockitoExtension.class)
class PopulationRetraitServiceImplTest {

    @Mock
    private PharmacopeeRepository pharmacopeeRepository;

    @Mock
    private ModeRetraitRepository modeRetraitRepository;

    @Spy
    private PopulationRetraitMapper mapper = new PopulationRetraitMapper();

    @InjectMocks
    private PopulationRetraitServiceImpl retraitService;

    private Pharmacopee pharmacopee;
    private Localisation localisation;
    private ModeRetrait modeLivraison;
    private ModeRetrait modePickup;

    @BeforeEach
    void setUp() {
        localisation = Localisation.builder()
                .id(1L)
                .region("Koulikoro")
                .cercle("Kati")
                .commune("Siby")
                .localite("Centre Commercial")
                .build();

        pharmacopee = Pharmacopee.builder()
                .id(1L)
                .nom("Pharmacie Mandé")
                .telephone("+223 70 11 22 33")
                .statut(StatutPharmacopee.VALIDEE)
                .localisation(localisation)
                .build();

        modeLivraison = ModeRetrait.builder()
                .id(10L)
                .type(TypeModeRetrait.LIVRAISON)
                .actif(true)
                .frais(1500.0)
                .pharmacopee(pharmacopee)
                .build();

        modePickup = ModeRetrait.builder()
                .id(11L)
                .type(TypeModeRetrait.PICKUP)
                .actif(true)
                .frais(0.0)
                .pharmacopee(pharmacopee)
                .build();
    }

    @Test
    @DisplayName("getOptionsRetraitPharmacopee - Récupère les options de mise à disposition")
    void getOptionsRetraitPharmacopee_Success() {
        when(pharmacopeeRepository.findById(1L)).thenReturn(Optional.of(pharmacopee));
        when(modeRetraitRepository.findByPharmacopeeId(1L)).thenReturn(List.of(modeLivraison, modePickup));

        PopulationPharmacopeeRetraitOptionsResponse response = retraitService.getOptionsRetraitPharmacopee(1L);

        assertThat(response).isNotNull();
        assertThat(response.getPharmacopeeId()).isEqualTo(1L);
        assertThat(response.getNomPharmacopee()).isEqualTo("Pharmacie Mandé");
        assertThat(response.getProposeLivraison()).isTrue();
        assertThat(response.getFraisLivraison()).isEqualTo(1500.0);
        assertThat(response.getProposePickup()).isTrue();
        assertThat(response.getOptions()).hasSize(2);
    }

    @Test
    @DisplayName("getOptionsRetraitPharmacopee - Pharmacopée introuvable -> ResourceNotFoundException")
    void getOptionsRetraitPharmacopee_NotFound() {
        when(pharmacopeeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> retraitService.getOptionsRetraitPharmacopee(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("getOptionsRetraitPharmacopee - Pharmacopée non validée -> BadRequestException")
    void getOptionsRetraitPharmacopee_NotValidee() {
        pharmacopee.setStatut(StatutPharmacopee.EN_ATTENTE);
        when(pharmacopeeRepository.findById(1L)).thenReturn(Optional.of(pharmacopee));

        assertThatThrownBy(() -> retraitService.getOptionsRetraitPharmacopee(1L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("pas agréée");
    }

    @Test
    @DisplayName("estimerOptionRetrait - Estimation réussie pour Pickup (gratuit)")
    void estimerOptionRetrait_Pickup_Success() {
        PopulationEstimationRetraitRequest request = PopulationEstimationRetraitRequest.builder()
                .pharmacopeeId(1L)
                .type(TypeModeRetrait.PICKUP)
                .build();

        when(pharmacopeeRepository.findById(1L)).thenReturn(Optional.of(pharmacopee));
        when(modeRetraitRepository.findByPharmacopeeIdAndType(1L, TypeModeRetrait.PICKUP))
                .thenReturn(Optional.of(modePickup));

        PopulationEstimationRetraitResponse response = retraitService.estimerOptionRetrait(request);

        assertThat(response).isNotNull();
        assertThat(response.getEligible()).isTrue();
        assertThat(response.getFrais()).isEqualTo(0.0);
        assertThat(response.getGratuit()).isTrue();
        assertThat(response.getAdresseRetrait()).contains("Centre Commercial");
        assertThat(response.getMessage()).contains("gratuit");
    }

    @Test
    @DisplayName("estimerOptionRetrait - Estimation réussie pour Livraison avec frais")
    void estimerOptionRetrait_Livraison_Success() {
        PopulationEstimationRetraitRequest request = PopulationEstimationRetraitRequest.builder()
                .pharmacopeeId(1L)
                .type(TypeModeRetrait.LIVRAISON)
                .build();

        when(pharmacopeeRepository.findById(1L)).thenReturn(Optional.of(pharmacopee));
        when(modeRetraitRepository.findByPharmacopeeIdAndType(1L, TypeModeRetrait.LIVRAISON))
                .thenReturn(Optional.of(modeLivraison));

        PopulationEstimationRetraitResponse response = retraitService.estimerOptionRetrait(request);

        assertThat(response).isNotNull();
        assertThat(response.getEligible()).isTrue();
        assertThat(response.getFrais()).isEqualTo(1500.0);
        assertThat(response.getGratuit()).isFalse();
        assertThat(response.getMessage()).contains("1500.0 FCFA");
    }

    @Test
    @DisplayName("estimerOptionRetrait - Mode inactif -> Non éligible")
    void estimerOptionRetrait_InactiveMode() {
        modeLivraison.setActif(false);

        PopulationEstimationRetraitRequest request = PopulationEstimationRetraitRequest.builder()
                .pharmacopeeId(1L)
                .type(TypeModeRetrait.LIVRAISON)
                .build();

        when(pharmacopeeRepository.findById(1L)).thenReturn(Optional.of(pharmacopee));
        when(modeRetraitRepository.findByPharmacopeeIdAndType(1L, TypeModeRetrait.LIVRAISON))
                .thenReturn(Optional.of(modeLivraison));

        PopulationEstimationRetraitResponse response = retraitService.estimerOptionRetrait(request);

        assertThat(response).isNotNull();
        assertThat(response.getEligible()).isFalse();
        assertThat(response.getMessage()).contains("pas proposée");
    }
}
