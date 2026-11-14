package com.pharmacopee.ladafura.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.pharmacopee.ladafura.Models.DisponibiliteProduit;
import com.pharmacopee.ladafura.Models.Lot;
import com.pharmacopee.ladafura.Models.MembrePharmacopee;
import com.pharmacopee.ladafura.Models.MouvementStock;
import com.pharmacopee.ladafura.Models.Pharmacopee;
import com.pharmacopee.ladafura.Models.Produit;
import com.pharmacopee.ladafura.dto.pharmacopee.stock.LotStockResponse;
import com.pharmacopee.ladafura.enums.TypeMouvementStock;
import com.pharmacopee.ladafura.exceptions.ForbiddenException;
import com.pharmacopee.ladafura.repository.DisponibiliteProduitRepository;
import com.pharmacopee.ladafura.repository.LotRepository;
import com.pharmacopee.ladafura.repository.MouvementStockRepository;
import com.pharmacopee.ladafura.services.impl.PharmacopeeStockServiceImpl;
import com.pharmacopee.ladafura.services.interfaces.IJournalAuditService;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeContexteService;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeContexteService.ContexteMembre;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeMouvementStockService;

@ExtendWith(MockitoExtension.class)
class PharmacopeeStockLectureTest {

    private static final Long PHARMACOPEE_ID = 1L;
    private static final Pageable PAGE = PageRequest.of(0, 10);

    @Mock private IPharmacopeeContexteService contexteService;
    @Mock private DisponibiliteProduitRepository disponibiliteProduitRepository;
    @Mock private IPharmacopeeMouvementStockService mouvementStockService;
    @Mock private MouvementStockRepository mouvementStockRepository;
    @Mock private LotRepository lotRepository;
    @Mock private IJournalAuditService journalAuditService;

    @InjectMocks private PharmacopeeStockServiceImpl service;

    private final Produit produit = Produit.builder().id(5L).nom("Sirop de moringa").build();
    private final DisponibiliteProduit fiche = DisponibiliteProduit.builder().id(50L).produit(produit).build();

    private void pharmacopeeCourante() {
        when(contexteService.verifierPermission(IPharmacopeeContexteService.STOCK_CONSULTER))
                .thenReturn(new ContexteMembre(new MembrePharmacopee(), Pharmacopee.builder().id(PHARMACOPEE_ID).build(), java.util.Set.of()));
    }

    private Lot lotPerimantDans(long jours) {
        return Lot.builder().id(jours).disponibiliteProduit(fiche).numeroLot("L" + jours)
                .datePeremption(LocalDate.now().plusDays(jours)).quantiteInitiale(10).quantiteRestante(4).build();
    }

    @Test
    @DisplayName("GET /stock/mouvements - un mouvement en ligne n'a pas d'auteur")
    void mouvementSansAuteur() {
        pharmacopeeCourante();
        when(mouvementStockRepository.findByPharmacopeeIdOrderByDateMouvementDesc(PHARMACOPEE_ID, PAGE))
                .thenReturn(new PageImpl<>(List.of(MouvementStock.builder().id(1L).disponibiliteProduit(fiche)
                        .type(TypeMouvementStock.SORTIE_COMMANDE).quantite(-2).stockAvant(20).stockApres(18).build())));

        assertThat(service.getMouvements(PAGE).getContent()).singleElement().satisfies(m -> {
            assertThat(m.getProduitNom()).isEqualTo("Sirop de moringa");
            assertThat(m.getQuantite()).isEqualTo(-2);
            assertThat(m.getAuteur()).isNull();
        });
    }

    @Test
    @DisplayName("GET /stock/lots - signale les lots périmés et proches de la péremption")
    void etatDesLots() {
        pharmacopeeCourante();
        when(lotRepository.findLotsEnStock(PHARMACOPEE_ID, PAGE))
                .thenReturn(new PageImpl<>(List.of(lotPerimantDans(-3), lotPerimantDans(10), lotPerimantDans(200))));

        List<LotStockResponse> lots = service.getLots(PAGE).getContent();

        assertThat(lots).extracting(LotStockResponse::isPerime).containsExactly(true, false, false);
        assertThat(lots).extracting(LotStockResponse::isProchePeremption).containsExactly(false, true, false);
    }

    @Test
    @DisplayName("sans STOCK_CONSULTER, ni mouvements ni lots ne sont lus")
    void permissionManquante() {
        when(contexteService.verifierPermission(IPharmacopeeContexteService.STOCK_CONSULTER)).thenThrow(new ForbiddenException("Refusé"));

        assertThatThrownBy(() -> service.getMouvements(PAGE)).isInstanceOf(ForbiddenException.class);
        assertThatThrownBy(() -> service.getLots(PAGE)).isInstanceOf(ForbiddenException.class);
        verifyNoInteractions(mouvementStockRepository, lotRepository);
    }
}
