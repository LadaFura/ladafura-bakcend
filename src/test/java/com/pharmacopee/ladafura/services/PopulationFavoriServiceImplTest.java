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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.pharmacopee.ladafura.Models.Favori;
import com.pharmacopee.ladafura.Models.Pharmacopee;
import com.pharmacopee.ladafura.Models.Plante;
import com.pharmacopee.ladafura.Models.Produit;
import com.pharmacopee.ladafura.Models.Utilisateur;
import com.pharmacopee.ladafura.dto.population.favori.PopulationFavoriCheckResponse;
import com.pharmacopee.ladafura.dto.population.favori.PopulationFavoriCountResponse;
import com.pharmacopee.ladafura.dto.population.favori.PopulationFavoriItemResponse;
import com.pharmacopee.ladafura.dto.population.favori.PopulationToggleFavoriRequest;
import com.pharmacopee.ladafura.dto.population.favori.PopulationToggleFavoriResponse;
import com.pharmacopee.ladafura.dto.population.pharmacopee.PopulationPharmacopeeSummaryResponse;
import com.pharmacopee.ladafura.dto.population.plante.PopulationPlanteSummaryResponse;
import com.pharmacopee.ladafura.dto.population.produit.PopulationProduitSummaryResponse;
import com.pharmacopee.ladafura.enums.Role;
import com.pharmacopee.ladafura.enums.StatutPharmacopee;
import com.pharmacopee.ladafura.enums.StatutPlante;
import com.pharmacopee.ladafura.enums.StatutProduit;
import com.pharmacopee.ladafura.enums.StatutUtilisateur;
import com.pharmacopee.ladafura.enums.TypeFavori;
import com.pharmacopee.ladafura.exceptions.ResourceNotFoundException;
import com.pharmacopee.ladafura.mappers.PopulationFavoriMapper;
import com.pharmacopee.ladafura.repository.FavoriRepository;
import com.pharmacopee.ladafura.repository.PharmacopeeRepository;
import com.pharmacopee.ladafura.repository.PlanteRepository;
import com.pharmacopee.ladafura.repository.ProduitRepository;
import com.pharmacopee.ladafura.services.impl.PopulationFavoriServiceImpl;
import com.pharmacopee.ladafura.services.interfaces.IPopulationAuthService;

@ExtendWith(MockitoExtension.class)
class PopulationFavoriServiceImplTest {

    @Mock
    private FavoriRepository favoriRepository;

    @Mock
    private PlanteRepository planteRepository;

    @Mock
    private ProduitRepository produitRepository;

    @Mock
    private PharmacopeeRepository pharmacopeeRepository;

    @Mock
    private IPopulationAuthService populationAuthService;

    @Mock
    private PopulationFavoriMapper mapper;

    @InjectMocks
    private PopulationFavoriServiceImpl favoriService;

    private Utilisateur currentUser;
    private Plante testPlante;
    private Produit testProduit;
    private Pharmacopee testPharmacopee;
    private Favori favoriPlante;

    @BeforeEach
    void setUp() {
        currentUser = new Utilisateur();
        currentUser.setId(10L);
        currentUser.setEmail("client@ladafura.ml");
        currentUser.setNom("Coulibaly");
        currentUser.setPrenom("Awa");
        currentUser.setRole(Role.POPULATION);
        currentUser.setStatut(StatutUtilisateur.ACTIF);

        testPlante = Plante.builder()
                .id(1L)
                .nomScientifique("Artemisia annua")
                .description("Plante fébrifuge")
                .statut(StatutPlante.VALIDE)
                .build();

        testProduit = Produit.builder()
                .id(5L)
                .nom("Sirop d'Artemisia")
                .forme("Sirop")
                .prix(2500.0)
                .statut(StatutProduit.VALIDE)
                .build();

        testPharmacopee = Pharmacopee.builder()
                .id(3L)
                .nom("Pharmacie Mandé")
                .statut(StatutPharmacopee.VALIDEE)
                .build();

        favoriPlante = Favori.builder()
                .id(100L)
                .utilisateur(currentUser)
                .plante(testPlante)
                .dateAjout(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("toggleFavori - Plante : Ajout quand non présent")
    void toggleFavori_Plante_Ajout() {
        when(populationAuthService.getCurrentPopulationUser()).thenReturn(currentUser);
        when(planteRepository.findByIdAndStatut(1L, StatutPlante.VALIDE)).thenReturn(Optional.of(testPlante));
        when(favoriRepository.findByUtilisateurIdAndPlanteId(10L, 1L)).thenReturn(Optional.empty());
        when(favoriRepository.save(any(Favori.class))).thenReturn(favoriPlante);
        when(mapper.toToggleResponse(TypeFavori.PLANTE, 1L, true, 100L, "Plante ajoutée à vos favoris avec succès."))
                .thenReturn(PopulationToggleFavoriResponse.builder().type(TypeFavori.PLANTE).cibleId(1L).favori(true).favoriId(100L).build());

        PopulationToggleFavoriRequest request = PopulationToggleFavoriRequest.builder()
                .type(TypeFavori.PLANTE)
                .cibleId(1L)
                .build();

        PopulationToggleFavoriResponse response = favoriService.toggleFavori(request);

        assertThat(response).isNotNull();
        assertThat(response.isFavori()).isTrue();
        assertThat(response.getFavoriId()).isEqualTo(100L);
        verify(favoriRepository).save(any(Favori.class));
    }

    @Test
    @DisplayName("toggleFavori - Plante : Retrait quand déjà présent")
    void toggleFavori_Plante_Retrait() {
        when(populationAuthService.getCurrentPopulationUser()).thenReturn(currentUser);
        when(planteRepository.findByIdAndStatut(1L, StatutPlante.VALIDE)).thenReturn(Optional.of(testPlante));
        when(favoriRepository.findByUtilisateurIdAndPlanteId(10L, 1L)).thenReturn(Optional.of(favoriPlante));
        when(mapper.toToggleResponse(TypeFavori.PLANTE, 1L, false, null, "Plante retirée de vos favoris."))
                .thenReturn(PopulationToggleFavoriResponse.builder().type(TypeFavori.PLANTE).cibleId(1L).favori(false).build());

        PopulationToggleFavoriRequest request = PopulationToggleFavoriRequest.builder()
                .type(TypeFavori.PLANTE)
                .cibleId(1L)
                .build();

        PopulationToggleFavoriResponse response = favoriService.toggleFavori(request);

        assertThat(response).isNotNull();
        assertThat(response.isFavori()).isFalse();
        verify(favoriRepository).delete(favoriPlante);
    }

    @Test
    @DisplayName("toggleFavori - Produit : Ajout quand non présent")
    void toggleFavori_Produit_Ajout() {
        when(populationAuthService.getCurrentPopulationUser()).thenReturn(currentUser);
        when(produitRepository.findByIdAndStatut(5L, StatutProduit.VALIDE)).thenReturn(Optional.of(testProduit));
        when(favoriRepository.findByUtilisateurIdAndProduitId(10L, 5L)).thenReturn(Optional.empty());
        Favori favProduit = Favori.builder().id(101L).utilisateur(currentUser).produit(testProduit).build();
        when(favoriRepository.save(any(Favori.class))).thenReturn(favProduit);
        when(mapper.toToggleResponse(TypeFavori.PRODUIT, 5L, true, 101L, "Produit ajouté à vos favoris avec succès."))
                .thenReturn(PopulationToggleFavoriResponse.builder().type(TypeFavori.PRODUIT).cibleId(5L).favori(true).favoriId(101L).build());

        PopulationToggleFavoriRequest request = PopulationToggleFavoriRequest.builder()
                .type(TypeFavori.PRODUIT)
                .cibleId(5L)
                .build();

        PopulationToggleFavoriResponse response = favoriService.toggleFavori(request);

        assertThat(response.isFavori()).isTrue();
        assertThat(response.getFavoriId()).isEqualTo(101L);
    }

    @Test
    @DisplayName("toggleFavori - Pharmacopée : Ajout quand non présent")
    void toggleFavori_Pharmacopee_Ajout() {
        when(populationAuthService.getCurrentPopulationUser()).thenReturn(currentUser);
        when(pharmacopeeRepository.findByIdAndStatut(3L, StatutPharmacopee.VALIDEE)).thenReturn(Optional.of(testPharmacopee));
        when(favoriRepository.findByUtilisateurIdAndPharmacopeeId(10L, 3L)).thenReturn(Optional.empty());
        Favori favPh = Favori.builder().id(102L).utilisateur(currentUser).pharmacopee(testPharmacopee).build();
        when(favoriRepository.save(any(Favori.class))).thenReturn(favPh);
        when(mapper.toToggleResponse(TypeFavori.PHARMACOPEE, 3L, true, 102L, "Pharmacopée ajoutée à vos favoris avec succès."))
                .thenReturn(PopulationToggleFavoriResponse.builder().type(TypeFavori.PHARMACOPEE).cibleId(3L).favori(true).favoriId(102L).build());

        PopulationToggleFavoriRequest request = PopulationToggleFavoriRequest.builder()
                .type(TypeFavori.PHARMACOPEE)
                .cibleId(3L)
                .build();

        PopulationToggleFavoriResponse response = favoriService.toggleFavori(request);

        assertThat(response.isFavori()).isTrue();
        assertThat(response.getFavoriId()).isEqualTo(102L);
    }

    @Test
    @DisplayName("toggleFavori - Cible introuvable lance ResourceNotFoundException")
    void toggleFavori_NotFound() {
        when(populationAuthService.getCurrentPopulationUser()).thenReturn(currentUser);
        when(planteRepository.findByIdAndStatut(99L, StatutPlante.VALIDE)).thenReturn(Optional.empty());

        PopulationToggleFavoriRequest request = PopulationToggleFavoriRequest.builder()
                .type(TypeFavori.PLANTE)
                .cibleId(99L)
                .build();

        assertThatThrownBy(() -> favoriService.toggleFavori(request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("ajouterPlanteFavori - Déjà présent renvoie statut sans duplication")
    void ajouterPlanteFavori_DejaPresent() {
        when(populationAuthService.getCurrentPopulationUser()).thenReturn(currentUser);
        when(favoriRepository.findByUtilisateurIdAndPlanteId(10L, 1L)).thenReturn(Optional.of(favoriPlante));
        when(mapper.toToggleResponse(TypeFavori.PLANTE, 1L, true, 100L, "Cette plante figure déjà dans vos favoris."))
                .thenReturn(PopulationToggleFavoriResponse.builder().type(TypeFavori.PLANTE).cibleId(1L).favori(true).favoriId(100L).build());

        PopulationToggleFavoriResponse response = favoriService.ajouterPlanteFavori(1L);

        assertThat(response.isFavori()).isTrue();
        assertThat(response.getFavoriId()).isEqualTo(100L);
    }

    @Test
    @DisplayName("supprimerFavoriById - Succès")
    void supprimerFavoriById_Success() {
        when(populationAuthService.getCurrentPopulationUser()).thenReturn(currentUser);
        when(favoriRepository.findByIdAndUtilisateurId(100L, 10L)).thenReturn(Optional.of(favoriPlante));

        favoriService.supprimerFavoriById(100L);

        verify(favoriRepository).delete(favoriPlante);
    }

    @Test
    @DisplayName("supprimerFavoriById - Introuvable lance ResourceNotFoundException")
    void supprimerFavoriById_NotFound() {
        when(populationAuthService.getCurrentPopulationUser()).thenReturn(currentUser);
        when(favoriRepository.findByIdAndUtilisateurId(999L, 10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> favoriService.supprimerFavoriById(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("checkFavori - Présent renvoie true")
    void checkFavori_Present() {
        when(populationAuthService.getCurrentPopulationUser()).thenReturn(currentUser);
        when(favoriRepository.findByUtilisateurIdAndPlanteId(10L, 1L)).thenReturn(Optional.of(favoriPlante));
        when(mapper.toCheckResponse(TypeFavori.PLANTE, 1L, true, 100L))
                .thenReturn(PopulationFavoriCheckResponse.builder().type(TypeFavori.PLANTE).cibleId(1L).favori(true).favoriId(100L).build());

        PopulationFavoriCheckResponse response = favoriService.checkFavori(TypeFavori.PLANTE, 1L);

        assertThat(response.isFavori()).isTrue();
        assertThat(response.getFavoriId()).isEqualTo(100L);
    }

    @Test
    @DisplayName("getFavoriCounts - Succès")
    void getFavoriCounts_Success() {
        when(populationAuthService.getCurrentPopulationUser()).thenReturn(currentUser);
        when(favoriRepository.countByUtilisateurId(10L)).thenReturn(6L);
        when(favoriRepository.countByUtilisateurIdAndPlanteIsNotNull(10L)).thenReturn(2L);
        when(favoriRepository.countByUtilisateurIdAndProduitIsNotNull(10L)).thenReturn(3L);
        when(favoriRepository.countByUtilisateurIdAndPharmacopeeIsNotNull(10L)).thenReturn(1L);

        when(mapper.toCountResponse(6L, 2L, 3L, 1L))
                .thenReturn(PopulationFavoriCountResponse.builder().total(6L).totalPlantes(2L).totalProduits(3L).totalPharmacopees(1L).build());

        PopulationFavoriCountResponse response = favoriService.getFavoriCounts();

        assertThat(response.getTotal()).isEqualTo(6L);
        assertThat(response.getTotalPlantes()).isEqualTo(2L);
        assertThat(response.getTotalProduits()).isEqualTo(3L);
        assertThat(response.getTotalPharmacopees()).isEqualTo(1L);
    }

    @Test
    @DisplayName("getMesFavoris - Tous les types")
    void getMesFavoris_All() {
        when(populationAuthService.getCurrentPopulationUser()).thenReturn(currentUser);
        Pageable pageable = PageRequest.of(0, 10);
        when(favoriRepository.findByUtilisateurId(10L, pageable)).thenReturn(new PageImpl<>(List.of(favoriPlante)));
        when(mapper.toItemResponse(favoriPlante)).thenReturn(PopulationFavoriItemResponse.builder().id(100L).type(TypeFavori.PLANTE).build());

        Page<PopulationFavoriItemResponse> result = favoriService.getMesFavoris(null, pageable);

        assertThat(result).hasSize(1);
        assertThat(result.getContent().get(0).getId()).isEqualTo(100L);
    }

    @Test
    @DisplayName("getMesPlantesFavorites - Succès")
    void getMesPlantesFavorites_Success() {
        when(populationAuthService.getCurrentPopulationUser()).thenReturn(currentUser);
        Pageable pageable = PageRequest.of(0, 10);
        when(favoriRepository.findByUtilisateurIdAndPlanteIsNotNull(10L, pageable))
                .thenReturn(new PageImpl<>(List.of(favoriPlante)));
        when(mapper.mapPlanteSummary(testPlante))
                .thenReturn(PopulationPlanteSummaryResponse.builder().id(1L).nomScientifique("Artemisia annua").build());

        Page<PopulationPlanteSummaryResponse> result = favoriService.getMesPlantesFavorites(pageable);

        assertThat(result).hasSize(1);
        assertThat(result.getContent().get(0).getNomScientifique()).isEqualTo("Artemisia annua");
    }

    @Test
    @DisplayName("getMesProduitsFavoris - Succès")
    void getMesProduitsFavoris_Success() {
        when(populationAuthService.getCurrentPopulationUser()).thenReturn(currentUser);
        Pageable pageable = PageRequest.of(0, 10);
        Favori favProduit = Favori.builder().id(101L).utilisateur(currentUser).produit(testProduit).build();
        when(favoriRepository.findByUtilisateurIdAndProduitIsNotNull(10L, pageable))
                .thenReturn(new PageImpl<>(List.of(favProduit)));
        when(mapper.mapProduitSummary(testProduit))
                .thenReturn(PopulationProduitSummaryResponse.builder().id(5L).nom("Sirop d'Artemisia").build());

        Page<PopulationProduitSummaryResponse> result = favoriService.getMesProduitsFavoris(pageable);

        assertThat(result).hasSize(1);
        assertThat(result.getContent().get(0).getNom()).isEqualTo("Sirop d'Artemisia");
    }

    @Test
    @DisplayName("getMesPharmacopeesFavorites - Succès")
    void getMesPharmacopeesFavorites_Success() {
        when(populationAuthService.getCurrentPopulationUser()).thenReturn(currentUser);
        Pageable pageable = PageRequest.of(0, 10);
        Favori favPh = Favori.builder().id(102L).utilisateur(currentUser).pharmacopee(testPharmacopee).build();
        when(favoriRepository.findByUtilisateurIdAndPharmacopeeIsNotNull(10L, pageable))
                .thenReturn(new PageImpl<>(List.of(favPh)));
        when(mapper.mapPharmacopeeSummary(testPharmacopee))
                .thenReturn(PopulationPharmacopeeSummaryResponse.builder().id(3L).nom("Pharmacie Mandé").build());

        Page<PopulationPharmacopeeSummaryResponse> result = favoriService.getMesPharmacopeesFavorites(pageable);

        assertThat(result).hasSize(1);
        assertThat(result.getContent().get(0).getNom()).isEqualTo("Pharmacie Mandé");
    }
}
