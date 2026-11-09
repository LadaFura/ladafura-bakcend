package com.pharmacopee.ladafura.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.pharmacopee.ladafura.Models.DisponibiliteProduit;
import com.pharmacopee.ladafura.Models.Lot;
import com.pharmacopee.ladafura.Models.MouvementStock;
import com.pharmacopee.ladafura.enums.TypeMouvementStock;
import com.pharmacopee.ladafura.exceptions.BadRequestException;
import com.pharmacopee.ladafura.repository.DisponibiliteProduitRepository;
import com.pharmacopee.ladafura.repository.LotRepository;
import com.pharmacopee.ladafura.repository.MouvementStockRepository;
import com.pharmacopee.ladafura.services.impl.PharmacopeeMouvementStockServiceImpl;

@ExtendWith(MockitoExtension.class)
class PharmacopeeMouvementStockAjustementTest {

    private static final Long PHARMACOPEE_ID = 1L;
    private static final Long FICHE_ID = 50L;

    @Mock private DisponibiliteProduitRepository disponibiliteProduitRepository;
    @Mock private LotRepository lotRepository;
    @Mock private MouvementStockRepository mouvementStockRepository;

    @InjectMocks private PharmacopeeMouvementStockServiceImpl service;

    private final DisponibiliteProduit fiche = DisponibiliteProduit.builder().id(FICHE_ID).quantiteStock(20).build();

    @BeforeEach
    void preparer() {
        lenient().when(disponibiliteProduitRepository.findByIdAndPharmacopeeIdForUpdate(FICHE_ID, PHARMACOPEE_ID)).thenReturn(Optional.of(fiche));
        lenient().when(mouvementStockRepository.save(any(MouvementStock.class))).thenAnswer(appel -> appel.getArgument(0));
    }

    private static Lot lot(int quantite, LocalDate peremption) {
        return Lot.builder().quantiteInitiale(quantite).quantiteRestante(quantite).datePeremption(peremption).build();
    }

    @Test
    @DisplayName("une hausse crée un mouvement sans lot")
    void hausse() {
        List<MouvementStock> mouvements = service.ajuster(PHARMACOPEE_ID, FICHE_ID, 25, "Inventaire", null);

        assertThat(mouvements).singleElement().satisfies(m -> {
            assertThat(m.getType()).isEqualTo(TypeMouvementStock.AJUSTEMENT_INVENTAIRE);
            assertThat(m.getQuantite()).isEqualTo(5);
            assertThat(m.getStockApres()).isEqualTo(25);
        });
        assertThat(fiche.getQuantiteStock()).isEqualTo(25);
    }

    @Test
    @DisplayName("une quantité inchangée ne crée aucun mouvement")
    void inchangee() {
        assertThat(service.ajuster(PHARMACOPEE_ID, FICHE_ID, 20, "Inventaire", null)).isEmpty();
        verify(mouvementStockRepository, never()).save(any());
    }

    @Test
    @DisplayName("une quantité négative est refusée")
    void negative() {
        assertThatThrownBy(() -> service.ajuster(PHARMACOPEE_ID, FICHE_ID, -1, "Inventaire", null)).isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("une baisse consomme le stock sans lot, puis les lots dans l'ordre fourni, et garde Σ lots ≤ stock")
    void baisse() {
        Lot perime = lot(6, LocalDate.now().minusDays(3));
        Lot proche = lot(6, LocalDate.now().plusDays(10));
        when(lotRepository.sumQuantiteRestante(FICHE_ID)).thenReturn(12);
        when(lotRepository.findLotsPourAjustementForUpdate(anyLong(), any())).thenReturn(List.of(perime, proche));

        List<MouvementStock> mouvements = service.ajuster(PHARMACOPEE_ID, FICHE_ID, 9, "Inventaire", null);

        // 20 en stock dont 8 sans lot : 8 hors lot, puis 3 sur le lot périmé.
        assertThat(mouvements).extracting(MouvementStock::getQuantite).containsExactly(-8, -3);
        assertThat(mouvements).extracting(MouvementStock::getStockApres).containsExactly(12, 9);
        assertThat(perime.getQuantiteRestante()).isEqualTo(3);
        assertThat(proche.getQuantiteRestante()).isEqualTo(6);
        assertThat(fiche.getQuantiteStock()).isEqualTo(9);
        assertThat(perime.getQuantiteRestante() + proche.getQuantiteRestante()).isLessThanOrEqualTo(fiche.getQuantiteStock());
    }
}
