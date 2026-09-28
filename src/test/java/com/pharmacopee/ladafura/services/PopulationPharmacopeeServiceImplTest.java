package com.pharmacopee.ladafura.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

import com.pharmacopee.ladafura.Models.CategorieProduit;
import com.pharmacopee.ladafura.Models.DisponibiliteProduit;
import com.pharmacopee.ladafura.Models.Localisation;
import com.pharmacopee.ladafura.Models.ModeRetrait;
import com.pharmacopee.ladafura.Models.Pharmacopee;
import com.pharmacopee.ladafura.Models.Produit;
import com.pharmacopee.ladafura.Models.Utilisateur;
import com.pharmacopee.ladafura.dto.population.pharmacopee.PopulationPharmacopeeDetailResponse;
import com.pharmacopee.ladafura.dto.population.pharmacopee.PopulationPharmacopeeModeRetraitDto;
import com.pharmacopee.ladafura.dto.population.pharmacopee.PopulationPharmacopeeProduitItemResponse;
import com.pharmacopee.ladafura.dto.population.pharmacopee.PopulationPharmacopeeSummaryResponse;
import com.pharmacopee.ladafura.enums.StatutAvis;
import com.pharmacopee.ladafura.enums.StatutPharmacopee;
import com.pharmacopee.ladafura.enums.StatutProduit;
import com.pharmacopee.ladafura.enums.TypeModeRetrait;
import com.pharmacopee.ladafura.exceptions.ResourceNotFoundException;
import com.pharmacopee.ladafura.mappers.PopulationPharmacopeeMapper;
import com.pharmacopee.ladafura.repository.AvisRepository;
import com.pharmacopee.ladafura.repository.DisponibiliteProduitRepository;
import com.pharmacopee.ladafura.repository.ModeRetraitRepository;
import com.pharmacopee.ladafura.repository.PharmacopeeRepository;
import com.pharmacopee.ladafura.services.impl.PopulationPharmacopeeServiceImpl;

@ExtendWith(MockitoExtension.class)
class PopulationPharmacopeeServiceImplTest {

    @Mock
    private PharmacopeeRepository pharmacopeeRepository;

    @Mock
    private ModeRetraitRepository modeRetraitRepository;

    @Mock
    private DisponibiliteProduitRepository disponibiliteProduitRepository;

    @Mock
    private AvisRepository avisRepository;

    @Spy
    private PopulationPharmacopeeMapper mapper = new PopulationPharmacopeeMapper();

    @InjectMocks
    private PopulationPharmacopeeServiceImpl pharmacopeeService;

    private Pharmacopee pharmacopeeValidee;
    private Localisation localisation;
    private Utilisateur utilisateur;
    private ModeRetrait modeLivraison;
    private ModeRetrait modePickup;
    private Produit produit;
    private DisponibiliteProduit disponibilite;

    @BeforeEach
    void setUp() {
        localisation = Localisation.builder()
                .id(1L)
                .region("Koulikoro")
                .cercle("Kati")
                .commune("Siby")
                .localite("Centre")
                .latitude(12.38)
                .longitude(-8.33)
                .build();

        utilisateur = new Utilisateur();
        utilisateur.setId(10L);
        utilisateur.setEmail("contact@pharmaciemande.ml");

        pharmacopeeValidee = Pharmacopee.builder()
                .id(3L)
                .nom("Pharmacie Traditionnelle Mandé")
                .description("Officine de référence pour la médecine traditionnelle")
                .telephone("+223 70 12 34 56")
                .statut(StatutPharmacopee.VALIDEE)
                .localisation(localisation)
                .utilisateur(utilisateur)
                .build();

        modeLivraison = ModeRetrait.builder()
                .id(1L)
                .type(TypeModeRetrait.LIVRAISON)
                .actif(true)
                .frais(1500.0)
                .pharmacopee(pharmacopeeValidee)
                .build();

        modePickup = ModeRetrait.builder()
                .id(2L)
                .type(TypeModeRetrait.PICKUP)
                .actif(true)
                .frais(0.0)
                .pharmacopee(pharmacopeeValidee)
                .build();

        CategorieProduit cat = CategorieProduit.builder().id(1L).nom("Tisanes").build();
        produit = Produit.builder()
                .id(5L)
                .nom("Tisane Kinkéliba")
                .forme("Sachet")
                .prix(2500.0)
                .statut(StatutProduit.VALIDE)
                .categorie(cat)
                .build();

        disponibilite = DisponibiliteProduit.builder()
                .id(10L)
                .produit(produit)
                .pharmacopee(pharmacopeeValidee)
                .disponible(true)
                .quantiteStock(15)
                .prix(2600.0)
                .build();
    }

    @Test
    @DisplayName("listerPharmacopees - Succès avec pagination et filtres géographiques")
    void listerPharmacopees_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Pharmacopee> page = new PageImpl<>(List.of(pharmacopeeValidee));

        when(pharmacopeeRepository.searchPharmacopees(StatutPharmacopee.VALIDEE, "mandé", "Koulikoro", "Kati", "Siby", pageable))
                .thenReturn(page);
        when(modeRetraitRepository.findByPharmacopeeId(3L))
                .thenReturn(List.of(modeLivraison, modePickup));
        when(disponibiliteProduitRepository.countProduitsValidesByPharmacopeeId(3L))
                .thenReturn(12L);
        when(avisRepository.findAverageNoteByPharmacopeeIdAndStatut(3L, StatutAvis.PUBLIE))
                .thenReturn(4.8);
        when(avisRepository.countByPharmacopeeIdAndStatut(3L, StatutAvis.PUBLIE))
                .thenReturn(20L);

        Page<PopulationPharmacopeeSummaryResponse> result =
                pharmacopeeService.listerPharmacopees("mandé", "Koulikoro", "Kati", "Siby", pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        PopulationPharmacopeeSummaryResponse item = result.getContent().get(0);
        assertThat(item.getId()).isEqualTo(3L);
        assertThat(item.getNom()).isEqualTo("Pharmacie Traditionnelle Mandé");
        assertThat(item.getRegion()).isEqualTo("Koulikoro");
        assertThat(item.isProposeLivraison()).isTrue();
        assertThat(item.isProposePickup()).isTrue();
        assertThat(item.getNombreProduits()).isEqualTo(12L);
        assertThat(item.getNoteMoyenne()).isEqualTo(4.8);
        assertThat(item.getNombreAvis()).isEqualTo(20L);

        verify(pharmacopeeRepository).searchPharmacopees(StatutPharmacopee.VALIDEE, "mandé", "Koulikoro", "Kati", "Siby", pageable);
    }

    @Test
    @DisplayName("getPharmacopeeDetail - Succès avec profil, contact et localisation")
    void getPharmacopeeDetail_Success() {
        when(pharmacopeeRepository.findByIdAndStatut(3L, StatutPharmacopee.VALIDEE))
                .thenReturn(Optional.of(pharmacopeeValidee));
        when(modeRetraitRepository.findByPharmacopeeId(3L))
                .thenReturn(List.of(modeLivraison, modePickup));
        when(disponibiliteProduitRepository.countProduitsValidesByPharmacopeeId(3L))
                .thenReturn(12L);
        when(avisRepository.findAverageNoteByPharmacopeeIdAndStatut(3L, StatutAvis.PUBLIE))
                .thenReturn(4.8);
        when(avisRepository.countByPharmacopeeIdAndStatut(3L, StatutAvis.PUBLIE))
                .thenReturn(20L);

        PopulationPharmacopeeDetailResponse response = pharmacopeeService.getPharmacopeeDetail(3L);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(3L);
        assertThat(response.getNom()).isEqualTo("Pharmacie Traditionnelle Mandé");
        assertThat(response.getEmail()).isEqualTo("contact@pharmaciemande.ml");
        assertThat(response.getTelephone()).isEqualTo("+223 70 12 34 56");
        assertThat(response.getLocalisation()).isNotNull();
        assertThat(response.getLocalisation().getCommune()).isEqualTo("Siby");
        assertThat(response.getModesRetrait()).hasSize(2);
        assertThat(response.getNombreProduits()).isEqualTo(12L);
    }

    @Test
    @DisplayName("getPharmacopeeDetail - Pharmacopée inexistante lance ResourceNotFoundException")
    void getPharmacopeeDetail_NotFound() {
        when(pharmacopeeRepository.findByIdAndStatut(99L, StatutPharmacopee.VALIDEE))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> pharmacopeeService.getPharmacopeeDetail(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("getProduitsByPharmacopee - Succès")
    void getProduitsByPharmacopee_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<DisponibiliteProduit> page = new PageImpl<>(List.of(disponibilite));

        when(pharmacopeeRepository.findByIdAndStatut(3L, StatutPharmacopee.VALIDEE))
                .thenReturn(Optional.of(pharmacopeeValidee));
        when(disponibiliteProduitRepository.findProduitsByPharmacopeeId(3L, true, pageable))
                .thenReturn(page);

        Page<PopulationPharmacopeeProduitItemResponse> result =
                pharmacopeeService.getProduitsByPharmacopee(3L, true, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        PopulationPharmacopeeProduitItemResponse item = result.getContent().get(0);
        assertThat(item.getProduitId()).isEqualTo(5L);
        assertThat(item.getNom()).isEqualTo("Tisane Kinkéliba");
        assertThat(item.getPrix()).isEqualTo(2600.0);
        assertThat(item.getQuantiteStock()).isEqualTo(15);
    }

    @Test
    @DisplayName("getModesRetraitByPharmacopee - Succès")
    void getModesRetraitByPharmacopee_Success() {
        when(pharmacopeeRepository.findByIdAndStatut(3L, StatutPharmacopee.VALIDEE))
                .thenReturn(Optional.of(pharmacopeeValidee));
        when(modeRetraitRepository.findByPharmacopeeId(3L))
                .thenReturn(List.of(modeLivraison, modePickup));

        List<PopulationPharmacopeeModeRetraitDto> modes = pharmacopeeService.getModesRetraitByPharmacopee(3L);

        assertThat(modes).hasSize(2);
        assertThat(modes.get(0).getType()).isEqualTo("LIVRAISON");
        assertThat(modes.get(1).getType()).isEqualTo("PICKUP");
    }
}
