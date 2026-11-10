package com.pharmacopee.ladafura.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.pharmacopee.ladafura.Models.Abonnement;
import com.pharmacopee.ladafura.Models.LimiteAbonnement;
import com.pharmacopee.ladafura.Models.MembrePharmacopee;
import com.pharmacopee.ladafura.Models.OffreAbonnement;
import com.pharmacopee.ladafura.Models.PaiementAbonnement;
import com.pharmacopee.ladafura.Models.Pharmacopee;
import com.pharmacopee.ladafura.dto.pharmacopee.abonnement.AbonnementResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.abonnement.DeclarerPaiementRequest;
import com.pharmacopee.ladafura.dto.pharmacopee.abonnement.MotifRequest;
import com.pharmacopee.ladafura.dto.pharmacopee.abonnement.PaiementAbonnementResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.abonnement.SouscrireOffreRequest;
import com.pharmacopee.ladafura.enums.ModePaiementAbonnement;
import com.pharmacopee.ladafura.enums.StatutAbonnement;
import com.pharmacopee.ladafura.enums.StatutPaiement;
import com.pharmacopee.ladafura.enums.TypeOffre;
import com.pharmacopee.ladafura.exceptions.BadRequestException;
import com.pharmacopee.ladafura.exceptions.ConflictException;
import com.pharmacopee.ladafura.exceptions.ForbiddenException;
import com.pharmacopee.ladafura.exceptions.ResourceNotFoundException;
import com.pharmacopee.ladafura.repository.AbonnementRepository;
import com.pharmacopee.ladafura.repository.ConfigurationLimiteOffreRepository;
import com.pharmacopee.ladafura.repository.DemandeContratRepository;
import com.pharmacopee.ladafura.repository.FactureAbonnementRepository;
import com.pharmacopee.ladafura.repository.OffreAbonnementRepository;
import com.pharmacopee.ladafura.repository.PaiementAbonnementRepository;
import com.pharmacopee.ladafura.services.impl.PharmacopeeAbonnementServiceImpl;
import com.pharmacopee.ladafura.services.interfaces.IAbonnementCycleService;
import com.pharmacopee.ladafura.services.interfaces.IJournalAuditService;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeContexteService;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeContexteService.ContexteMembre;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeQuotaService;

@ExtendWith(MockitoExtension.class)
class PharmacopeeAbonnementServiceImplTest {

    private static final Long PHARMACOPEE_ID = 5L;

    @Mock private IPharmacopeeContexteService contexteService;
    @Mock private IPharmacopeeQuotaService quotaService;
    @Mock private IAbonnementCycleService cycleService;
    @Mock private IJournalAuditService journalAuditService;
    @Mock private AbonnementRepository abonnementRepository;
    @Mock private OffreAbonnementRepository offreRepository;
    @Mock private ConfigurationLimiteOffreRepository configurationRepository;
    @Mock private PaiementAbonnementRepository paiementRepository;
    @Mock private FactureAbonnementRepository factureRepository;
    @Mock private DemandeContratRepository demandeRepository;

    @Mock private com.pharmacopee.ladafura.services.interfaces.IAbonnementNotificationService abonnementNotificationService;

    @InjectMocks private PharmacopeeAbonnementServiceImpl service;

    private final Pharmacopee pharmacopee = Pharmacopee.builder().id(PHARMACOPEE_ID).nom("Officine").build();
    private final MembrePharmacopee membre = MembrePharmacopee.builder().id(1L).build();

    @BeforeEach
    void preparer() {
        lenient().when(contexteService.verifierPermission(anyString())).thenReturn(new ContexteMembre(membre, pharmacopee, Set.of()));
        lenient().when(paiementRepository.save(any(PaiementAbonnement.class))).thenAnswer(i -> i.getArgument(0));
    }

    private Abonnement abonnement(StatutAbonnement statut) {
        Abonnement a = Abonnement.builder().id(50L).pharmacopee(pharmacopee).statut(statut).prixSouscrit(new BigDecimal("10000")).deviseSouscrite("XOF").build();
        lenient().when(abonnementRepository.findByIdAndPharmacopeeId(50L, PHARMACOPEE_ID)).thenReturn(Optional.of(a));
        return a;
    }

    private static OffreAbonnement offre(TypeOffre type, boolean actif) {
        return OffreAbonnement.builder().id(1L).code("X").typeOffre(type).actif(actif).build();
    }

    private static DeclarerPaiementRequest paiement(String montant) {
        return DeclarerPaiementRequest.builder().montant(new BigDecimal(montant)).modePaiement(ModePaiementAbonnement.ORANGE_MONEY).referenceTransaction("OM1").build();
    }

    private static DeclarerPaiementRequest paiementEspeces(String contactNom, String contactTelephone) {
        return DeclarerPaiementRequest.builder().montant(new BigDecimal("10000")).modePaiement(ModePaiementAbonnement.ESPECES)
                .referenceTransaction("IGNOREE").contactNom(contactNom).contactTelephone(contactTelephone).build();
    }

    @Test
    @DisplayName("espèces : le contact est enregistré, la référence ignorée et LADAFURA prévenue")
    void especesAvecContact() {
        abonnement(StatutAbonnement.EN_ATTENTE_PAIEMENT);

        PaiementAbonnementResponse reponse = service.declarerPaiement(50L, paiementEspeces("Awa Traoré", "+223 70 11 22 33"));

        org.assertj.core.api.Assertions.assertThat(reponse.getContactTelephone()).isEqualTo("+223 70 11 22 33");
        org.assertj.core.api.Assertions.assertThat(reponse.getReferenceTransaction()).isNull();
        org.mockito.Mockito.verify(abonnementNotificationService).paiementDeclare(any(PaiementAbonnement.class));
    }

    @Test
    @DisplayName("espèces : le contact est obligatoire")
    void especesSansContact() {
        abonnement(StatutAbonnement.EN_ATTENTE_PAIEMENT);

        assertThatThrownBy(() -> service.declarerPaiement(50L, paiementEspeces("Awa Traoré", " ")))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("abonnement en vigueur : limites et consommation")
    void enVigueur() {
        Abonnement a = abonnement(StatutAbonnement.ACTIF);
        a.ajouterLimite(LimiteAbonnement.builder().codeLimite("MAX_PRODUITS").valeurMaximale(20).illimite(false).build());
        when(abonnementRepository.findEnVigueur(PHARMACOPEE_ID)).thenReturn(Optional.of(a));
        when(quotaService.consommationActuelle(PHARMACOPEE_ID, "MAX_PRODUITS")).thenReturn(4L);

        AbonnementResponse reponse = service.getAbonnementEnVigueur();

        assertThat(reponse.getLimites()).singleElement().satisfies(l -> assertThat(l.getConsommation()).isEqualTo(4L));
    }

    @Test
    @DisplayName("consultation sans permission refusée")
    void consultationRefusee() {
        when(contexteService.verifierPermission(IPharmacopeeContexteService.ABONNEMENT_CONSULTER)).thenThrow(new ForbiddenException("non"));

        assertThatThrownBy(() -> service.getAbonnementEnVigueur()).isInstanceOf(ForbiddenException.class);
    }

    @Test
    @DisplayName("souscription : ne dépend pas d'un abonnement ACTIF, pour rester possible en grâce")
    void souscriptionEnGrace() {
        when(offreRepository.findById(1L)).thenReturn(Optional.of(offre(TypeOffre.MENSUELLE, true)));
        Abonnement cree = abonnement(StatutAbonnement.EN_ATTENTE_PAIEMENT);
        when(cycleService.creerDepuisOffre(eq(pharmacopee), any(), eq(membre))).thenReturn(cree);

        service.souscrire(new SouscrireOffreRequest(1L));

        verify(contexteService, never()).verifierEcritureAutorisee(any());
    }

    @Test
    @DisplayName("une offre inactive, gratuite ou sur contrat n'est pas souscriptible")
    void offresNonSouscriptibles() {
        for (OffreAbonnement o : new OffreAbonnement[] {offre(TypeOffre.MENSUELLE, false), offre(TypeOffre.GRATUITE, true), offre(TypeOffre.CONTRAT_PERSONNALISE, true)}) {
            when(offreRepository.findById(1L)).thenReturn(Optional.of(o));
            assertThatThrownBy(() -> service.souscrire(new SouscrireOffreRequest(1L))).isInstanceOf(BadRequestException.class);
        }
    }

    @Test
    @DisplayName("une seule souscription en attente à la fois")
    void souscriptionDejaEnAttente() {
        when(offreRepository.findById(1L)).thenReturn(Optional.of(offre(TypeOffre.MENSUELLE, true)));
        when(abonnementRepository.existsByPharmacopeeIdAndStatut(PHARMACOPEE_ID, StatutAbonnement.EN_ATTENTE_PAIEMENT)).thenReturn(true);

        assertThatThrownBy(() -> service.souscrire(new SouscrireOffreRequest(1L))).isInstanceOf(ConflictException.class);
    }

    @Test
    @DisplayName("déclaration d'un paiement : EN_ATTENTE, à confirmer par l'administrateur")
    void declaration() {
        abonnement(StatutAbonnement.EN_ATTENTE_PAIEMENT);

        PaiementAbonnementResponse reponse = service.declarerPaiement(50L, paiement("10000"));

        assertThat(reponse.getStatut()).isEqualTo(StatutPaiement.EN_ATTENTE);
        assertThat(reponse.getReference()).startsWith("PAB-");
    }

    @Test
    @DisplayName("un montant différent du prix est refusé")
    void montantIncorrect() {
        abonnement(StatutAbonnement.EN_ATTENTE_PAIEMENT);

        assertThatThrownBy(() -> service.declarerPaiement(50L, paiement("9000"))).isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("un paiement déjà en vérification bloque une nouvelle déclaration")
    void paiementEnCours() {
        abonnement(StatutAbonnement.EN_ATTENTE_PAIEMENT);
        when(paiementRepository.existsByAbonnementIdAndStatut(50L, StatutPaiement.EN_ATTENTE)).thenReturn(true);

        assertThatThrownBy(() -> service.declarerPaiement(50L, paiement("10000"))).isInstanceOf(ConflictException.class);
    }

    @Test
    @DisplayName("l'abonnement d'une autre pharmacopée n'est pas trouvé")
    void autrePharmacopee() {
        when(abonnementRepository.findByIdAndPharmacopeeId(50L, PHARMACOPEE_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.declarerPaiement(50L, paiement("10000"))).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("annulation d'une souscription en attente")
    void annulation() {
        Abonnement a = abonnement(StatutAbonnement.EN_ATTENTE_PAIEMENT);

        service.annuler(50L, new MotifRequest("Erreur d'offre"));

        assertThat(a.getStatut()).isEqualTo(StatutAbonnement.ANNULE);
        assertThat(a.getMotifAnnulation()).isEqualTo("Erreur d'offre");
    }

    @Test
    @DisplayName("un abonnement actif ne peut pas être annulé par le gérant")
    void annulationActif() {
        abonnement(StatutAbonnement.ACTIF);

        assertThatThrownBy(() -> service.annuler(50L, new MotifRequest("x"))).isInstanceOf(ConflictException.class);
    }
}
