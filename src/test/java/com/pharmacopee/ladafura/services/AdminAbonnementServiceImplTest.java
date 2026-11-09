package com.pharmacopee.ladafura.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Year;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.pharmacopee.ladafura.Models.Abonnement;
import com.pharmacopee.ladafura.Models.FactureAbonnement;
import com.pharmacopee.ladafura.Models.PaiementAbonnement;
import com.pharmacopee.ladafura.Models.ParametrePlateforme;
import com.pharmacopee.ladafura.Models.Pharmacopee;
import com.pharmacopee.ladafura.Models.Utilisateur;
import com.pharmacopee.ladafura.dto.pharmacopee.abonnement.FactureAbonnementResponse;
import com.pharmacopee.ladafura.enums.ModePaiementAbonnement;
import com.pharmacopee.ladafura.enums.StatutAbonnement;
import com.pharmacopee.ladafura.enums.StatutFacture;
import com.pharmacopee.ladafura.enums.StatutPaiement;
import com.pharmacopee.ladafura.exceptions.BadRequestException;
import com.pharmacopee.ladafura.exceptions.ConflictException;
import com.pharmacopee.ladafura.repository.AbonnementRepository;
import com.pharmacopee.ladafura.repository.FactureAbonnementRepository;
import com.pharmacopee.ladafura.repository.HistoriqueAbonnementRepository;
import com.pharmacopee.ladafura.repository.PaiementAbonnementRepository;
import com.pharmacopee.ladafura.repository.ParametrePlateformeRepository;
import com.pharmacopee.ladafura.services.impl.AdminAbonnementServiceImpl;
import com.pharmacopee.ladafura.services.interfaces.IAbonnementCycleService;
import com.pharmacopee.ladafura.services.interfaces.IJournalAuditService;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeAuthService;

@ExtendWith(MockitoExtension.class)
class AdminAbonnementServiceImplTest {

    @Mock private AbonnementRepository abonnementRepository;
    @Mock private PaiementAbonnementRepository paiementRepository;
    @Mock private FactureAbonnementRepository factureRepository;
    @Mock private HistoriqueAbonnementRepository historiqueRepository;
    @Mock private ParametrePlateformeRepository parametreRepository;
    @Mock private IAbonnementCycleService cycleService;
    @Mock private IJournalAuditService journalAuditService;
    @Mock private IPharmacopeeAuthService pharmacopeeAuthService;

    @InjectMocks private AdminAbonnementServiceImpl service;

    private final Pharmacopee pharmacopee = Pharmacopee.builder().id(5L).nom("Officine").build();
    private final Utilisateur admin = new Utilisateur();

    @BeforeEach
    void preparer() {
        lenient().when(pharmacopeeAuthService.getCurrentUtilisateur()).thenReturn(admin);
        lenient().when(factureRepository.save(any(FactureAbonnement.class))).thenAnswer(i -> i.getArgument(0));
    }

    private PaiementAbonnement paiement(String montant, String reference) {
        Abonnement abonnement = Abonnement.builder().id(50L).pharmacopee(pharmacopee).statut(StatutAbonnement.EN_ATTENTE_PAIEMENT)
                .codeOffreSouscrite("MENSUELLE").prixSouscrit(new BigDecimal("11800")).build();
        PaiementAbonnement p = PaiementAbonnement.builder().id(9L).abonnement(abonnement).montant(new BigDecimal(montant)).devise("XOF")
                .modePaiement(ModePaiementAbonnement.WAVE).referenceTransaction(reference).statut(StatutPaiement.EN_ATTENTE).reference("PAB-1").build();
        when(paiementRepository.findById(9L)).thenReturn(Optional.of(p));
        return p;
    }

    private void taux(String valeur) {
        when(parametreRepository.findByCode("TAUX_TAXE_ABONNEMENT")).thenReturn(Optional.of(ParametrePlateforme.builder().valeur(valeur).build()));
    }

    private void aucuneFacture() {
        when(factureRepository.findFirstByNumeroFactureStartingWithOrderByNumeroFactureDesc(any())).thenReturn(Optional.empty());
    }

    @Test
    @DisplayName("confirmation : active l'abonnement et émet une facture payée")
    void confirmation() {
        PaiementAbonnement p = paiement("11800", "W1");
        taux("0");
        aucuneFacture();

        FactureAbonnementResponse facture = service.confirmerPaiement(9L);

        assertThat(p.getStatut()).isEqualTo(StatutPaiement.REUSSI);
        verify(cycleService).activerApresPaiement(p.getAbonnement(), admin);
        assertThat(facture.getStatut()).isEqualTo(StatutFacture.PAYEE);
        assertThat(facture.getNumeroFacture()).isEqualTo("LDF-" + Year.now().getValue() + "-000001");
        assertThat(facture.getMontantTTC()).isEqualByComparingTo("11800");
    }

    @Test
    @DisplayName("avec une taxe de 18 %, le montant payé est le TTC")
    void factureAvecTaxe() {
        paiement("11800", "W1");
        taux("18");
        aucuneFacture();

        FactureAbonnementResponse facture = service.confirmerPaiement(9L);

        assertThat(facture.getMontantHT()).isEqualByComparingTo("10000");
        assertThat(facture.getMontantTaxe()).isEqualByComparingTo("1800");
        assertThat(facture.getMontantTTC()).isEqualByComparingTo("11800");
    }

    @Test
    @DisplayName("la numérotation suit la dernière facture de l'année")
    void numerotation() {
        paiement("11800", "W1");
        taux("0");
        String prefixe = "LDF-" + Year.now().getValue() + "-";
        when(factureRepository.findFirstByNumeroFactureStartingWithOrderByNumeroFactureDesc(prefixe))
                .thenReturn(Optional.of(FactureAbonnement.builder().numeroFacture(prefixe + "000041").build()));

        assertThat(service.confirmerPaiement(9L).getNumeroFacture()).isEqualTo(prefixe + "000042");
    }

    @Test
    @DisplayName("un montant différent du prix est refusé")
    void montantIncorrect() {
        paiement("100", "W1");

        assertThatThrownBy(() -> service.confirmerPaiement(9L)).isInstanceOf(BadRequestException.class);
        verify(cycleService, never()).activerApresPaiement(any(), any());
    }

    @Test
    @DisplayName("une référence de transaction déjà confirmée est refusée")
    void referenceReutilisee() {
        paiement("11800", "W1");
        when(paiementRepository.existsByModePaiementAndReferenceTransactionAndStatut(ModePaiementAbonnement.WAVE, "W1", StatutPaiement.REUSSI)).thenReturn(true);

        assertThatThrownBy(() -> service.confirmerPaiement(9L)).isInstanceOf(ConflictException.class);
    }

    @Test
    @DisplayName("un deuxième paiement confirmé pour un abonnement est refusé")
    void deuxiemeConfirmation() {
        paiement("11800", "W1");
        when(paiementRepository.existsByAbonnementIdAndStatut(50L, StatutPaiement.REUSSI)).thenReturn(true);

        assertThatThrownBy(() -> service.confirmerPaiement(9L)).isInstanceOf(ConflictException.class);
    }

    @Test
    @DisplayName("un paiement déjà traité ne se confirme plus")
    void paiementDejaTraite() {
        paiement("11800", "W1").setStatut(StatutPaiement.ECHOUE);

        assertThatThrownBy(() -> service.confirmerPaiement(9L)).isInstanceOf(ConflictException.class);
    }

    @Test
    @DisplayName("rejet : statut ECHOUE avec motif, l'abonnement reste en attente")
    void rejet() {
        PaiementAbonnement p = paiement("11800", "W1");

        service.rejeterPaiement(9L, "Transaction introuvable");

        assertThat(p.getStatut()).isEqualTo(StatutPaiement.ECHOUE);
        assertThat(p.getMotifRejet()).isEqualTo("Transaction introuvable");
        assertThat(p.getAbonnement().getStatut()).isEqualTo(StatutAbonnement.EN_ATTENTE_PAIEMENT);
    }

    private Abonnement abonnement(StatutAbonnement statut, String code) {
        Abonnement a = Abonnement.builder().id(50L).pharmacopee(pharmacopee).statut(statut).codeOffreSouscrite(code).build();
        when(abonnementRepository.findById(50L)).thenReturn(Optional.of(a));
        return a;
    }

    @Test
    @DisplayName("annuler un abonnement en vigueur ramène à l'offre gratuite")
    void annulationEnVigueur() {
        Abonnement a = abonnement(StatutAbonnement.ACTIF, "MENSUELLE");

        service.annuler(50L, "Fraude");

        assertThat(a.getStatut()).isEqualTo(StatutAbonnement.ANNULE);
        verify(cycleService).retournerAuGratuit(pharmacopee, a, admin);
    }

    @Test
    @DisplayName("annuler un abonnement programmé ne crée pas d'offre gratuite")
    void annulationProgramme() {
        abonnement(StatutAbonnement.PROGRAMME, "MENSUELLE");

        service.annuler(50L, "Erreur");

        verify(cycleService, never()).retournerAuGratuit(any(), any(), any());
    }

    @Test
    @DisplayName("l'offre gratuite ne peut pas être annulée")
    void annulationGratuit() {
        abonnement(StatutAbonnement.ACTIF, "GRATUITE");

        assertThatThrownBy(() -> service.annuler(50L, "x")).isInstanceOf(ConflictException.class);
    }

    @Test
    @DisplayName("suspension : uniquement un abonnement en vigueur")
    void suspension() {
        Abonnement a = abonnement(StatutAbonnement.ACTIF, "MENSUELLE");

        service.suspendre(50L, "Litige");

        assertThat(a.getStatut()).isEqualTo(StatutAbonnement.SUSPENDU);
        verify(cycleService).historiser(eq(a), any(), any(), any(), eq("Litige"), eq(admin));
    }

    @Test
    @DisplayName("réactivation : uniquement un abonnement suspendu")
    void reactivationRefusee() {
        abonnement(StatutAbonnement.ACTIF, "MENSUELLE");

        assertThatThrownBy(() -> service.reactiver(50L, "x")).isInstanceOf(ConflictException.class);
    }
}
