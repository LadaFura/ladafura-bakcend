package com.pharmacopee.ladafura.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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

import com.pharmacopee.ladafura.Models.CategorieProduit;
import com.pharmacopee.ladafura.Models.CompositionProduit;
import com.pharmacopee.ladafura.Models.DisponibiliteProduit;
import com.pharmacopee.ladafura.Models.Localisation;
import com.pharmacopee.ladafura.Models.Maladie;
import com.pharmacopee.ladafura.Models.ModeRetrait;
import com.pharmacopee.ladafura.Models.NomPlante;
import com.pharmacopee.ladafura.Models.Pharmacopee;
import com.pharmacopee.ladafura.Models.Plante;
import com.pharmacopee.ladafura.Models.Produit;
import com.pharmacopee.ladafura.dto.population.produit.PopulationOffrePharmacopeeDto;
import com.pharmacopee.ladafura.dto.population.produit.PopulationProduitDetailResponse;
import com.pharmacopee.ladafura.dto.population.produit.PopulationProduitSummaryResponse;
import com.pharmacopee.ladafura.enums.StatutAvis;
import com.pharmacopee.ladafura.enums.StatutPharmacopee;
import com.pharmacopee.ladafura.enums.StatutPlante;
import com.pharmacopee.ladafura.enums.StatutProduit;
import com.pharmacopee.ladafura.enums.TypeModeRetrait;
import com.pharmacopee.ladafura.exceptions.ResourceNotFoundException;
import com.pharmacopee.ladafura.mappers.PopulationProduitMapper;
import com.pharmacopee.ladafura.repository.AvisRepository;
import com.pharmacopee.ladafura.repository.CompositionProduitRepository;
import com.pharmacopee.ladafura.repository.DisponibiliteProduitRepository;
import com.pharmacopee.ladafura.repository.ModeRetraitRepository;
import com.pharmacopee.ladafura.repository.ProduitRepository;
import com.pharmacopee.ladafura.services.impl.PopulationProduitServiceImpl;

@ExtendWith(MockitoExtension.class)
class PopulationProduitServiceImplTest {

    @Mock
    private ProduitRepository produitRepository;

    @Mock
    private CompositionProduitRepository compositionProduitRepository;

    @Mock
    private DisponibiliteProduitRepository disponibiliteProduitRepository;

    @Mock
    private ModeRetraitRepository modeRetraitRepository;

    @Spy
    private PopulationProduitMapper mapper = new PopulationProduitMapper();

    @InjectMocks
    private PopulationProduitServiceImpl produitService;

    private Produit produitValide;
    private CategorieProduit categorie;
    private Plante plante;
    private CompositionProduit composition;
    private Pharmacopee pharmacopee;
    private DisponibiliteProduit disponibilite;
    private ModeRetrait modeRetrait;

    @BeforeEach
    void setUp() {
        categorie = CategorieProduit.builder()
                .id(1L)
                .nom("Tisanes et Infusions")
                .description("Tisanes traditionnelles")
                .build();

        Maladie paludisme = Maladie.builder()
                .id(10L)
                .nom("Paludisme")
                .description("Fièvres intermittentes")
                .build();

        NomPlante nomVernaculaire = NomPlante.builder()
                .id(1L)
                .nom("Kinkéliba")
                .langue("Bambara")
                .build();

        plante = Plante.builder()
                .id(1L)
                .nomScientifique("Combretum micranthum")
                .statut(StatutPlante.VALIDE)
                .photoUrl("https://ladafura.ml/uploads/kinkeliba.jpg")
                .nomsPlante(new ArrayList<>(List.of(nomVernaculaire)))
                .maladies(Set.of(paludisme))
                .build();

        produitValide = Produit.builder()
                .id(5L)
                .nom("Tisane Kinkéliba Bio")
                .description("Infusion naturelle pour le foie et la digestion")
                .forme("Sachet 100g")
                .composition("Feuilles de Combretum micranthum séchées")
                .prix(2500.0)
                .photoUrl("https://ladafura.ml/uploads/produits/tisane.jpg")
                .statut(StatutProduit.VALIDE)
                .categorie(categorie)
                .compositions(new ArrayList<>())
                .build();

        composition = CompositionProduit.builder()
                .id(1L)
                .plante(plante)
                .produit(produitValide)
                .quantite(100.0)
                .unite("g")
                .build();
        produitValide.getCompositions().add(composition);

        Localisation loc = Localisation.builder()
                .id(2L)
                .region("Koulikoro")
                .cercle("Kati")
                .commune("Siby")
                .localite("Centre")
                .latitude(12.38)
                .longitude(-8.33)
                .build();

        pharmacopee = Pharmacopee.builder()
                .id(3L)
                .nom("Pharmacie Mandé")
                .telephone("+223 70 00 11 22")
                .statut(StatutPharmacopee.VALIDEE)
                .localisation(loc)
                .build();

        disponibilite = DisponibiliteProduit.builder()
                .id(12L)
                .produit(produitValide)
                .pharmacopee(pharmacopee)
                .disponible(true)
                .quantiteStock(20)
                .prix(2600.0)
                .build();

        modeRetrait = ModeRetrait.builder()
                .id(1L)
                .type(TypeModeRetrait.LIVRAISON)
                .actif(true)
                .frais(1500.0)
                .pharmacopee(pharmacopee)
                .build();
    }

    @Test
    @DisplayName("listerProduits - Succès avec pagination, filtres et compteurs")
    void listerProduits_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Produit> page = new PageImpl<>(List.of(produitValide));

        when(produitRepository.searchProduits(StatutProduit.VALIDE, "tisane", 1L, 5000.0, pageable))
                .thenReturn(page);
        when(disponibiliteProduitRepository.findOffresValideesByProduitId(5L))
                .thenReturn(List.of(disponibilite));

        Page<PopulationProduitSummaryResponse> result =
                produitService.listerProduits("tisane", 1L, 5000.0, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        PopulationProduitSummaryResponse item = result.getContent().get(0);
        assertThat(item.getId()).isEqualTo(5L);
        assertThat(item.getNom()).isEqualTo("Tisane Kinkéliba Bio");
        assertThat(item.getPrixIndicatif()).isEqualTo(2500.0);
        assertThat(item.getCategorieNom()).isEqualTo("Tisanes et Infusions");
        assertThat(item.getPlantesPrincipales()).contains("Combretum micranthum");
        assertThat(item.getNombrePharmacopees()).isEqualTo(1);
        assertThat(item.isDisponibleEnPharmacie()).isTrue();
        assertThat(item.getNoteMoyenne()).isNull();
        assertThat(item.getNombreAvis()).isEqualTo(0L);

        verify(produitRepository).searchProduits(StatutProduit.VALIDE, "tisane", 1L, 5000.0, pageable);
    }

    @Test
    @DisplayName("getProduitDetail - Succès avec composition, maladies, offres et modes de retrait")
    void getProduitDetail_Success() {
        when(produitRepository.findByIdAndStatut(5L, StatutProduit.VALIDE))
                .thenReturn(Optional.of(produitValide));
        when(compositionProduitRepository.findByProduitId(5L))
                .thenReturn(List.of(composition));
        when(disponibiliteProduitRepository.findOffresValideesByProduitId(5L))
                .thenReturn(List.of(disponibilite));
        when(modeRetraitRepository.findByPharmacopeeId(3L))
                .thenReturn(List.of(modeRetrait));

        PopulationProduitDetailResponse response = produitService.getProduitDetail(5L);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(5L);
        assertThat(response.getNom()).isEqualTo("Tisane Kinkéliba Bio");
        assertThat(response.getForme()).isEqualTo("Sachet 100g");
        assertThat(response.getCompositionTexte()).contains("Feuilles de Combretum micranthum");

        // Compositions
        assertThat(response.getCompositions()).hasSize(1);
        assertThat(response.getCompositions().get(0).getNomScientifique()).isEqualTo("Combretum micranthum");
        assertThat(response.getCompositions().get(0).getNomsVernaculaires()).contains("Kinkéliba");
        assertThat(response.getCompositions().get(0).getQuantite()).isEqualTo(100.0);

        // Maladies
        assertThat(response.getMaladies()).hasSize(1);
        assertThat(response.getMaladies().get(0).getNom()).isEqualTo("Paludisme");

        // Offres pharmacopées
        assertThat(response.getOffresPharmacopees()).hasSize(1);
        PopulationOffrePharmacopeeDto offre = response.getOffresPharmacopees().get(0);
        assertThat(offre.getPharmacopeeId()).isEqualTo(3L);
        assertThat(offre.getNomPharmacopee()).isEqualTo("Pharmacie Mandé");
        assertThat(offre.getPrix()).isEqualTo(2600.0);
        assertThat(offre.getQuantiteStock()).isEqualTo(20);
        assertThat(offre.getModesRetrait()).hasSize(1);
        assertThat(offre.getModesRetrait().get(0).getType()).isEqualTo("LIVRAISON");
    }

    @Test
    @DisplayName("getProduitDetail - Produit inexistant ou non validé lance ResourceNotFoundException")
    void getProduitDetail_NotFound() {
        when(produitRepository.findByIdAndStatut(99L, StatutProduit.VALIDE))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> produitService.getProduitDetail(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("getOffresByProduit - Succès")
    void getOffresByProduit_Success() {
        when(produitRepository.findByIdAndStatut(5L, StatutProduit.VALIDE))
                .thenReturn(Optional.of(produitValide));
        when(disponibiliteProduitRepository.findOffresValideesByProduitId(5L))
                .thenReturn(List.of(disponibilite));
        when(modeRetraitRepository.findByPharmacopeeId(3L))
                .thenReturn(List.of(modeRetrait));

        List<PopulationOffrePharmacopeeDto> offres = produitService.getOffresByProduit(5L);

        assertThat(offres).hasSize(1);
        assertThat(offres.get(0).getNomPharmacopee()).isEqualTo("Pharmacie Mandé");
        assertThat(offres.get(0).getDisponible()).isTrue();
    }

    @Test
    @DisplayName("getOffresByProduit - Produit inexistant lance ResourceNotFoundException")
    void getOffresByProduit_NotFound() {
        when(produitRepository.findByIdAndStatut(99L, StatutProduit.VALIDE))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> produitService.getOffresByProduit(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
