package com.pharmacopee.ladafura.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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

import com.pharmacopee.ladafura.Models.Collecte;
import com.pharmacopee.ladafura.Models.EtudeScientifique;
import com.pharmacopee.ladafura.Models.Localisation;
import com.pharmacopee.ladafura.Models.Maladie;
import com.pharmacopee.ladafura.Models.NomPlante;
import com.pharmacopee.ladafura.Models.Plante;
import com.pharmacopee.ladafura.Models.VertuDeLaPlante;
import com.pharmacopee.ladafura.dto.population.plante.PopulationConnaissanceTraditionnelleDto;
import com.pharmacopee.ladafura.dto.population.plante.PopulationEtudeScientifiqueDto;
import com.pharmacopee.ladafura.dto.population.plante.PopulationPlanteDetailResponse;
import com.pharmacopee.ladafura.dto.population.plante.PopulationPlanteSummaryResponse;
import com.pharmacopee.ladafura.enums.StatutPlante;
import com.pharmacopee.ladafura.enums.StatutValidation;
import com.pharmacopee.ladafura.exceptions.ResourceNotFoundException;
import com.pharmacopee.ladafura.mappers.PopulationPlanteMapper;
import com.pharmacopee.ladafura.repository.EtudeScientifiqueRepository;
import com.pharmacopee.ladafura.repository.NomPlanteRepository;
import com.pharmacopee.ladafura.repository.PlanteRepository;
import com.pharmacopee.ladafura.repository.VertuDeLaPlanteRepository;
import com.pharmacopee.ladafura.services.impl.PopulationPlanteServiceImpl;

@ExtendWith(MockitoExtension.class)
class PopulationPlanteServiceImplTest {

    @Mock
    private PlanteRepository planteRepository;

    @Mock
    private VertuDeLaPlanteRepository vertuDeLaPlanteRepository;

    @Mock
    private EtudeScientifiqueRepository etudeScientifiqueRepository;

    @Mock
    private NomPlanteRepository nomPlanteRepository;

    @Spy
    private PopulationPlanteMapper mapper = new PopulationPlanteMapper();

    @InjectMocks
    private PopulationPlanteServiceImpl planteService;

    private Plante planteValide;
    private NomPlante nomVernaculaire;
    private Maladie maladiePaludisme;
    private VertuDeLaPlante vertuValidee;
    private EtudeScientifique etude;
    private Collecte collecte;
    private Localisation localisation;

    @BeforeEach
    void setUp() {
        nomVernaculaire = NomPlante.builder()
                .id(1L)
                .nom("Kinkéliba")
                .langue("Bambara")
                .pays("Mali")
                .build();

        maladiePaludisme = Maladie.builder()
                .id(10L)
                .nom("Paludisme")
                .description("Fièvre et céphalées")
                .build();

        planteValide = Plante.builder()
                .id(1L)
                .nomScientifique("Combretum micranthum")
                .description("Arbuste sahélo-soudanais très répandu en Afrique de l'Ouest")
                .photoUrl("https://ladafura.ml/uploads/kinkeliba.jpg")
                .statut(StatutPlante.VALIDE)
                .nomsPlante(new ArrayList<>(List.of(nomVernaculaire)))
                .maladies(Set.of(maladiePaludisme))
                .build();

        localisation = Localisation.builder()
                .id(5L)
                .region("Koulikoro")
                .cercle("Kati")
                .commune("Siby")
                .localite("Djissoumala")
                .latitude(12.38)
                .longitude(-8.33)
                .build();

        collecte = Collecte.builder()
                .id(100L)
                .description("Collecte en zone rurale")
                .photoUrl("https://ladafura.ml/uploads/collectes/photo1.jpg")
                .audioUrl("https://ladafura.ml/uploads/collectes/audio1.mp3")
                .localisation(localisation)
                .build();

        vertuValidee = VertuDeLaPlante.builder()
                .id(20L)
                .usageRapporte("Traitement adjuvant des fièvres et troubles bilieux")
                .partieUtilisee("Feuilles")
                .preparation("Décoction aqueuse")
                .precaution("Aucune toxicité majeure rapportée aux doses usuelles")
                .description("Usage ancestral bien documenté")
                .statut(StatutValidation.VALIDE)
                .collecte(collecte)
                .plante(planteValide)
                .build();

        etude = EtudeScientifique.builder()
                .id(30L)
                .titre("Étude clinique pilote sur les effets hypotenseurs et antioxydants")
                .auteurs("Sanogo R., Diallo D.")
                .annee(2021)
                .reference("Mali Médical, 2021; 36(2): 45-51")
                .resume("Les extraits hydro-alcooliques ont montré une réduction significative...")
                .documentUrl("https://ladafura.ml/etudes/combretum2021.pdf")
                .plante(planteValide)
                .build();
    }

    @Test
    @DisplayName("listerPlantes - Succès avec pagination et compteurs")
    void listerPlantes_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Plante> page = new PageImpl<>(List.of(planteValide));

        when(planteRepository.findByStatut(StatutPlante.VALIDE, pageable)).thenReturn(page);
        when(vertuDeLaPlanteRepository.findByPlanteIdAndStatut(1L, StatutValidation.VALIDE)).thenReturn(List.of(vertuValidee));
        when(etudeScientifiqueRepository.findByPlanteId(1L)).thenReturn(List.of(etude));

        Page<PopulationPlanteSummaryResponse> result = planteService.listerPlantes(pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        PopulationPlanteSummaryResponse item = result.getContent().get(0);
        assertThat(item.getId()).isEqualTo(1L);
        assertThat(item.getNomScientifique()).isEqualTo("Combretum micranthum");
        assertThat(item.getNomsVernaculaires()).contains("Kinkéliba");
        assertThat(item.getMaladies()).contains("Paludisme");
        assertThat(item.getNombreConnaissances()).isEqualTo(1);
        assertThat(item.getNombreEtudesScientifiques()).isEqualTo(1);

        verify(planteRepository).findByStatut(StatutPlante.VALIDE, pageable);
    }

    @Test
    @DisplayName("getPlanteDetail - Succès avec distinction claire des savoirs, études, médias et localités")
    void getPlanteDetail_Success() {
        when(planteRepository.findByIdAndStatut(1L, StatutPlante.VALIDE)).thenReturn(Optional.of(planteValide));
        when(vertuDeLaPlanteRepository.findByPlanteIdAndStatut(1L, StatutValidation.VALIDE)).thenReturn(List.of(vertuValidee));
        when(etudeScientifiqueRepository.findByPlanteId(1L)).thenReturn(List.of(etude));
        when(nomPlanteRepository.findByPlanteId(1L)).thenReturn(List.of(nomVernaculaire));

        PopulationPlanteDetailResponse response = planteService.getPlanteDetail(1L);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getNomScientifique()).isEqualTo("Combretum micranthum");
        assertThat(response.getAvertissementMedical()).isNotBlank();
        assertThat(response.getAvertissementMedical()).contains("ne constituent en aucun cas une preuve scientifique");

        // 1. Noms vernaculaires
        assertThat(response.getNomsVernaculaires()).hasSize(1);
        assertThat(response.getNomsVernaculaires().get(0).getNom()).isEqualTo("Kinkéliba");
        assertThat(response.getNomsVernaculaires().get(0).getLangue()).isEqualTo("Bambara");

        // 2. Connaissances traditionnelles
        assertThat(response.getConnaissancesTraditionnelles()).hasSize(1);
        assertThat(response.getConnaissancesTraditionnelles().get(0).getUsageRapporte())
                .isEqualTo("Traitement adjuvant des fièvres et troubles bilieux");

        // 3. Études scientifiques
        assertThat(response.getEtudesScientifiques()).hasSize(1);
        assertThat(response.getEtudesScientifiques().get(0).getTitre())
                .isEqualTo("Étude clinique pilote sur les effets hypotenseurs et antioxydants");

        // 4. Maladies
        assertThat(response.getMaladiesAssociees()).hasSize(1);
        assertThat(response.getMaladiesAssociees().get(0).getNom()).isEqualTo("Paludisme");

        // 5. Médias (Photo officielle + Photo terrain + Audio terrain)
        assertThat(response.getMedias()).hasSize(3);
        assertThat(response.getMedias()).anyMatch(m -> "PHOTO_OFFICIELLE".equals(m.getTypeMedia()));
        assertThat(response.getMedias()).anyMatch(m -> "PHOTO_TERRAIN".equals(m.getTypeMedia()));
        assertThat(response.getMedias()).anyMatch(m -> "AUDIO_TERRAIN".equals(m.getTypeMedia()));

        // 6. Localités
        assertThat(response.getLocalites()).hasSize(1);
        assertThat(response.getLocalites().get(0).getRegion()).isEqualTo("Koulikoro");
        assertThat(response.getLocalites().get(0).getCercle()).isEqualTo("Kati");
        assertThat(response.getLocalites().get(0).getCommune()).isEqualTo("Siby");
    }

    @Test
    @DisplayName("getPlanteDetail - Plante inexistante ou non validée lance ResourceNotFoundException")
    void getPlanteDetail_NotFound() {
        when(planteRepository.findByIdAndStatut(99L, StatutPlante.VALIDE)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> planteService.getPlanteDetail(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("getConnaissancesByPlante - Succès")
    void getConnaissancesByPlante_Success() {
        when(planteRepository.findByIdAndStatut(1L, StatutPlante.VALIDE)).thenReturn(Optional.of(planteValide));
        when(vertuDeLaPlanteRepository.findByPlanteIdAndStatut(1L, StatutValidation.VALIDE)).thenReturn(List.of(vertuValidee));

        List<PopulationConnaissanceTraditionnelleDto> result = planteService.getConnaissancesByPlante(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(20L);
        assertThat(result.get(0).getPartieUtilisee()).isEqualTo("Feuilles");
    }

    @Test
    @DisplayName("getConnaissancesByPlante - Plante inexistante lance ResourceNotFoundException")
    void getConnaissancesByPlante_NotFound() {
        when(planteRepository.findByIdAndStatut(99L, StatutPlante.VALIDE)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> planteService.getConnaissancesByPlante(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("getEtudesByPlante - Succès")
    void getEtudesByPlante_Success() {
        when(planteRepository.findByIdAndStatut(1L, StatutPlante.VALIDE)).thenReturn(Optional.of(planteValide));
        when(etudeScientifiqueRepository.findByPlanteId(1L)).thenReturn(List.of(etude));

        List<PopulationEtudeScientifiqueDto> result = planteService.getEtudesByPlante(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(30L);
        assertThat(result.get(0).getAnnee()).isEqualTo(2021);
    }

    @Test
    @DisplayName("getEtudesByPlante - Plante inexistante lance ResourceNotFoundException")
    void getEtudesByPlante_NotFound() {
        when(planteRepository.findByIdAndStatut(99L, StatutPlante.VALIDE)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> planteService.getEtudesByPlante(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
