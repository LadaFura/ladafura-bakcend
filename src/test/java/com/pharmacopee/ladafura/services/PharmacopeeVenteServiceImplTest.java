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

import com.pharmacopee.ladafura.Models.Client;
import com.pharmacopee.ladafura.Models.DisponibiliteProduit;
import com.pharmacopee.ladafura.Models.LigneVente;
import com.pharmacopee.ladafura.Models.MembrePharmacopee;
import com.pharmacopee.ladafura.Models.MouvementStock;
import com.pharmacopee.ladafura.Models.OperationCaisse;
import com.pharmacopee.ladafura.Models.PaiementVente;
import com.pharmacopee.ladafura.Models.Pharmacopee;
import com.pharmacopee.ladafura.Models.Produit;
import com.pharmacopee.ladafura.Models.Vente;
import com.pharmacopee.ladafura.dto.pharmacopee.vente.EnregistrerVenteRequest;
import com.pharmacopee.ladafura.dto.pharmacopee.vente.LigneVenteRequest;
import com.pharmacopee.ladafura.dto.pharmacopee.vente.PaiementVenteRequest;
import com.pharmacopee.ladafura.dto.pharmacopee.vente.ValiderVenteRequest;
import com.pharmacopee.ladafura.dto.pharmacopee.vente.VenteResponse;
import com.pharmacopee.ladafura.enums.MethodePaiement;
import com.pharmacopee.ladafura.enums.NatureOperationCaisse;
import com.pharmacopee.ladafura.enums.SensOperation;
import com.pharmacopee.ladafura.enums.StatutEcriture;
import com.pharmacopee.ladafura.enums.StatutVente;
import com.pharmacopee.ladafura.enums.TypeMouvementStock;
import com.pharmacopee.ladafura.exceptions.BadRequestException;
import com.pharmacopee.ladafura.exceptions.ConflictException;
import com.pharmacopee.ladafura.exceptions.ForbiddenException;
import com.pharmacopee.ladafura.exceptions.QuotaExceededException;
import com.pharmacopee.ladafura.exceptions.ResourceNotFoundException;
import com.pharmacopee.ladafura.repository.ClientRepository;
import com.pharmacopee.ladafura.repository.DisponibiliteProduitRepository;
import com.pharmacopee.ladafura.repository.MouvementStockRepository;
import com.pharmacopee.ladafura.repository.OperationCaisseRepository;
import com.pharmacopee.ladafura.repository.PaiementVenteRepository;
import com.pharmacopee.ladafura.repository.VenteRepository;
import com.pharmacopee.ladafura.services.impl.PharmacopeeVenteServiceImpl;
import com.pharmacopee.ladafura.services.interfaces.IJournalAuditService;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeContexteService;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeContexteService.ContexteMembre;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeMouvementStockService;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeOperationCaisseService;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeQuotaService;

@ExtendWith(MockitoExtension.class)
class PharmacopeeVenteServiceImplTest {

    private static final Long PHARMACOPEE_ID = 5L;
    private static final Long FICHE_ID = 8L;
    private static final Set<String> TOUS_DROITS = Set.of(IPharmacopeeContexteService.VENTES_REMISE, IPharmacopeeContexteService.PAIEMENTS_ENCAISSER);

    @Mock private IPharmacopeeContexteService contexteService;
    @Mock private IPharmacopeeQuotaService quotaService;
    @Mock private IPharmacopeeMouvementStockService mouvementStockService;
    @Mock private IPharmacopeeOperationCaisseService operationCaisseService;
    @Mock private IJournalAuditService journalAuditService;
    @Mock private VenteRepository venteRepository;
    @Mock private ClientRepository clientRepository;
    @Mock private DisponibiliteProduitRepository disponibiliteRepository;
    @Mock private MouvementStockRepository mouvementStockRepository;
    @Mock private PaiementVenteRepository paiementRepository;
    @Mock private OperationCaisseRepository operationCaisseRepository;

    @InjectMocks private PharmacopeeVenteServiceImpl service;

    private final Pharmacopee pharmacopee = Pharmacopee.builder().id(PHARMACOPEE_ID).nom("Officine").build();
    private final MembrePharmacopee membre = MembrePharmacopee.builder().id(1L).build();
    private final Produit produit = Produit.builder().id(7L).nom("Tisane").prix(1500.0).build();
    private final DisponibiliteProduit fiche = DisponibiliteProduit.builder().id(FICHE_ID).pharmacopee(pharmacopee).produit(produit).prix(2000.0).build();

    @BeforeEach
    void preparer() {
        droits(TOUS_DROITS);
        lenient().when(venteRepository.saveAndFlush(any(Vente.class))).thenAnswer(i -> i.getArgument(0));
        lenient().when(paiementRepository.save(any(PaiementVente.class))).thenAnswer(i -> i.getArgument(0));
        lenient().when(paiementRepository.sumMontantValide(any())).thenReturn(BigDecimal.ZERO);
        lenient().when(disponibiliteRepository.findByIdAndPharmacopeeId(FICHE_ID, PHARMACOPEE_ID)).thenReturn(Optional.of(fiche));
        lenient().when(venteRepository.findFirstByPharmacopeeIdAndNumeroVenteStartingWithOrderByNumeroVenteDesc(any(), any())).thenReturn(Optional.empty());
    }

    private void droits(Set<String> permissions) {
        ContexteMembre contexte = new ContexteMembre(membre, pharmacopee, permissions);
        lenient().when(contexteService.verifierPermission(anyString())).thenReturn(contexte);
        lenient().when(contexteService.verifierOperationCommerciale(anyString())).thenReturn(contexte);
    }

    private static LigneVenteRequest ligne(int quantite, String remise) {
        return LigneVenteRequest.builder().disponibiliteProduitId(FICHE_ID).quantite(quantite).remise(remise != null ? new BigDecimal(remise) : null).build();
    }

    private static EnregistrerVenteRequest vente(LigneVenteRequest... lignes) {
        return EnregistrerVenteRequest.builder().lignes(List.of(lignes)).build();
    }

    private Vente venteExistante(StatutVente statut, Client client) {
        Vente v = Vente.builder().id(50L).pharmacopee(pharmacopee).numeroVente("VTE-2026-000001").statut(statut).client(client)
                .montantRemise(BigDecimal.ZERO).montantBrut(BigDecimal.ZERO).montantTotal(new BigDecimal("4000")).build();
        lenient().when(venteRepository.findByIdAndPharmacopeeId(50L, PHARMACOPEE_ID)).thenReturn(Optional.of(v));
        return v;
    }

    private LigneVente ajouterLigne(Vente v, int quantite) {
        LigneVente l = LigneVente.builder().id(70L).disponibiliteProduit(fiche).quantite(quantite).prixUnitaire(new BigDecimal("1")).remise(BigDecimal.ZERO).build();
        v.ajouterLigne(l);
        return l;
    }

    private static ValiderVenteRequest paiements(String... montants) {
        return ValiderVenteRequest.builder().paiements(java.util.Arrays.stream(montants).map(PharmacopeeVenteServiceImplTest::paiement).toList()).build();
    }

    private static PaiementVenteRequest paiement(String montant) {
        return PaiementVenteRequest.builder().montant(new BigDecimal(montant)).methode(MethodePaiement.MOBILE_MONEY).caisseId(3L).build();
    }

    @Test
    @DisplayName("vente en attente : numérotée, prix de la fiche, aucun effet sur le stock")
    void creation() {
        VenteResponse reponse = service.creer(vente(ligne(2, null)));

        assertThat(reponse.getNumeroVente()).isEqualTo("VTE-" + Year.now().getValue() + "-000001");
        assertThat(reponse.getLignes().get(0).getPrixUnitaire()).isEqualByComparingTo("2000");
        assertThat(reponse.getMontantTotal()).isEqualByComparingTo("4000");
        verify(mouvementStockService, never()).sortir(any(), any(), anyInt(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("sans prix sur la fiche, le prix national du produit s'applique")
    void prixNational() {
        fiche.setPrix(null);

        assertThat(service.creer(vente(ligne(1, null))).getMontantTotal()).isEqualByComparingTo("1500");
    }

    @Test
    @DisplayName("la fiche d'une autre pharmacopée n'est pas trouvée")
    void ficheAutrePharmacopee() {
        when(disponibiliteRepository.findByIdAndPharmacopeeId(FICHE_ID, PHARMACOPEE_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.creer(vente(ligne(1, null)))).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("une remise exige la permission VENTES_REMISE")
    void remiseSansDroit() {
        droits(Set.of());

        assertThatThrownBy(() -> service.creer(vente(ligne(1, "100")))).isInstanceOf(ForbiddenException.class);
    }

    @Test
    @DisplayName("une remise supérieure au montant est refusée")
    void remiseExcessive() {
        assertThatThrownBy(() -> service.creer(vente(ligne(1, "5000")))).isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("un client désactivé ne peut pas être rattaché")
    void clientDesactive() {
        when(clientRepository.findByIdAndPharmacopeeId(9L, PHARMACOPEE_ID)).thenReturn(Optional.of(Client.builder().id(9L).actif(false).build()));

        assertThatThrownBy(() -> service.creer(EnregistrerVenteRequest.builder().clientId(9L).build())).isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("une vente vide ne se valide pas")
    void validationVide() {
        venteExistante(StatutVente.EN_ATTENTE, null);

        assertThatThrownBy(() -> service.valider(50L, null)).isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("quota mensuel atteint : validation refusée, stock intact")
    void validationQuota() {
        ajouterLigne(venteExistante(StatutVente.EN_ATTENTE, null), 2);
        doThrow(new QuotaExceededException("limite")).when(quotaService).verifierQuota(PHARMACOPEE_ID, IPharmacopeeQuotaService.MAX_VENTES_MENSUELLES);

        assertThatThrownBy(() -> service.valider(50L, paiements("4000"))).isInstanceOf(QuotaExceededException.class);
        verify(mouvementStockService, never()).sortir(any(), any(), anyInt(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("validation payée : prix figés, stock sorti, entrée en caisse de nature VENTE")
    void validationPayee() {
        Vente v = venteExistante(StatutVente.EN_ATTENTE, null);
        LigneVente l = ajouterLigne(v, 2);

        VenteResponse reponse = service.valider(50L, paiements("4000"));

        assertThat(v.estValidee()).isTrue();
        assertThat(l.getPrixUnitaire()).isEqualByComparingTo("2000");
        assertThat(v.getValidePar()).isSameAs(membre);
        assertThat(reponse.getMontantTotal()).isEqualByComparingTo("4000");
        verify(mouvementStockService).sortir(eq(PHARMACOPEE_ID), eq(FICHE_ID), eq(2), eq(TypeMouvementStock.SORTIE_VENTE), any(), any(), eq(membre));
        verify(operationCaisseService).enregistrer(eq(PHARMACOPEE_ID), argThat(o -> o.sens() == SensOperation.ENTREE && o.nature() == NatureOperationCaisse.VENTE && o.paiementVente() != null));
    }

    @Test
    @DisplayName("un reste à payer sans client est refusé")
    void creditSansClient() {
        ajouterLigne(venteExistante(StatutVente.EN_ATTENTE, null), 2);

        assertThatThrownBy(() -> service.valider(50L, paiements("1000"))).isInstanceOf(BadRequestException.class);
        verify(mouvementStockService, never()).sortir(any(), any(), anyInt(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("un crédit avec client est accepté")
    void creditAvecClient() {
        Vente v = venteExistante(StatutVente.EN_ATTENTE, Client.builder().id(9L).nom("Sidibé").build());
        ajouterLigne(v, 2);

        service.valider(50L, paiements("1000"));

        assertThat(v.estValidee()).isTrue();
    }

    @Test
    @DisplayName("des paiements supérieurs au total sont refusés")
    void tropPercu() {
        ajouterLigne(venteExistante(StatutVente.EN_ATTENTE, null), 2);

        assertThatThrownBy(() -> service.valider(50L, paiements("3000", "2000"))).isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("encaisser à la validation exige PAIEMENTS_ENCAISSER")
    void encaissementSansDroit() {
        droits(Set.of());
        ajouterLigne(venteExistante(StatutVente.EN_ATTENTE, null), 2);

        assertThatThrownBy(() -> service.valider(50L, paiements("4000"))).isInstanceOf(ForbiddenException.class);
    }

    @Test
    @DisplayName("une vente validée n'est plus modifiable ni abandonnable")
    void venteValideeImmuable() {
        venteExistante(StatutVente.VALIDEE, null);

        assertThatThrownBy(() -> service.modifier(50L, vente())).isInstanceOf(ConflictException.class);
        assertThatThrownBy(() -> service.abandonner(50L)).isInstanceOf(ConflictException.class);
    }

    @Test
    @DisplayName("abandon d'une vente en attente, conservée")
    void abandon() {
        Vente v = venteExistante(StatutVente.EN_ATTENTE, null);

        service.abandonner(50L);

        assertThat(v.getStatut()).isEqualTo(StatutVente.ABANDONNEE);
    }

    private PaiementVente paiementExistant(Vente v, StatutEcriture statut) {
        return PaiementVente.builder().id(90L).vente(v).statut(statut).montant(new BigDecimal("1000")).build();
    }

    private void operationDe(Long paiementId) {
        when(operationCaisseRepository.findFirstByPaiementVenteIdAndNatureNot(paiementId, NatureOperationCaisse.ANNULATION))
                .thenReturn(Optional.of(OperationCaisse.builder().id(200L).build()));
    }

    @Test
    @DisplayName("annulation : stock remis, paiements valides contre-passés")
    void annulation() {
        Vente v = venteExistante(StatutVente.VALIDEE, null);
        ajouterLigne(v, 2);
        List<MouvementStock> sorties = List.of(MouvementStock.builder().quantite(-2).build());
        when(mouvementStockRepository.findByLigneVenteId(70L)).thenReturn(sorties);
        PaiementVente valide = paiementExistant(v, StatutEcriture.VALIDE);
        when(paiementRepository.findByVenteId(50L)).thenReturn(List.of(valide, PaiementVente.builder().id(91L).statut(StatutEcriture.ANNULE).build()));
        operationDe(90L);

        service.annuler(50L, "Erreur de caisse");

        assertThat(v.getStatut()).isEqualTo(StatutVente.ANNULEE);
        assertThat(valide.getStatut()).isEqualTo(StatutEcriture.ANNULE);
        verify(mouvementStockService).annuler(sorties, TypeMouvementStock.ANNULATION_VENTE, "Erreur de caisse", membre);
        verify(operationCaisseService).contrePasser(eq(PHARMACOPEE_ID), eq(200L), any(), eq(membre));
    }

    @Test
    @DisplayName("seule une vente validée s'annule")
    void annulationNonValidee() {
        venteExistante(StatutVente.EN_ATTENTE, null);

        assertThatThrownBy(() -> service.annuler(50L, "x")).isInstanceOf(ConflictException.class);
    }

    @Test
    @DisplayName("un paiement ultérieur ne dépasse pas le reste à payer")
    void paiementTropEleve() {
        venteExistante(StatutVente.VALIDEE, Client.builder().id(9L).nom("S").build());
        when(paiementRepository.sumMontantValide(50L)).thenReturn(new BigDecimal("3500"));

        assertThatThrownBy(() -> service.enregistrerPaiement(50L, paiement("501"))).isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("un paiement ultérieur solde la dette et entre en caisse")
    void paiementUlterieur() {
        venteExistante(StatutVente.VALIDEE, Client.builder().id(9L).nom("S").build());

        service.enregistrerPaiement(50L, paiement("4000"));

        verify(operationCaisseService).enregistrer(eq(PHARMACOPEE_ID), any());
    }

    @Test
    @DisplayName("une vente en attente ne reçoit pas de paiement")
    void paiementEnAttente() {
        venteExistante(StatutVente.EN_ATTENTE, null);

        assertThatThrownBy(() -> service.enregistrerPaiement(50L, paiement("100"))).isInstanceOf(ConflictException.class);
    }

    @Test
    @DisplayName("annulation d'un paiement : contre-passation")
    void annulationPaiement() {
        Vente v = venteExistante(StatutVente.VALIDEE, null);
        PaiementVente p = paiementExistant(v, StatutEcriture.VALIDE);
        when(paiementRepository.findByIdAndVentePharmacopeeId(90L, PHARMACOPEE_ID)).thenReturn(Optional.of(p));
        operationDe(90L);

        service.annulerPaiement(50L, 90L, "Doublon");

        assertThat(p.getStatut()).isEqualTo(StatutEcriture.ANNULE);
    }

    @Test
    @DisplayName("le paiement d'une autre vente n'est pas trouvé")
    void paiementAutreVente() {
        PaiementVente p = paiementExistant(Vente.builder().id(51L).build(), StatutEcriture.VALIDE);
        when(paiementRepository.findByIdAndVentePharmacopeeId(90L, PHARMACOPEE_ID)).thenReturn(Optional.of(p));

        assertThatThrownBy(() -> service.annulerPaiement(50L, 90L, "x")).isInstanceOf(ResourceNotFoundException.class);
    }
}
