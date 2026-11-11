package com.pharmacopee.ladafura.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.pharmacopee.ladafura.Models.Caisse;
import com.pharmacopee.ladafura.Models.Commande;
import com.pharmacopee.ladafura.Models.MembrePharmacopee;
import com.pharmacopee.ladafura.Models.Paiement;
import com.pharmacopee.ladafura.Models.Pharmacopee;
import com.pharmacopee.ladafura.dto.pharmacopee.commande.UpdateStatutCommandeRequest;
import com.pharmacopee.ladafura.dto.pharmacopee.paiement.PharmacopeePaiementResponse;
import com.pharmacopee.ladafura.enums.MethodePaiement;
import com.pharmacopee.ladafura.enums.StatutCommande;
import com.pharmacopee.ladafura.enums.StatutPaiement;
import com.pharmacopee.ladafura.exceptions.BadRequestException;
import com.pharmacopee.ladafura.exceptions.ConflictException;
import com.pharmacopee.ladafura.exceptions.ResourceNotFoundException;
import com.pharmacopee.ladafura.repository.CommandeRepository;
import com.pharmacopee.ladafura.repository.PaiementRepository;
import com.pharmacopee.ladafura.services.impl.PharmacopeeCommandeServiceImpl;
import com.pharmacopee.ladafura.services.impl.PharmacopeePaiementServiceImpl;
import com.pharmacopee.ladafura.services.interfaces.ICommandeStockCaisseService;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeContexteService;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PharmacopeePaiementVerificationTest {

    private static final Long PHARMACOPEE_ID = 1L;
    private static final Long PAIEMENT_ID = 50L;
    private static final Long COMMANDE_ID = 7L;
    private static final Long COMPTE_ID = 12L;

    @Mock
    private IPharmacopeeContexteService contexteService;
    @Mock
    private ICommandeStockCaisseService commandeStockCaisseService;
    @Mock
    private PaiementRepository paiementRepository;
    @Mock
    private CommandeRepository commandeRepository;

    @InjectMocks
    private PharmacopeePaiementServiceImpl service;
    @InjectMocks
    private PharmacopeeCommandeServiceImpl commandeService;

    private MembrePharmacopee membre;
    private Commande commande;
    private Paiement paiement;

    @BeforeEach
    void setUp() {
        Pharmacopee pharmacopee = new Pharmacopee();
        pharmacopee.setId(PHARMACOPEE_ID);
        pharmacopee.setNom("Pharmacopée Mandé");
        membre = new MembrePharmacopee();
        var contexte = new IPharmacopeeContexteService.ContexteMembre(membre, pharmacopee, Set.of());
        when(contexteService.verifierPharmacopeeValidee(any())).thenReturn(contexte);

        Caisse compte = new Caisse();
        compte.setId(COMPTE_ID);
        commande = Commande.builder().id(COMMANDE_ID).numero("CMD-1").statut(StatutCommande.EN_ATTENTE)
                .pharmacopee(pharmacopee).montantTotal(6500.0).build();
        paiement = Paiement.builder().id(PAIEMENT_ID).methode(MethodePaiement.MOBILE_MONEY)
                .statut(StatutPaiement.EN_ATTENTE).montant(6500.0).reference("OM-1").commande(commande).build();
        paiement.setCaisse(compte);
        commande.setPaiement(paiement);

        when(paiementRepository.findByIdAndCommandePharmacopeeId(PAIEMENT_ID, PHARMACOPEE_ID)).thenReturn(Optional.of(paiement));
        when(paiementRepository.save(any(Paiement.class))).thenAnswer(inv -> inv.getArgument(0));
        when(commandeRepository.findByIdAndPharmacopeeId(COMMANDE_ID, PHARMACOPEE_ID)).thenReturn(Optional.of(commande));
    }

    @Test
    @DisplayName("confirmerReception - paiement réussi, compte choisi crédité, commande confirmée")
    void confirmer_Succes() {
        PharmacopeePaiementResponse reponse = service.confirmerReception(PAIEMENT_ID);

        assertThat(reponse.getStatut()).isEqualTo(StatutPaiement.REUSSI);
        assertThat(commande.getStatut()).isEqualTo(StatutCommande.CONFIRMEE);
        verify(commandeStockCaisseService).encaisser(paiement, COMPTE_ID, membre);
    }

    @Test
    @DisplayName("confirmerReception - paiement déjà traité : pas de second encaissement (409)")
    void confirmer_DejaTraite() {
        paiement.setStatut(StatutPaiement.REUSSI);

        assertThatThrownBy(() -> service.confirmerReception(PAIEMENT_ID)).isInstanceOf(ConflictException.class);
        verify(commandeStockCaisseService, never()).encaisser(any(), any(), any());
    }

    @Test
    @DisplayName("confirmerReception - commande annulée -> 409, rien n'est encaissé")
    void confirmer_CommandeAnnulee() {
        commande.setStatut(StatutCommande.ANNULEE);

        assertThatThrownBy(() -> service.confirmerReception(PAIEMENT_ID)).isInstanceOf(ConflictException.class);
        verify(commandeStockCaisseService, never()).encaisser(any(), any(), any());
    }

    @Test
    @DisplayName("confirmerReception - paiement en espèces -> 400")
    void confirmer_MauvaisType() {
        paiement.setMethode(MethodePaiement.CASH);

        assertThatThrownBy(() -> service.confirmerReception(PAIEMENT_ID)).isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("confirmerReception - paiement d'une autre pharmacopée -> 404")
    void confirmer_AutreTenant() {
        when(paiementRepository.findByIdAndCommandePharmacopeeId(eq(99L), any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.confirmerReception(99L)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("rejeterPaiement - paiement ECHOUE avec motif, commande en attente, rien n'est encaissé")
    void rejeter_Succes() {
        PharmacopeePaiementResponse reponse = service.rejeterPaiement(PAIEMENT_ID, " Aucun paiement reçu ");

        assertThat(reponse.getStatut()).isEqualTo(StatutPaiement.ECHOUE);
        assertThat(reponse.getMotifRejet()).isEqualTo("Aucun paiement reçu");
        assertThat(commande.getStatut()).isEqualTo(StatutCommande.EN_ATTENTE);
        verify(commandeStockCaisseService, never()).encaisser(any(), any(), any());
    }

    @Test
    @DisplayName("updateStatut - une commande au Mobile Money non vérifié ne peut pas être confirmée (409)")
    void commande_NonConfirmableAvantPaiement() {
        var demande = UpdateStatutCommandeRequest.builder().nouveauStatut(StatutCommande.CONFIRMEE).build();

        assertThatThrownBy(() -> commandeService.updateStatut(COMMANDE_ID, demande)).isInstanceOf(ConflictException.class);
        assertThat(commande.getStatut()).isEqualTo(StatutCommande.EN_ATTENTE);
    }
}
