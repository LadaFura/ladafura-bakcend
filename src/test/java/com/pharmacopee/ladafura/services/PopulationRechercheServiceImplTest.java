package com.pharmacopee.ladafura.services;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.pharmacopee.ladafura.Models.Localisation;
import com.pharmacopee.ladafura.Models.Maladie;
import com.pharmacopee.ladafura.Models.NomPlante;
import com.pharmacopee.ladafura.Models.Pharmacopee;
import com.pharmacopee.ladafura.Models.Plante;
import com.pharmacopee.ladafura.Models.Produit;
import com.pharmacopee.ladafura.dto.population.recherche.PopulationGlobalSearchResponse;
import com.pharmacopee.ladafura.dto.population.recherche.PopulationMaladieSearchItem;
import com.pharmacopee.ladafura.dto.population.recherche.PopulationPharmacopeeSearchItem;
import com.pharmacopee.ladafura.dto.population.recherche.PopulationPlanteSearchItem;
import com.pharmacopee.ladafura.dto.population.recherche.PopulationProduitSearchItem;
import com.pharmacopee.ladafura.dto.population.recherche.PopulationVernaculaireSearchItem;
import com.pharmacopee.ladafura.enums.StatutPharmacopee;
import com.pharmacopee.ladafura.enums.StatutPlante;
import com.pharmacopee.ladafura.enums.StatutProduit;
import com.pharmacopee.ladafura.mappers.PopulationRechercheMapper;
import com.pharmacopee.ladafura.repository.MaladieRepository;
import com.pharmacopee.ladafura.repository.NomPlanteRepository;
import com.pharmacopee.ladafura.repository.PharmacopeeRepository;
import com.pharmacopee.ladafura.repository.PlanteRepository;
import com.pharmacopee.ladafura.repository.ProduitRepository;
import com.pharmacopee.ladafura.services.impl.PopulationRechercheServiceImpl;

@ExtendWith(MockitoExtension.class)
class PopulationRechercheServiceImplTest {

    @Mock
    private PlanteRepository planteRepository;

    @Mock
    private NomPlanteRepository nomPlanteRepository;

    @Mock
    private MaladieRepository maladieRepository;

    @Mock
    private ProduitRepository produitRepository;

    @Mock
    private PharmacopeeRepository pharmacopeeRepository;

    @Spy
    private PopulationRechercheMapper populationRechercheMapper = new PopulationRechercheMapper();

    @InjectMocks
    private PopulationRechercheServiceImpl rechercheService;

    @Test
    @DisplayName("rechercheGlobale - Consolide les résultats des 5 entités")
    void rechercheGlobale_succes() {
        Plante plante = Plante.builder().id(1L).nomScientifique("Combretum micranthum").statut(StatutPlante.VALIDE).build();
        NomPlante vernaculaire = NomPlante.builder().id(2L).nom("Kinkéliba").langue("Bambara").plante(plante).build();
        Maladie maladie = Maladie.builder().id(3L).nom("Paludisme").build();
        Produit produit = Produit.builder().id(4L).nom("Tisane Kinkéliba").prix(1500.0).statut(StatutProduit.VALIDE).build();
        Localisation loc = Localisation.builder().region("Koulikoro").cercle("Kati").build();
        Pharmacopee pharmacopee = Pharmacopee.builder().id(5L).nom("Pharmacie Mandé").localisation(loc).statut(StatutPharmacopee.VALIDEE).build();

        when(planteRepository.searchTopByStatutAndKeyword(eq(StatutPlante.VALIDE), eq("kinke"), any(Pageable.class)))
                .thenReturn(List.of(plante));
        when(nomPlanteRepository.findByNomContainingIgnoreCase(eq("kinke"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(vernaculaire)));
        when(maladieRepository.findByNomContainingIgnoreCase(eq("kinke"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(maladie)));
        when(produitRepository.searchTopByStatutAndKeyword(eq(StatutProduit.VALIDE), eq("kinke"), any(Pageable.class)))
                .thenReturn(List.of(produit));
        when(pharmacopeeRepository.searchTopByStatutAndKeyword(eq(StatutPharmacopee.VALIDEE), eq("kinke"), any(Pageable.class)))
                .thenReturn(List.of(pharmacopee));

        PopulationGlobalSearchResponse response = rechercheService.rechercheGlobale("kinke");

        assertThat(response).isNotNull();
        assertThat(response.getQuery()).isEqualTo("kinke");
        assertThat(response.getTotalResultats()).isEqualTo(5);
        assertThat(response.getPlantes()).hasSize(1);
        assertThat(response.getPlantes().get(0).getNomScientifique()).isEqualTo("Combretum micranthum");
        assertThat(response.getNomsVernaculaires()).hasSize(1);
        assertThat(response.getMaladies()).hasSize(1);
        assertThat(response.getProduits()).hasSize(1);
        assertThat(response.getPharmacopees()).hasSize(1);
    }

    @Test
    @DisplayName("rechercheGlobale - Retourne résultat vide si la requête est vide ou nulle")
    void rechercheGlobale_queryVide() {
        PopulationGlobalSearchResponse response = rechercheService.rechercheGlobale("   ");

        assertThat(response).isNotNull();
        assertThat(response.getTotalResultats()).isEqualTo(0);
        assertThat(response.getPlantes()).isEmpty();
    }

    @Test
    @DisplayName("rechercherPlantes - Pagination avec mot-clé")
    void rechercherPlantes_succes() {
        Plante plante = Plante.builder().id(1L).nomScientifique("Combretum micranthum").statut(StatutPlante.VALIDE).build();
        Page<Plante> page = new PageImpl<>(List.of(plante));

        when(planteRepository.searchByStatutAndKeyword(eq(StatutPlante.VALIDE), eq("Combretum"), any(Pageable.class)))
                .thenReturn(page);

        Page<PopulationPlanteSearchItem> result = rechercheService.rechercherPlantes("Combretum", PageRequest.of(0, 10));

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getNomScientifique()).isEqualTo("Combretum micranthum");
    }

    @Test
    @DisplayName("rechercherNomsVernaculaires - Multicritère nom et langue")
    void rechercherNomsVernaculaires_succes() {
        NomPlante np = NomPlante.builder().id(2L).nom("Kinkéliba").langue("Bambara").build();
        Page<NomPlante> page = new PageImpl<>(List.of(np));

        when(nomPlanteRepository.searchNomsVernaculaires(eq("Kinkéliba"), eq("Bambara"), any(Pageable.class)))
                .thenReturn(page);

        Page<PopulationVernaculaireSearchItem> result =
                rechercheService.rechercherNomsVernaculaires("Kinkéliba", "Bambara", PageRequest.of(0, 10));

        assertThat(result).isNotNull();
        assertThat(result.getContent().get(0).getNom()).isEqualTo("Kinkéliba");
        assertThat(result.getContent().get(0).getLangue()).isEqualTo("Bambara");
    }

    @Test
    @DisplayName("rechercherMaladies - Paginé par mot-clé")
    void rechercherMaladies_succes() {
        Maladie maladie = Maladie.builder().id(3L).nom("Paludisme").build();
        Page<Maladie> page = new PageImpl<>(List.of(maladie));

        when(maladieRepository.findByNomContainingIgnoreCase(eq("Palu"), any(Pageable.class)))
                .thenReturn(page);

        Page<PopulationMaladieSearchItem> result = rechercheService.rechercherMaladies("Palu", PageRequest.of(0, 10));

        assertThat(result).isNotNull();
        assertThat(result.getContent().get(0).getNom()).isEqualTo("Paludisme");
    }

    @Test
    @DisplayName("rechercherProduits - Multicritère avec catégorie et prixMax")
    void rechercherProduits_succes() {
        Produit p = Produit.builder().id(4L).nom("Tisane").prix(1500.0).statut(StatutProduit.VALIDE).build();
        Page<Produit> page = new PageImpl<>(List.of(p));

        when(produitRepository.searchProduits(eq(StatutProduit.VALIDE), eq("Tisane"), eq(1L), eq(2000.0), any(Pageable.class)))
                .thenReturn(page);

        Page<PopulationProduitSearchItem> result =
                rechercheService.rechercherProduits("Tisane", 1L, 2000.0, PageRequest.of(0, 10));

        assertThat(result).isNotNull();
        assertThat(result.getContent().get(0).getPrix()).isEqualTo(1500.0);
    }

    @Test
    @DisplayName("rechercherPharmacopees - Multicritère géographique")
    void rechercherPharmacopees_succes() {
        Pharmacopee ph = Pharmacopee.builder().id(5L).nom("Officine Koulikoro").statut(StatutPharmacopee.VALIDEE).build();
        Page<Pharmacopee> page = new PageImpl<>(List.of(ph));

        when(pharmacopeeRepository.searchPharmacopees(eq(StatutPharmacopee.VALIDEE), eq("Koulikoro"), eq("Koulikoro"), eq("Kati"), eq("Siby"), any(Pageable.class)))
                .thenReturn(page);

        Page<PopulationPharmacopeeSearchItem> result =
                rechercheService.rechercherPharmacopees("Koulikoro", "Koulikoro", "Kati", "Siby", PageRequest.of(0, 10));

        assertThat(result).isNotNull();
        assertThat(result.getContent().get(0).getNom()).isEqualTo("Officine Koulikoro");
    }
}
