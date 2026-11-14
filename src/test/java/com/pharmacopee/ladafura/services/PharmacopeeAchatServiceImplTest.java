package com.pharmacopee.ladafura.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Year;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.pharmacopee.ladafura.Models.Achat;
import com.pharmacopee.ladafura.Models.DisponibiliteProduit;
import com.pharmacopee.ladafura.Models.Fournisseur;
import com.pharmacopee.ladafura.Models.LigneAchat;
import com.pharmacopee.ladafura.Models.Lot;
import com.pharmacopee.ladafura.Models.MembrePharmacopee;
import com.pharmacopee.ladafura.Models.MouvementStock;
import com.pharmacopee.ladafura.Models.OperationCaisse;
import com.pharmacopee.ladafura.Models.Pharmacopee;
import com.pharmacopee.ladafura.Models.Produit;
import com.pharmacopee.ladafura.Models.ReglementAchat;
import com.pharmacopee.ladafura.dto.pharmacopee.achat.AchatResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.achat.EnregistrerAchatRequest;
import com.pharmacopee.ladafura.dto.pharmacopee.achat.EnregistrerReglementAchatRequest;
import com.pharmacopee.ladafura.dto.pharmacopee.achat.LigneAchatRequest;
import com.pharmacopee.ladafura.enums.MethodePaiement;
import com.pharmacopee.ladafura.enums.NatureOperationCaisse;
import com.pharmacopee.ladafura.enums.SensOperation;
import com.pharmacopee.ladafura.enums.StatutAchat;
import com.pharmacopee.ladafura.enums.StatutEcriture;
import com.pharmacopee.ladafura.enums.StatutFournisseur;
import com.pharmacopee.ladafura.enums.StatutProduit;
import com.pharmacopee.ladafura.enums.TypeMouvementStock;
import com.pharmacopee.ladafura.exceptions.BadRequestException;
import com.pharmacopee.ladafura.exceptions.ConflictException;
import com.pharmacopee.ladafura.exceptions.QuotaExceededException;
import com.pharmacopee.ladafura.exceptions.ResourceNotFoundException;
import com.pharmacopee.ladafura.repository.AchatRepository;
import com.pharmacopee.ladafura.repository.DisponibiliteProduitRepository;
import com.pharmacopee.ladafura.repository.FournisseurRepository;
import com.pharmacopee.ladafura.repository.LotRepository;
import com.pharmacopee.ladafura.repository.MouvementStockRepository;
import com.pharmacopee.ladafura.repository.OperationCaisseRepository;
import com.pharmacopee.ladafura.repository.ProduitRepository;
import com.pharmacopee.ladafura.repository.ReglementAchatRepository;
import com.pharmacopee.ladafura.services.impl.PharmacopeeAchatServiceImpl;
import com.pharmacopee.ladafura.services.interfaces.IJournalAuditService;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeContexteService;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeContexteService.ContexteMembre;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeMouvementStockService;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeOperationCaisseService;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeQuotaService;

@ExtendWith(MockitoExtension.class)
class PharmacopeeAchatServiceImplTest {

    private static final Long PHARMACOPEE_ID = 5L;
    private static final Long PRODUIT_ID = 7L;

    @Mock private IPharmacopeeContexteService contexteService;
    @Mock private IPharmacopeeQuotaService quotaService;
    @Mock private IPharmacopeeMouvementStockService mouvementStockService;
    @Mock private IPharmacopeeOperationCaisseService operationCaisseService;
    @Mock private IJournalAuditService journalAuditService;
    @Mock private AchatRepository achatRepository;
    @Mock private FournisseurRepository fournisseurRepository;
    @Mock private ProduitRepository produitRepository;
    @Mock private DisponibiliteProduitRepository disponibiliteRepository;
    @Mock private LotRepository lotRepository;
    @Mock private MouvementStockRepository mouvementStockRepository;
    @Mock private ReglementAchatRepository reglementRepository;
    @Mock private OperationCaisseRepository operationCaisseRepository;

    @InjectMocks private PharmacopeeAchatServiceImpl service;

    private final Pharmacopee pharmacopee = Pharmacopee.builder().id(PHARMACOPEE_ID).nom("Officine").build();
    private final MembrePharmacopee membre = MembrePharmacopee.builder().id(1L).build();
    private final Produit produit = Produit.builder().id(PRODUIT_ID).nom("Tisane").statut(StatutProduit.VALIDE).build();

    @BeforeEach
    void preparer() {
        ContexteMembre contexte = new ContexteMembre(membre, pharmacopee, Set.of());
        lenient().when(contexteService.verifierPermission(anyString())).thenReturn(contexte);
        lenient().when(contexteService.verifierOperationCommerciale(anyString())).thenReturn(contexte);
        lenient().when(achatRepository.saveAndFlush(any(Achat.class))).thenAnswer(i -> i.getArgument(0));
        lenient().when(reglementRepository.save(any(ReglementAchat.class))).thenAnswer(i -> i.getArgument(0));
        lenient().when(reglementRepository.sumMontantValide(any())).thenReturn(BigDecimal.ZERO);
        lenient().when(produitRepository.findById(PRODUIT_ID)).thenReturn(Optional.of(produit));
        lenient().when(achatRepository.findFirstByPharmacopeeIdAndNumeroAchatStartingWithOrderByNumeroAchatDesc(any(), any())).thenReturn(Optional.empty());
    }

    private static LigneAchatRequest ligne(int quantite, String prix, String remise) {
        return LigneAchatRequest.builder().produitId(PRODUIT_ID).quantite(quantite).prixUnitaire(new BigDecimal(prix))
                .remise(remise != null ? new BigDecimal(remise) : null).build();
    }

    private static EnregistrerAchatRequest brouillon(LigneAchatRequest... lignes) {
        return EnregistrerAchatRequest.builder().lignes(List.of(lignes)).build();
    }

    private Achat achat(StatutAchat statut) {
        Achat a = Achat.builder().id(50L).pharmacopee(pharmacopee).numeroAchat("ACH-2026-000001").statut(statut)
                .montantTotal(new BigDecimal("15000")).montantRemise(BigDecimal.ZERO).build();
        lenient().when(achatRepository.findByIdAndPharmacopeeId(50L, PHARMACOPEE_ID)).thenReturn(Optional.of(a));
        return a;
    }

    private LigneAchat ajouterLigne(Achat a, String numeroLot, LocalDate peremption) {
        LigneAchat l = LigneAchat.builder().id(70L).produit(produit).quantite(10).prixUnitaire(new BigDecimal("1500"))
                .remise(BigDecimal.ZERO).sousTotal(new BigDecimal("15000")).numeroLot(numeroLot).datePeremption(peremption).build();
        a.ajouterLigne(l);
        return l;
    }

    @Test
    @DisplayName("création sans fournisseur : brouillon numéroté, total calculé, aucun mouvement de stock")
    void creationSansFournisseur() {
        AchatResponse reponse = service.creer(brouillon(ligne(10, "1500", "1000")));

        assertThat(reponse.getNumeroAchat()).isEqualTo("ACH-" + Year.now().getValue() + "-000001");
        assertThat(reponse.getFournisseurId()).isNull();
        assertThat(reponse.getMontantTotal()).isEqualByComparingTo("14000");
        verify(mouvementStockService, never()).entrer(any(), any(), anyInt(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("la numérotation suit le dernier achat de la pharmacopée pour l'année")
    void numerotation() {
        String prefixe = "ACH-" + Year.now().getValue() + "-";
        when(achatRepository.findFirstByPharmacopeeIdAndNumeroAchatStartingWithOrderByNumeroAchatDesc(PHARMACOPEE_ID, prefixe))
                .thenReturn(Optional.of(Achat.builder().numeroAchat(prefixe + "000009").build()));

        assertThat(service.creer(brouillon()).getNumeroAchat()).isEqualTo(prefixe + "000010");
    }

    @Test
    @DisplayName("un fournisseur inactif ne peut pas être choisi")
    void fournisseurInactif() {
        when(fournisseurRepository.findByIdAndPharmacopeeId(3L, PHARMACOPEE_ID))
                .thenReturn(Optional.of(Fournisseur.builder().id(3L).statut(StatutFournisseur.INACTIF).build()));
        EnregistrerAchatRequest request = EnregistrerAchatRequest.builder().fournisseurId(3L).build();

        assertThatThrownBy(() -> service.creer(request)).isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("le fournisseur d'une autre pharmacopée n'est pas trouvé")
    void fournisseurAutrePharmacopee() {
        when(fournisseurRepository.findByIdAndPharmacopeeId(3L, PHARMACOPEE_ID)).thenReturn(Optional.empty());
        EnregistrerAchatRequest request = EnregistrerAchatRequest.builder().fournisseurId(3L).build();

        assertThatThrownBy(() -> service.creer(request)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("un produit non validé au catalogue est refusé")
    void produitNonValide() {
        produit.setStatut(StatutProduit.BROUILLON);

        assertThatThrownBy(() -> service.creer(brouillon(ligne(1, "100", null)))).isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("une remise supérieure au montant est refusée")
    void remiseExcessive() {
        assertThatThrownBy(() -> service.creer(brouillon(ligne(1, "100", "200")))).isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("un achat validé n'est plus modifiable ni supprimable")
    void achatValideImmuable() {
        achat(StatutAchat.VALIDE);

        assertThatThrownBy(() -> service.modifier(50L, brouillon())).isInstanceOf(ConflictException.class);
        assertThatThrownBy(() -> service.supprimer(50L)).isInstanceOf(ConflictException.class);
    }

    @Test
    @DisplayName("l'achat d'une autre pharmacopée n'est pas trouvé")
    void achatAutrePharmacopee() {
        when(achatRepository.findByIdAndPharmacopeeId(99L, PHARMACOPEE_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.valider(99L)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("un achat sans ligne ne se valide pas")
    void validationSansLigne() {
        achat(StatutAchat.BROUILLON);

        assertThatThrownBy(() -> service.valider(50L)).isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("validation sur une fiche existante : entrée en stock, sans contrôle de quota")
    void validationFicheExistante() {
        Achat a = achat(StatutAchat.BROUILLON);
        ajouterLigne(a, null, null);
        DisponibiliteProduit fiche = DisponibiliteProduit.builder().id(8L).pharmacopee(pharmacopee).produit(produit).build();
        when(disponibiliteRepository.findByPharmacopeeIdAndProduitIdForUpdate(PHARMACOPEE_ID, PRODUIT_ID)).thenReturn(Optional.of(fiche));

        service.valider(50L);

        assertThat(a.estValide()).isTrue();
        assertThat(a.getValidePar()).isSameAs(membre);
        verify(mouvementStockService).entrer(eq(PHARMACOPEE_ID), eq(8L), eq(10), eq(TypeMouvementStock.ENTREE_ACHAT), eq(null), any(), any(), eq(membre));
        verify(quotaService, never()).verifierQuota(any(), any());
        verify(lotRepository, never()).save(any());
    }

    @Test
    @DisplayName("nouveau produit : fiche créée non visible en vente, après contrôle du quota")
    void validationNouvelleFiche() {
        Achat a = achat(StatutAchat.BROUILLON);
        ajouterLigne(a, null, null);
        when(disponibiliteRepository.findByPharmacopeeIdAndProduitIdForUpdate(PHARMACOPEE_ID, PRODUIT_ID)).thenReturn(Optional.empty());
        when(disponibiliteRepository.saveAndFlush(any(DisponibiliteProduit.class))).thenAnswer(i -> {
            DisponibiliteProduit d = i.getArgument(0);
            d.setId(8L);
            return d;
        });

        service.valider(50L);

        verify(quotaService).verifierQuota(PHARMACOPEE_ID, IPharmacopeeQuotaService.MAX_PRODUITS);
        verify(disponibiliteRepository).saveAndFlush(argThat(d -> Boolean.FALSE.equals(d.getDisponible())));
    }

    @Test
    @DisplayName("quota de produits atteint : la validation est refusée et l'achat reste en brouillon")
    void validationQuota() {
        Achat a = achat(StatutAchat.BROUILLON);
        ajouterLigne(a, null, null);
        when(disponibiliteRepository.findByPharmacopeeIdAndProduitIdForUpdate(PHARMACOPEE_ID, PRODUIT_ID)).thenReturn(Optional.empty());
        doThrow(new QuotaExceededException("limite")).when(quotaService).verifierQuota(PHARMACOPEE_ID, IPharmacopeeQuotaService.MAX_PRODUITS);

        assertThatThrownBy(() -> service.valider(50L)).isInstanceOf(QuotaExceededException.class);
        assertThat(a.estBrouillon()).isTrue();
    }

    @Test
    @DisplayName("un numéro de lot ou une péremption créent un lot rattaché à la ligne")
    void validationAvecLot() {
        Achat a = achat(StatutAchat.BROUILLON);
        LigneAchat ligne = ajouterLigne(a, "L-2026-07", LocalDate.of(2027, 6, 30));
        when(disponibiliteRepository.findByPharmacopeeIdAndProduitIdForUpdate(PHARMACOPEE_ID, PRODUIT_ID))
                .thenReturn(Optional.of(DisponibiliteProduit.builder().id(8L).pharmacopee(pharmacopee).produit(produit).build()));
        when(lotRepository.save(any(Lot.class))).thenAnswer(i -> i.getArgument(0));

        service.valider(50L);

        verify(lotRepository).save(argThat(l -> l.getLigneAchat() == ligne && l.getQuantiteInitiale() == 10 && "L-2026-07".equals(l.getNumeroLot())));
    }

    @Test
    @DisplayName("annuler un brouillon est sans effet sur le stock")
    void annulationBrouillon() {
        Achat a = achat(StatutAchat.BROUILLON);

        service.annuler(50L, "Erreur");

        assertThat(a.getStatut()).isEqualTo(StatutAchat.ANNULE);
        verify(mouvementStockService, never()).annuler(any(), any(), any(), any());
    }

    @Test
    @DisplayName("annuler un achat validé crée les mouvements inverses")
    void annulationValide() {
        Achat a = achat(StatutAchat.VALIDE);
        ajouterLigne(a, null, null);
        List<MouvementStock> entrees = List.of(MouvementStock.builder().quantite(10).build());
        when(mouvementStockRepository.findByLigneAchatId(70L)).thenReturn(entrees);

        service.annuler(50L, "Marchandise refusée");

        verify(mouvementStockService).annuler(entrees, TypeMouvementStock.ANNULATION_ACHAT, "Marchandise refusée", membre);
        assertThat(a.getMotifAnnulation()).isEqualTo("Marchandise refusée");
    }

    @Test
    @DisplayName("un achat qui a des règlements valides ne s'annule pas")
    void annulationAvecReglements() {
        achat(StatutAchat.VALIDE);
        when(reglementRepository.sumMontantValide(50L)).thenReturn(new BigDecimal("1000"));

        assertThatThrownBy(() -> service.annuler(50L, "x")).isInstanceOf(ConflictException.class);
    }

    @Test
    @DisplayName("un achat déjà annulé ne s'annule pas deux fois")
    void annulationDouble() {
        achat(StatutAchat.ANNULE);

        assertThatThrownBy(() -> service.annuler(50L, "x")).isInstanceOf(ConflictException.class);
    }

    private static EnregistrerReglementAchatRequest reglement(String montant) {
        return EnregistrerReglementAchatRequest.builder().montant(new BigDecimal(montant)).methode(MethodePaiement.CASH).caisseId(3L).build();
    }

    @Test
    @DisplayName("règlement partiel : une sortie de caisse de nature ACHAT")
    void reglementPartiel() {
        achat(StatutAchat.VALIDE);

        service.enregistrerReglement(50L, reglement("5000"));

        verify(operationCaisseService).enregistrer(eq(PHARMACOPEE_ID), argThat(o -> o.sens() == SensOperation.SORTIE
                && o.nature() == NatureOperationCaisse.ACHAT && o.caisseId().equals(3L) && o.reglementAchat() != null));
    }

    @Test
    @DisplayName("un règlement ne peut pas dépasser le reste à payer")
    void reglementTropEleve() {
        achat(StatutAchat.VALIDE);
        when(reglementRepository.sumMontantValide(50L)).thenReturn(new BigDecimal("14000"));

        assertThatThrownBy(() -> service.enregistrerReglement(50L, reglement("1000.01"))).isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("un brouillon ne se règle pas")
    void reglementBrouillon() {
        achat(StatutAchat.BROUILLON);

        assertThatThrownBy(() -> service.enregistrerReglement(50L, reglement("100"))).isInstanceOf(ConflictException.class);
    }

    private ReglementAchat reglementExistant(Achat a, StatutEcriture statut) {
        ReglementAchat r = ReglementAchat.builder().id(90L).achat(a).statut(statut).build();
        when(reglementRepository.findByIdAndAchatPharmacopeeId(90L, PHARMACOPEE_ID)).thenReturn(Optional.of(r));
        return r;
    }

    @Test
    @DisplayName("annuler un règlement contre-passe son opération de caisse")
    void annulationReglement() {
        ReglementAchat r = reglementExistant(achat(StatutAchat.VALIDE), StatutEcriture.VALIDE);
        when(operationCaisseRepository.findFirstByReglementAchatIdAndNatureNot(90L, NatureOperationCaisse.ANNULATION))
                .thenReturn(Optional.of(OperationCaisse.builder().id(200L).build()));

        service.annulerReglement(50L, 90L, "Doublon");

        verify(operationCaisseService).contrePasser(eq(PHARMACOPEE_ID), eq(200L), any(), eq(membre));
        assertThat(r.getStatut()).isEqualTo(StatutEcriture.ANNULE);
    }

    @Test
    @DisplayName("un règlement déjà annulé ne s'annule pas deux fois")
    void annulationReglementDouble() {
        reglementExistant(achat(StatutAchat.VALIDE), StatutEcriture.ANNULE);

        assertThatThrownBy(() -> service.annulerReglement(50L, 90L, "x")).isInstanceOf(ConflictException.class);
    }

    @Test
    @DisplayName("le règlement d'un autre achat n'est pas trouvé")
    void reglementAutreAchat() {
        reglementExistant(Achat.builder().id(51L).build(), StatutEcriture.VALIDE);

        assertThatThrownBy(() -> service.annulerReglement(50L, 90L, "x")).isInstanceOf(ResourceNotFoundException.class);
    }
}
