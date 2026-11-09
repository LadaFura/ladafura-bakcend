package com.pharmacopee.ladafura.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

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
import com.pharmacopee.ladafura.Models.Pharmacopee;
import com.pharmacopee.ladafura.enums.TypeMouvementStock;
import com.pharmacopee.ladafura.exceptions.BadRequestException;
import com.pharmacopee.ladafura.exceptions.ConflictException;
import com.pharmacopee.ladafura.exceptions.ResourceNotFoundException;
import com.pharmacopee.ladafura.repository.DisponibiliteProduitRepository;
import com.pharmacopee.ladafura.repository.LotRepository;
import com.pharmacopee.ladafura.repository.MouvementStockRepository;
import com.pharmacopee.ladafura.services.impl.PharmacopeeMouvementStockServiceImpl;

@ExtendWith(MockitoExtension.class)
class PharmacopeeMouvementStockServiceImplTest {

    private static final Long PHARMACOPEE_ID = 5L;
    private static final Long FICHE_ID = 8L;

    @Mock private DisponibiliteProduitRepository disponibiliteProduitRepository;
    @Mock private LotRepository lotRepository;
    @Mock private MouvementStockRepository mouvementStockRepository;

    @InjectMocks private PharmacopeeMouvementStockServiceImpl service;

    private final Pharmacopee pharmacopee = Pharmacopee.builder().id(PHARMACOPEE_ID).nom("Officine").build();

    @BeforeEach
    void preparer() {
        lenient().when(mouvementStockRepository.save(any(MouvementStock.class))).thenAnswer(i -> i.getArgument(0));
    }

    private DisponibiliteProduit fiche(int stock) {
        DisponibiliteProduit fiche = DisponibiliteProduit.builder().id(FICHE_ID).pharmacopee(pharmacopee).quantiteStock(stock).build();
        when(disponibiliteProduitRepository.findByIdAndPharmacopeeIdForUpdate(FICHE_ID, PHARMACOPEE_ID)).thenReturn(Optional.of(fiche));
        return fiche;
    }

    private void lots(int totalSuivi, Lot... vendables) {
        when(lotRepository.findLotsVendablesForUpdate(eq(FICHE_ID), any())).thenReturn(List.of(vendables));
        when(lotRepository.sumQuantiteRestante(FICHE_ID)).thenReturn(totalSuivi);
    }

    private static Lot lot(long id, int restante) {
        return Lot.builder().id(id).quantiteInitiale(restante).quantiteRestante(restante).build();
    }

    @Test
    @DisplayName("une entrée augmente le stock et trace le mouvement")
    void entree() {
        DisponibiliteProduit fiche = fiche(10);

        MouvementStock mouvement = service.entrer(PHARMACOPEE_ID, FICHE_ID, 5, TypeMouvementStock.ENTREE_ACHAT, null, null, null, null);

        assertThat(fiche.getQuantiteStock()).isEqualTo(15);
        assertThat(mouvement.getStockAvant()).isEqualTo(10);
        assertThat(mouvement.getStockApres()).isEqualTo(15);
    }

    @Test
    @DisplayName("une quantité nulle est refusée")
    void quantiteNulle() {
        assertThatThrownBy(() -> service.entrer(PHARMACOPEE_ID, FICHE_ID, 0, TypeMouvementStock.ENTREE_ACHAT, null, null, null, null))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("la fiche d'une autre pharmacopée n'est pas trouvée")
    void autrePharmacopee() {
        when(disponibiliteProduitRepository.findByIdAndPharmacopeeIdForUpdate(anyLong(), anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.entrer(PHARMACOPEE_ID, FICHE_ID, 1, TypeMouvementStock.ENTREE_ACHAT, null, null, null, null))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("sans lot, une sortie crée un seul mouvement")
    void sortieSansLot() {
        DisponibiliteProduit fiche = fiche(10);
        lots(0);

        List<MouvementStock> mouvements = service.sortir(PHARMACOPEE_ID, FICHE_ID, 4, TypeMouvementStock.SORTIE_VENTE, null, null, null);

        assertThat(mouvements).hasSize(1);
        assertThat(mouvements.get(0).getQuantite()).isEqualTo(-4);
        assertThat(fiche.getQuantiteStock()).isEqualTo(6);
    }

    @Test
    @DisplayName("la sortie puise dans les lots dans l'ordre fourni, un mouvement par lot")
    void sortieSurPlusieursLots() {
        DisponibiliteProduit fiche = fiche(10);
        Lot premier = lot(1, 3);
        Lot second = lot(2, 7);
        lots(10, premier, second);

        List<MouvementStock> mouvements = service.sortir(PHARMACOPEE_ID, FICHE_ID, 5, TypeMouvementStock.SORTIE_VENTE, null, null, null);

        assertThat(mouvements).extracting(MouvementStock::getLot).containsExactly(premier, second);
        assertThat(mouvements).extracting(MouvementStock::getQuantite).containsExactly(-3, -2);
        assertThat(mouvements).extracting(MouvementStock::getStockApres).containsExactly(7, 5);
        assertThat(premier.getQuantiteRestante()).isZero();
        assertThat(second.getQuantiteRestante()).isEqualTo(5);
        assertThat(fiche.getQuantiteStock()).isEqualTo(5);
    }

    @Test
    @DisplayName("les unités d'un lot périmé ne sont jamais vendues")
    void lotPerimeExclu() {
        fiche(10);
        // 4 unités vendables sur 10 : les 6 autres sont dans un lot périmé, absent des lots vendables.
        lots(10, lot(1, 4));

        assertThatThrownBy(() -> service.sortir(PHARMACOPEE_ID, FICHE_ID, 5, TypeMouvementStock.SORTIE_VENTE, null, null, null))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    @DisplayName("après les lots, la sortie utilise le stock non suivi par lot")
    void lotsPuisStockSansLot() {
        DisponibiliteProduit fiche = fiche(10);
        Lot lot = lot(1, 3);
        lots(3, lot);

        List<MouvementStock> mouvements = service.sortir(PHARMACOPEE_ID, FICHE_ID, 5, TypeMouvementStock.SORTIE_VENTE, null, null, null);

        assertThat(mouvements).extracting(MouvementStock::getLot).containsExactly(lot, null);
        assertThat(mouvements).extracting(MouvementStock::getQuantite).containsExactly(-3, -2);
        assertThat(fiche.getQuantiteStock()).isEqualTo(5);
    }

    @Test
    @DisplayName("un stock insuffisant donne 409 sans rien modifier")
    void stockInsuffisant() {
        DisponibiliteProduit fiche = fiche(2);
        Lot lot = lot(1, 2);
        lots(2, lot);

        assertThatThrownBy(() -> service.sortir(PHARMACOPEE_ID, FICHE_ID, 3, TypeMouvementStock.SORTIE_VENTE, null, null, null))
                .isInstanceOf(ConflictException.class);
        assertThat(fiche.getQuantiteStock()).isEqualTo(2);
        assertThat(lot.getQuantiteRestante()).isEqualTo(2);
    }

    private MouvementStock mouvement(DisponibiliteProduit fiche, Lot lot, int quantite) {
        return MouvementStock.builder().pharmacopee(pharmacopee).disponibiliteProduit(fiche).lot(lot).quantite(quantite).build();
    }

    @Test
    @DisplayName("annuler une sortie remet les quantités dans le même lot")
    void annulationSortie() {
        DisponibiliteProduit fiche = fiche(5);
        Lot lot = lot(1, 0);

        List<MouvementStock> inverses = service.annuler(List.of(mouvement(fiche, lot, -3)), TypeMouvementStock.ANNULATION_VENTE, "Erreur", null);

        assertThat(inverses.get(0).getQuantite()).isEqualTo(3);
        assertThat(lot.getQuantiteRestante()).isEqualTo(3);
        assertThat(fiche.getQuantiteStock()).isEqualTo(8);
    }

    @Test
    @DisplayName("annuler une entrée déjà consommée est refusé")
    void annulationEntreeConsommee() {
        DisponibiliteProduit fiche = fiche(2);

        assertThatThrownBy(() -> service.annuler(List.of(mouvement(fiche, null, 5)), TypeMouvementStock.ANNULATION_ACHAT, "Erreur", null))
                .isInstanceOf(ConflictException.class);
        assertThat(fiche.getQuantiteStock()).isEqualTo(2);
    }

    @Test
    @DisplayName("annuler une entrée dont le lot a été entamé est refusé")
    void annulationLotConsomme() {
        DisponibiliteProduit fiche = fiche(20);
        Lot lot = lot(1, 2);

        assertThatThrownBy(() -> service.annuler(List.of(mouvement(fiche, lot, 5)), TypeMouvementStock.ANNULATION_ACHAT, "Erreur", null))
                .isInstanceOf(ConflictException.class);
    }
}
