package com.pharmacopee.ladafura.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.pharmacopee.ladafura.Models.Caisse;
import com.pharmacopee.ladafura.Models.Commande;
import com.pharmacopee.ladafura.Models.DisponibiliteProduit;
import com.pharmacopee.ladafura.Models.LigneCommande;
import com.pharmacopee.ladafura.Models.MouvementStock;
import com.pharmacopee.ladafura.Models.OperationCaisse;
import com.pharmacopee.ladafura.Models.Paiement;
import com.pharmacopee.ladafura.Models.Pharmacopee;
import com.pharmacopee.ladafura.Models.Produit;
import com.pharmacopee.ladafura.Models.SessionCaisse;
import com.pharmacopee.ladafura.enums.MethodePaiement;
import com.pharmacopee.ladafura.enums.NatureOperationCaisse;
import com.pharmacopee.ladafura.enums.StatutPaiement;
import com.pharmacopee.ladafura.enums.StatutSessionCaisse;
import com.pharmacopee.ladafura.enums.TypeCaisse;
import com.pharmacopee.ladafura.enums.TypeMouvementStock;
import com.pharmacopee.ladafura.exceptions.BadRequestException;
import com.pharmacopee.ladafura.exceptions.ConflictException;
import com.pharmacopee.ladafura.repository.CaisseRepository;
import com.pharmacopee.ladafura.repository.DisponibiliteProduitRepository;
import com.pharmacopee.ladafura.repository.MouvementStockRepository;
import com.pharmacopee.ladafura.repository.OperationCaisseRepository;
import com.pharmacopee.ladafura.repository.SessionCaisseRepository;
import com.pharmacopee.ladafura.services.impl.CommandeStockCaisseServiceImpl;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeMouvementStockService;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeOperationCaisseService;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeOperationCaisseService.NouvelleOperation;

@ExtendWith(MockitoExtension.class)
class CommandeStockCaisseServiceImplTest {

    private static final Long PHARMACOPEE_ID = 1L;

    @Mock private DisponibiliteProduitRepository disponibiliteProduitRepository;
    @Mock private MouvementStockRepository mouvementStockRepository;
    @Mock private OperationCaisseRepository operationCaisseRepository;
    @Mock private CaisseRepository caisseRepository;
    @Mock private SessionCaisseRepository sessionCaisseRepository;
    @Mock private IPharmacopeeMouvementStockService mouvementStockService;
    @Mock private IPharmacopeeOperationCaisseService operationCaisseService;

    @InjectMocks private CommandeStockCaisseServiceImpl service;

    private final Pharmacopee pharmacopee = Pharmacopee.builder().id(PHARMACOPEE_ID).build();
    private final DisponibiliteProduit fiche = DisponibiliteProduit.builder().id(50L).quantiteStock(0).disponible(true).build();
    private LigneCommande ligne;
    private Commande commande;
    private Paiement paiement;

    @BeforeEach
    void preparer() {
        ligne = LigneCommande.builder().id(9L).produit(Produit.builder().id(5L).build()).quantite(2).build();
        commande = Commande.builder().id(100L).numero("CMD-1").pharmacopee(pharmacopee).build();
        commande.setLignes(List.of(ligne));
        paiement = Paiement.builder().id(70L).commande(commande).montant(6500.0).statut(StatutPaiement.REUSSI).methode(MethodePaiement.MOBILE_MONEY).build();
        commande.setPaiement(paiement);
        lenient().when(disponibiliteProduitRepository.findByPharmacopeeIdAndProduitId(PHARMACOPEE_ID, 5L)).thenReturn(Optional.of(fiche));
        lenient().when(operationCaisseRepository.findFirstByPaiementIdAndNature(70L, NatureOperationCaisse.PAIEMENT_COMMANDE)).thenReturn(Optional.empty());
    }

    private static Caisse caisse(Long id, TypeCaisse type) {
        return Caisse.builder().id(id).type(type).actif(true).build();
    }

    @Test
    @DisplayName("la commande sort du stock sans auteur et la rupture rend le produit indisponible")
    void sortieDeStock() {
        service.sortirStock(commande);

        verify(mouvementStockService).sortir(eq(PHARMACOPEE_ID), eq(50L), eq(2), eq(TypeMouvementStock.SORTIE_COMMANDE), any(), any(), isNull());
        assertThat(fiche.getDisponible()).isFalse();
    }

    @Test
    @DisplayName("l'annulation annule les mouvements de la commande")
    void reintegrationAvecMouvements() {
        List<MouvementStock> mouvements = List.of(MouvementStock.builder().id(1L).build());
        when(mouvementStockRepository.findByLigneCommandeId(9L)).thenReturn(mouvements);

        service.reintegrerStock(commande, null);

        verify(mouvementStockService).annuler(eq(mouvements), eq(TypeMouvementStock.ANNULATION_COMMANDE), any(), isNull());
        verify(mouvementStockService, never()).entrer(anyLong(), anyLong(), anyInt(), any(), any(), any(), any(), any());
        assertThat(fiche.getDisponible()).isTrue();
    }

    @Test
    @DisplayName("une commande antérieure aux mouvements est réintégrée par une simple entrée")
    void reintegrationSansMouvement() {
        when(mouvementStockRepository.findByLigneCommandeId(9L)).thenReturn(List.of());

        service.reintegrerStock(commande, null);

        verify(mouvementStockService).entrer(eq(PHARMACOPEE_ID), eq(50L), eq(2), eq(TypeMouvementStock.ANNULATION_COMMANDE), isNull(), any(), any(), isNull());
    }

    @Test
    @DisplayName("un paiement Mobile Money entre sur le compte Mobile Money actif")
    void encaissementMobileMoney() {
        when(caisseRepository.findFirstByPharmacopeeIdAndTypeAndActifTrue(PHARMACOPEE_ID, TypeCaisse.MOBILE_MONEY))
                .thenReturn(Optional.of(caisse(3L, TypeCaisse.MOBILE_MONEY)));

        service.encaisser(paiement, null, null);

        ArgumentCaptor<NouvelleOperation> operation = ArgumentCaptor.forClass(NouvelleOperation.class);
        verify(operationCaisseService).enregistrer(eq(PHARMACOPEE_ID), operation.capture());
        assertThat(operation.getValue().caisseId()).isEqualTo(3L);
        assertThat(operation.getValue().nature()).isEqualTo(NatureOperationCaisse.PAIEMENT_COMMANDE);
        assertThat(operation.getValue().montant()).isEqualByComparingTo("6500");
    }

    @Test
    @DisplayName("sans compte actif du mode de paiement, le paiement est refusé")
    void compteAbsent() {
        when(caisseRepository.findFirstByPharmacopeeIdAndTypeAndActifTrue(PHARMACOPEE_ID, TypeCaisse.MOBILE_MONEY)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.encaisser(paiement, null, null)).isInstanceOf(ConflictException.class);
    }

    @Test
    @DisplayName("un paiement ne peut pas être encaissé deux fois")
    void doubleEncaissement() {
        when(operationCaisseRepository.findFirstByPaiementIdAndNature(70L, NatureOperationCaisse.PAIEMENT_COMMANDE))
                .thenReturn(Optional.of(OperationCaisse.builder().id(1L).build()));

        assertThatThrownBy(() -> service.encaisser(paiement, null, null)).isInstanceOf(ConflictException.class);
        verify(operationCaisseService, never()).enregistrer(anyLong(), any());
    }

    @Test
    @DisplayName("espèces sans caisse indiquée : l'unique session ouverte est retenue")
    void especesUneSession() {
        paiement.setMethode(MethodePaiement.CASH);
        when(caisseRepository.findByPharmacopeeIdAndActifTrue(PHARMACOPEE_ID))
                .thenReturn(List.of(caisse(1L, TypeCaisse.PHYSIQUE), caisse(2L, TypeCaisse.PHYSIQUE), caisse(3L, TypeCaisse.MOBILE_MONEY)));
        when(sessionCaisseRepository.findFirstByCaisseIdAndStatut(1L, StatutSessionCaisse.OUVERTE)).thenReturn(Optional.empty());
        when(sessionCaisseRepository.findFirstByCaisseIdAndStatut(2L, StatutSessionCaisse.OUVERTE)).thenReturn(Optional.of(new SessionCaisse()));

        service.encaisser(paiement, null, null);

        ArgumentCaptor<NouvelleOperation> operation = ArgumentCaptor.forClass(NouvelleOperation.class);
        verify(operationCaisseService).enregistrer(eq(PHARMACOPEE_ID), operation.capture());
        assertThat(operation.getValue().caisseId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("espèces sans session ouverte : 409")
    void especesAucuneSession() {
        paiement.setMethode(MethodePaiement.CASH);
        when(caisseRepository.findByPharmacopeeIdAndActifTrue(PHARMACOPEE_ID)).thenReturn(List.of(caisse(1L, TypeCaisse.PHYSIQUE)));
        when(sessionCaisseRepository.findFirstByCaisseIdAndStatut(1L, StatutSessionCaisse.OUVERTE)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.encaisser(paiement, null, null)).isInstanceOf(ConflictException.class);
    }

    @Test
    @DisplayName("espèces avec plusieurs sessions ouvertes : la caisse doit être indiquée")
    void especesPlusieursSessions() {
        paiement.setMethode(MethodePaiement.CASH);
        when(caisseRepository.findByPharmacopeeIdAndActifTrue(PHARMACOPEE_ID))
                .thenReturn(List.of(caisse(1L, TypeCaisse.PHYSIQUE), caisse(2L, TypeCaisse.PHYSIQUE)));
        when(sessionCaisseRepository.findFirstByCaisseIdAndStatut(anyLong(), eq(StatutSessionCaisse.OUVERTE))).thenReturn(Optional.of(new SessionCaisse()));

        assertThatThrownBy(() -> service.encaisser(paiement, null, null)).isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("l'annulation contre-passe l'encaissement et passe le paiement à ANNULE")
    void annulationEncaissement() {
        when(operationCaisseRepository.findFirstByPaiementIdAndNature(70L, NatureOperationCaisse.PAIEMENT_COMMANDE))
                .thenReturn(Optional.of(OperationCaisse.builder().id(8L).build()));

        service.annulerEncaissement(commande, null);

        verify(operationCaisseService).contrePasser(eq(PHARMACOPEE_ID), eq(8L), any(), isNull());
        assertThat(paiement.getStatut()).isEqualTo(StatutPaiement.ANNULE);
    }

    @Test
    @DisplayName("un paiement en attente est annulé sans opération de caisse")
    void annulationSansEncaissement() {
        paiement.setStatut(StatutPaiement.EN_ATTENTE);

        service.annulerEncaissement(commande, null);

        verify(operationCaisseService, never()).contrePasser(anyLong(), anyLong(), any(), any());
        assertThat(paiement.getStatut()).isEqualTo(StatutPaiement.ANNULE);
    }
}
