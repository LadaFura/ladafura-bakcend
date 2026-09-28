package com.pharmacopee.ladafura.services;

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
import static org.mockito.Mockito.when;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.pharmacopee.ladafura.Models.DisponibiliteProduit;
import com.pharmacopee.ladafura.Models.Localisation;
import com.pharmacopee.ladafura.Models.ModeRetrait;
import com.pharmacopee.ladafura.Models.Pharmacopee;
import com.pharmacopee.ladafura.Models.Produit;
import com.pharmacopee.ladafura.Models.Utilisateur;
import com.pharmacopee.ladafura.dto.population.cartographie.PopulationCarteDetailPharmacopeeResponse;
import com.pharmacopee.ladafura.dto.population.cartographie.PopulationCartePharmacopeeItem;
import com.pharmacopee.ladafura.dto.population.cartographie.PopulationCarteProduitItem;
import com.pharmacopee.ladafura.enums.StatutAvis;
import com.pharmacopee.ladafura.enums.StatutPharmacopee;
import com.pharmacopee.ladafura.enums.StatutProduit;
import com.pharmacopee.ladafura.enums.TypeModeRetrait;
import com.pharmacopee.ladafura.exceptions.ResourceNotFoundException;
import com.pharmacopee.ladafura.mappers.PopulationCarteMapper;
import com.pharmacopee.ladafura.repository.AvisRepository;
import com.pharmacopee.ladafura.repository.DisponibiliteProduitRepository;
import com.pharmacopee.ladafura.repository.ModeRetraitRepository;
import com.pharmacopee.ladafura.repository.PharmacopeeRepository;
import com.pharmacopee.ladafura.repository.ProduitRepository;
import com.pharmacopee.ladafura.services.impl.PopulationCarteServiceImpl;

@ExtendWith(MockitoExtension.class)
class PopulationCarteServiceImplTest {

    @Mock
    private PharmacopeeRepository pharmacopeeRepository;

    @Mock
    private DisponibiliteProduitRepository disponibiliteProduitRepository;

    @Mock
    private ProduitRepository produitRepository;

    @Mock
    private ModeRetraitRepository modeRetraitRepository;

    @Mock
    private AvisRepository avisRepository;

    @Spy
    private PopulationCarteMapper mapper = new PopulationCarteMapper();

    @InjectMocks
    private PopulationCarteServiceImpl carteService;

    private Pharmacopee pharmacopee1;
    private Pharmacopee pharmacopee2;
    private Localisation loc1;
    private Localisation loc2;
    private ModeRetrait modeLivraison;
    private Produit produit;
    private DisponibiliteProduit disp1;

    @BeforeEach
    void setUp() {
        loc1 = Localisation.builder()
                .id(1L)
                .region("Koulikoro")
                .cercle("Kati")
                .commune("Siby")
                .localite("Centre Siby")
                .latitude(12.3847)
                .longitude(-8.3341)
                .build();

        loc2 = Localisation.builder()
                .id(2L)
                .region("Bamako")
                .cercle("Bamako")
                .commune("Commune III")
                .localite("Badalabougou")
                .latitude(12.6392)
                .longitude(-8.0029)
                .build();

        Utilisateur u1 = new Utilisateur();
        u1.setId(10L);
        u1.setEmail("mande@ladafura.ml");

        pharmacopee1 = Pharmacopee.builder()
                .id(1L)
                .nom("Pharmacie Mandé")
                .description("Officine Siby")
                .telephone("+223 70 11 22 33")
                .statut(StatutPharmacopee.VALIDEE)
                .localisation(loc1)
                .utilisateur(u1)
                .build();

        pharmacopee2 = Pharmacopee.builder()
                .id(2L)
                .nom("Pharmacie Bamako Centre")
                .description("Officine Bamako")
                .telephone("+223 70 44 55 66")
                .statut(StatutPharmacopee.VALIDEE)
                .localisation(loc2)
                .build();

        modeLivraison = ModeRetrait.builder()
                .id(1L)
                .type(TypeModeRetrait.LIVRAISON)
                .actif(true)
                .frais(1500.0)
                .pharmacopee(pharmacopee1)
                .build();

        produit = Produit.builder()
                .id(5L)
                .nom("Tisane Kinkéliba")
                .forme("Sachet")
                .prix(2500.0)
                .statut(StatutProduit.VALIDE)
                .build();

        disp1 = DisponibiliteProduit.builder()
                .id(10L)
                .produit(produit)
                .pharmacopee(pharmacopee1)
                .disponible(true)
                .quantiteStock(12)
                .prix(2500.0)
                .build();
    }

    @Test
    @DisplayName("getPharmacopeesSurCarte - Succès avec calcul de distance et tri par proximité")
    void getPharmacopeesSurCarte_Success() {
        Page<Pharmacopee> page = new PageImpl<>(List.of(pharmacopee1, pharmacopee2));

        when(pharmacopeeRepository.searchPharmacopees(
                StatutPharmacopee.VALIDEE, null, null, null, null, Pageable.unpaged()))
                .thenReturn(page);
        when(modeRetraitRepository.findByPharmacopeeId(any())).thenReturn(List.of(modeLivraison));
        when(disponibiliteProduitRepository.countProduitsValidesByPharmacopeeId(any())).thenReturn(5L);
        when(avisRepository.findAverageNoteByPharmacopeeIdAndStatut(any(), eq(StatutAvis.PUBLIE))).thenReturn(4.5);
        when(avisRepository.countByPharmacopeeIdAndStatut(any(), eq(StatutAvis.PUBLIE))).thenReturn(10L);

        // Position utilisateur à Bamako (proche de pharmacopee2)
        List<PopulationCartePharmacopeeItem> items = carteService.getPharmacopeesSurCarte(
                null, null, null, null, 12.6390, -8.0028, 100.0);

        assertThat(items).hasSize(2);
        // Le plus proche en premier (pharmacie 2 à Bamako)
        assertThat(items.get(0).getPharmacopeeId()).isEqualTo(2L);
        assertThat(items.get(0).getDistanceKm()).isLessThan(1.0);
        assertThat(items.get(1).getPharmacopeeId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("localiserProduitSurCarte - Succès par produitId")
    void localiserProduitSurCarte_ByProduitId() {
        when(disponibiliteProduitRepository.findOffresValideesByProduitId(5L))
                .thenReturn(List.of(disp1));
        when(modeRetraitRepository.findByPharmacopeeId(1L))
                .thenReturn(List.of(modeLivraison));

        List<PopulationCarteProduitItem> items = carteService.localiserProduitSurCarte(
                5L, null, 12.38, -8.33, 50.0, true);

        assertThat(items).hasSize(1);
        assertThat(items.get(0).getNomPharmacopee()).isEqualTo("Pharmacie Mandé");
        assertThat(items.get(0).getProduitId()).isEqualTo(5L);
        assertThat(items.get(0).getNomProduit()).isEqualTo("Tisane Kinkéliba");
        assertThat(items.get(0).getDisponible()).isTrue();
        assertThat(items.get(0).getDistanceKm()).isNotNull();
    }

    @Test
    @DisplayName("localiserProduitSurCarte - Succès par recherche mot-clé")
    void localiserProduitSurCarte_ByQuery() {
        when(produitRepository.searchTopByStatutAndKeyword(StatutProduit.VALIDE, "tisane", PageRequest.of(0, 5)))
                .thenReturn(List.of(produit));
        when(disponibiliteProduitRepository.findOffresValideesByProduitId(5L))
                .thenReturn(List.of(disp1));
        when(modeRetraitRepository.findByPharmacopeeId(1L))
                .thenReturn(List.of(modeLivraison));

        List<PopulationCarteProduitItem> items = carteService.localiserProduitSurCarte(
                null, "tisane", 12.38, -8.33, 50.0, true);

        assertThat(items).hasSize(1);
        assertThat(items.get(0).getNomPharmacopee()).isEqualTo("Pharmacie Mandé");
    }

    @Test
    @DisplayName("getPharmacopeeCarteDetail - Succès avec calcul de distance")
    void getPharmacopeeCarteDetail_Success() {
        when(pharmacopeeRepository.findByIdAndStatut(1L, StatutPharmacopee.VALIDEE))
                .thenReturn(Optional.of(pharmacopee1));
        when(modeRetraitRepository.findByPharmacopeeId(1L))
                .thenReturn(List.of(modeLivraison));
        when(disponibiliteProduitRepository.countProduitsValidesByPharmacopeeId(1L))
                .thenReturn(15L);
        when(avisRepository.findAverageNoteByPharmacopeeIdAndStatut(1L, StatutAvis.PUBLIE))
                .thenReturn(4.8);
        when(avisRepository.countByPharmacopeeIdAndStatut(1L, StatutAvis.PUBLIE))
                .thenReturn(20L);

        PopulationCarteDetailPharmacopeeResponse response = carteService.getPharmacopeeCarteDetail(
                1L, 12.3800, -8.3300);

        assertThat(response).isNotNull();
        assertThat(response.getPharmacopeeId()).isEqualTo(1L);
        assertThat(response.getNom()).isEqualTo("Pharmacie Mandé");
        assertThat(response.getEmail()).isEqualTo("mande@ladafura.ml");
        assertThat(response.getDistanceKm()).isNotNull();
        assertThat(response.getLocalisation()).isNotNull();
        assertThat(response.getModesRetrait()).hasSize(1);
    }

    @Test
    @DisplayName("getPharmacopeeCarteDetail - Pharmacopée non trouvée lance ResourceNotFoundException")
    void getPharmacopeeCarteDetail_NotFound() {
        when(pharmacopeeRepository.findByIdAndStatut(99L, StatutPharmacopee.VALIDEE))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> carteService.getPharmacopeeCarteDetail(99L, null, null))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
