package com.pharmacopee.ladafura.controllers;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.pharmacopee.ladafura.controllers.population.PopulationHistoriqueController;
import com.pharmacopee.ladafura.dto.population.historique.PopulationDepensesSyntheseResponse;
import com.pharmacopee.ladafura.dto.population.historique.PopulationJournalActiviteItem;
import com.pharmacopee.ladafura.dto.population.historique.PopulationProduitAcheteItem;
import com.pharmacopee.ladafura.dto.population.historique.PopulationReleveActiviteResponse;
import com.pharmacopee.ladafura.enums.TypeEvenementHistorique;
import com.pharmacopee.ladafura.services.interfaces.IPopulationHistoriqueService;

@ExtendWith(MockitoExtension.class)
class PopulationHistoriqueControllerTest {

    @Mock
    private IPopulationHistoriqueService historiqueService;

    @InjectMocks
    private PopulationHistoriqueController historiqueController;

    @Test
    @DisplayName("GET /api/v1/population/historique/activite - 200 OK")
    void getJournalActivite_200OK() {
        PopulationJournalActiviteItem item = PopulationJournalActiviteItem.builder()
                .id("CMD-1")
                .type(TypeEvenementHistorique.COMMANDE)
                .titre("Commande LIVREE")
                .description("Commande CMD-2026-0001")
                .dateEvenement(LocalDateTime.now())
                .montant(10000.0)
                .build();

        Pageable pageable = PageRequest.of(0, 20);
        when(historiqueService.getJournalActivite(eq(TypeEvenementHistorique.COMMANDE), any(), any(), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(item)));

        ResponseEntity<Page<PopulationJournalActiviteItem>> response =
                historiqueController.getJournalActivite(TypeEvenementHistorique.COMMANDE, null, null, pageable);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getTotalElements()).isEqualTo(1);
        assertThat(response.getBody().getContent().get(0).getId()).isEqualTo("CMD-1");
        verify(historiqueService).getJournalActivite(eq(TypeEvenementHistorique.COMMANDE), any(), any(), eq(pageable));
    }

    @Test
    @DisplayName("GET /api/v1/population/historique/depenses - 200 OK")
    void getSyntheseDepenses_200OK() {
        PopulationDepensesSyntheseResponse synthese = PopulationDepensesSyntheseResponse.builder()
                .montantTotalDepense(25000.0)
                .nombreTotalTransactions(4)
                .panierMoyen(6250.0)
                .depensesParMois(List.of())
                .depensesParMethode(List.of())
                .build();

        when(historiqueService.getSyntheseDepenses()).thenReturn(synthese);

        ResponseEntity<PopulationDepensesSyntheseResponse> response =
                historiqueController.getSyntheseDepenses();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMontantTotalDepense()).isEqualTo(25000.0);
        assertThat(response.getBody().getPanierMoyen()).isEqualTo(6250.0);
        verify(historiqueService).getSyntheseDepenses();
    }

    @Test
    @DisplayName("GET /api/v1/population/historique/produits-achetes - 200 OK")
    void getHistoriqueProduitsAchetes_200OK() {
        PopulationProduitAcheteItem item = PopulationProduitAcheteItem.builder()
                .produitId(101L)
                .nomProduit("Sirop Kinkeliba")
                .quantiteTotaleAchetee(4)
                .montantTotalDepense(12000.0)
                .dejaEvalue(false)
                .build();

        Pageable pageable = PageRequest.of(0, 20);
        when(historiqueService.getHistoriqueProduitsAchetes(pageable))
                .thenReturn(new PageImpl<>(List.of(item)));

        ResponseEntity<Page<PopulationProduitAcheteItem>> response =
                historiqueController.getHistoriqueProduitsAchetes(pageable);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent().get(0).getNomProduit()).isEqualTo("Sirop Kinkeliba");
        verify(historiqueService).getHistoriqueProduitsAchetes(pageable);
    }

    @Test
    @DisplayName("GET /api/v1/population/historique/releve - 200 OK")
    void getReleveActivite_200OK() {
        PopulationReleveActiviteResponse releve = PopulationReleveActiviteResponse.builder()
                .utilisateurId(10L)
                .nomComplet("Fatoumata Coulibaly")
                .email("fatou@ladafura.ml")
                .totalCommandes(3)
                .totalCommandesLivrees(3)
                .totalDepense(18500.0)
                .totalAvis(2)
                .totalFavoris(5)
                .dernieresActivites(List.of())
                .build();

        when(historiqueService.getReleveActivite()).thenReturn(releve);

        ResponseEntity<PopulationReleveActiviteResponse> response =
                historiqueController.getReleveActivite();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getNomComplet()).isEqualTo("Fatoumata Coulibaly");
        assertThat(response.getBody().getTotalCommandes()).isEqualTo(3);
        verify(historiqueService).getReleveActivite();
    }
}
