package com.pharmacopee.ladafura.services;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import com.pharmacopee.ladafura.dto.pharmacopee.stock.UpdateStockCompletRequest;
import com.pharmacopee.ladafura.dto.pharmacopee.stock.UpdateStockRequest;
import com.pharmacopee.ladafura.exceptions.ForbiddenException;
import com.pharmacopee.ladafura.repository.CommandeRepository;
import com.pharmacopee.ladafura.repository.DisponibiliteProduitRepository;
import com.pharmacopee.ladafura.services.impl.PharmacopeeCommandeServiceImpl;
import com.pharmacopee.ladafura.services.impl.PharmacopeeStockServiceImpl;
import com.pharmacopee.ladafura.services.interfaces.IJournalAuditService;
import com.pharmacopee.ladafura.services.interfaces.ICommandeStockCaisseService;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeContexteService;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeMouvementStockService;

@ExtendWith(MockitoExtension.class)
class PharmacopeePermissionsBasculeTest {

    @Mock private IPharmacopeeContexteService contexteService;
    @Mock private DisponibiliteProduitRepository disponibiliteProduitRepository;
    @Mock private IPharmacopeeMouvementStockService mouvementStockService;
    @Mock private IJournalAuditService journalAuditService;
    @Mock private CommandeRepository commandeRepository;
    @Mock private ICommandeStockCaisseService commandeStockCaisseService;

    @InjectMocks private PharmacopeeStockServiceImpl stockService;
    @InjectMocks private PharmacopeeCommandeServiceImpl commandeService;

    private static ForbiddenException refus() {
        return new ForbiddenException("Vous n'avez pas la permission d'effectuer cette opération.");
    }

    @Test
    @DisplayName("sans STOCK_AJUSTER, la quantité n'est pas modifiable")
    void stockAjusterRefuse() {
        when(contexteService.verifierOperationCommerciale(IPharmacopeeContexteService.STOCK_AJUSTER)).thenThrow(refus());

        assertThatThrownBy(() -> stockService.updateQuantiteStock(5L, UpdateStockRequest.builder().quantiteStock(3).build()))
                .isInstanceOf(ForbiddenException.class);
        verifyNoInteractions(disponibiliteProduitRepository, mouvementStockService);
    }

    @Test
    @DisplayName("sans STOCK_CONSULTER, le stock n'est pas lisible")
    void stockConsulterRefuse() {
        when(contexteService.verifierPermission(IPharmacopeeContexteService.STOCK_CONSULTER)).thenThrow(refus());

        assertThatThrownBy(() -> stockService.getStocks(null, null, PageRequest.of(0, 10))).isInstanceOf(ForbiddenException.class);
        verifyNoInteractions(disponibiliteProduitRepository);
    }

    @Test
    @DisplayName("modifier le prix et la quantité exige aussi STOCK_AJUSTER")
    void miseAJourCompleteVerifieLesDeuxPermissions() {
        when(contexteService.verifierOperationCommerciale(IPharmacopeeContexteService.PRODUITS_GERER)).thenReturn(null);
        when(contexteService.verifierPermission(IPharmacopeeContexteService.STOCK_AJUSTER)).thenThrow(refus());

        assertThatThrownBy(() -> stockService.updateStockComplet(5L, UpdateStockCompletRequest.builder().quantiteStock(3).prix(1500.0).build()))
                .isInstanceOf(ForbiddenException.class);
        verifyNoInteractions(mouvementStockService);
    }

    @Test
    @DisplayName("les commandes en cours se traitent sans contrôle d'abonnement, mais avec COMMANDES_TRAITER")
    void commandesTraiterRefuse() {
        when(contexteService.verifierPharmacopeeValidee(anyString())).thenThrow(refus());

        assertThatThrownBy(() -> commandeService.confirmerCommande(1L)).isInstanceOf(ForbiddenException.class);

        verify(contexteService).verifierPharmacopeeValidee(IPharmacopeeContexteService.COMMANDES_TRAITER);
        verifyNoInteractions(commandeRepository, commandeStockCaisseService);
    }
}
