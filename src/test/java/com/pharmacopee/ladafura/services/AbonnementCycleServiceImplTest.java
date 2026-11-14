package com.pharmacopee.ladafura.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.pharmacopee.ladafura.Models.Abonnement;
import com.pharmacopee.ladafura.Models.ConfigurationLimiteOffre;
import com.pharmacopee.ladafura.Models.LimiteAbonnement;
import com.pharmacopee.ladafura.Models.LimiteOffre;
import com.pharmacopee.ladafura.Models.MembrePharmacopee;
import com.pharmacopee.ladafura.Models.OffreAbonnement;
import com.pharmacopee.ladafura.Models.Pharmacopee;
import com.pharmacopee.ladafura.enums.MotifSuspensionMembre;
import com.pharmacopee.ladafura.enums.StatutAbonnement;
import com.pharmacopee.ladafura.enums.StatutMembre;
import com.pharmacopee.ladafura.enums.TypeEvenementAbonnement;
import com.pharmacopee.ladafura.exceptions.ConflictException;
import com.pharmacopee.ladafura.repository.AbonnementRepository;
import com.pharmacopee.ladafura.repository.ConfigurationLimiteOffreRepository;
import com.pharmacopee.ladafura.repository.HistoriqueAbonnementRepository;
import com.pharmacopee.ladafura.repository.LimiteAbonnementRepository;
import com.pharmacopee.ladafura.repository.MembrePharmacopeeRepository;
import com.pharmacopee.ladafura.repository.OffreAbonnementRepository;
import com.pharmacopee.ladafura.services.impl.AbonnementCycleServiceImpl;
import com.pharmacopee.ladafura.services.interfaces.IJournalAuditService;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeQuotaService;

@ExtendWith(MockitoExtension.class)
class AbonnementCycleServiceImplTest {

    private static final Long PHARMACOPEE_ID = 5L;

    @Mock private AbonnementRepository abonnementRepository;
    @Mock private LimiteAbonnementRepository limiteAbonnementRepository;
    @Mock private HistoriqueAbonnementRepository historiqueRepository;
    @Mock private OffreAbonnementRepository offreRepository;
    @Mock private ConfigurationLimiteOffreRepository configurationRepository;
    @Mock private MembrePharmacopeeRepository membreRepository;
    @Mock private IJournalAuditService journalAuditService;

    @InjectMocks private AbonnementCycleServiceImpl service;

    private final Pharmacopee pharmacopee = Pharmacopee.builder().id(PHARMACOPEE_ID).nom("Officine").build();

    @BeforeEach
    void preparer() {
        lenient().when(abonnementRepository.save(any(Abonnement.class))).thenAnswer(i -> i.getArgument(0));
        lenient().when(abonnementRepository.saveAndFlush(any(Abonnement.class))).thenAnswer(i -> i.getArgument(0));
        lenient().when(limiteAbonnementRepository.findByAbonnementIdAndCodeLimite(any(), any())).thenReturn(Optional.empty());
    }

    private Abonnement abonnement(Long id, String code, StatutAbonnement statut, Integer duree) {
        return Abonnement.builder().id(id).pharmacopee(pharmacopee).codeOffreSouscrite(code).statut(statut)
                .dureeMoisSouscrite(duree).joursGraceSouscrits(7).prixSouscrit(new BigDecimal("10000")).build();
    }

    private void enVigueur(Abonnement courant) {
        when(abonnementRepository.findEnVigueur(PHARMACOPEE_ID)).thenReturn(Optional.ofNullable(courant));
    }

    @Test
    @DisplayName("la souscription copie les conditions et les quotas de l'offre")
    void instantane() {
        OffreAbonnement offre = OffreAbonnement.builder().id(1L).code("MENSUELLE").nom("Mensuelle").prix(new BigDecimal("10000"))
                .devise("XOF").dureeMois(1).joursGrace(7).build();
        LimiteOffre limite = LimiteOffre.builder().id(9L).codeLimite("MAX_PRODUITS").build();
        when(configurationRepository.findByOffreId(1L)).thenReturn(List.of(
                ConfigurationLimiteOffre.builder().limite(limite).valeurMaximale(200).illimite(false).build()));

        Abonnement abonnement = service.creerDepuisOffre(pharmacopee, offre, null);

        assertThat(abonnement.getStatut()).isEqualTo(StatutAbonnement.EN_ATTENTE_PAIEMENT);
        assertThat(abonnement.getPrixSouscrit()).isEqualByComparingTo("10000");
        assertThat(abonnement.getDureeMoisSouscrite()).isEqualTo(1);
        assertThat(abonnement.getLimites()).singleElement().satisfies(l -> {
            assertThat(l.getCodeLimite()).isEqualTo("MAX_PRODUITS");
            assertThat(l.getValeurMaximale()).isEqualTo(200);
        });
    }

    @Test
    @DisplayName("depuis l'offre gratuite : l'ancien expire d'abord, puis activation immédiate")
    void depuisGratuit() {
        Abonnement gratuit = abonnement(1L, "GRATUITE", StatutAbonnement.ACTIF, null);
        Abonnement nouveau = abonnement(2L, "MENSUELLE", StatutAbonnement.EN_ATTENTE_PAIEMENT, 1);
        enVigueur(gratuit);

        service.activerApresPaiement(nouveau, null);

        InOrder ordre = Mockito.inOrder(abonnementRepository);
        ordre.verify(abonnementRepository).saveAndFlush(gratuit);
        ordre.verify(abonnementRepository).saveAndFlush(nouveau);
        assertThat(gratuit.getStatut()).isEqualTo(StatutAbonnement.EXPIRE);
        assertThat(nouveau.getStatut()).isEqualTo(StatutAbonnement.ACTIF);
        assertThat(nouveau.getDateFin()).isEqualTo(nouveau.getDateDebut().plusMonths(1));
    }

    @Test
    @DisplayName("renouvellement pendant la grâce : activation immédiate")
    void depuisGrace() {
        Abonnement enGrace = abonnement(1L, "MENSUELLE", StatutAbonnement.EN_GRACE, 1);
        Abonnement nouveau = abonnement(2L, "MENSUELLE", StatutAbonnement.EN_ATTENTE_PAIEMENT, 1);
        enVigueur(enGrace);

        service.activerApresPaiement(nouveau, null);

        assertThat(enGrace.getStatut()).isEqualTo(StatutAbonnement.EXPIRE);
        assertThat(nouveau.getStatut()).isEqualTo(StatutAbonnement.ACTIF);
    }

    @Test
    @DisplayName("renouvellement anticipé de la même offre : PROGRAMME, à la fin de l'abonnement en cours")
    void renouvellementAnticipe() {
        LocalDateTime fin = LocalDateTime.now().plusDays(10);
        Abonnement actif = abonnement(1L, "MENSUELLE", StatutAbonnement.ACTIF, 1);
        actif.setDateFin(fin);
        Abonnement nouveau = abonnement(2L, "MENSUELLE", StatutAbonnement.EN_ATTENTE_PAIEMENT, 1);
        enVigueur(actif);

        service.activerApresPaiement(nouveau, null);

        assertThat(actif.getStatut()).isEqualTo(StatutAbonnement.ACTIF);
        assertThat(nouveau.getStatut()).isEqualTo(StatutAbonnement.PROGRAMME);
        assertThat(nouveau.getDateDebut()).isEqualTo(fin);
        assertThat(nouveau.getDateFin()).isEqualTo(fin.plusMonths(1));
    }

    @Test
    @DisplayName("un seul renouvellement programmé à la fois")
    void deuxProgrammes() {
        Abonnement actif = abonnement(1L, "MENSUELLE", StatutAbonnement.ACTIF, 1);
        actif.setDateFin(LocalDateTime.now().plusDays(10));
        enVigueur(actif);
        lenient().when(abonnementRepository.existsByPharmacopeeIdAndStatut(PHARMACOPEE_ID, StatutAbonnement.PROGRAMME)).thenReturn(true);

        assertThatThrownBy(() -> service.activerApresPaiement(abonnement(2L, "MENSUELLE", StatutAbonnement.EN_ATTENTE_PAIEMENT, 1), null))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    @DisplayName("changement d'offre : l'ancien est annulé, le nouveau actif immédiatement")
    void changementOffre() {
        Abonnement actif = abonnement(1L, "MENSUELLE", StatutAbonnement.ACTIF, 1);
        actif.setDateFin(LocalDateTime.now().plusDays(10));
        Abonnement nouveau = abonnement(2L, "ANNUELLE", StatutAbonnement.EN_ATTENTE_PAIEMENT, 12);
        enVigueur(actif);

        service.activerApresPaiement(nouveau, null);

        assertThat(actif.getStatut()).isEqualTo(StatutAbonnement.ANNULE);
        assertThat(actif.getMotifAnnulation()).isEqualTo("Changement d'offre");
        assertThat(nouveau.getStatut()).isEqualTo(StatutAbonnement.ACTIF);
    }

    @Test
    @DisplayName("un abonnement qui n'attend pas de paiement est refusé")
    void statutIncorrect() {
        assertThatThrownBy(() -> service.activerApresPaiement(abonnement(2L, "MENSUELLE", StatutAbonnement.ANNULE, 1), null))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    @DisplayName("un abonnement suspendu bloque l'activation")
    void suspenduBloque() {
        when(abonnementRepository.existsByPharmacopeeIdAndStatut(PHARMACOPEE_ID, StatutAbonnement.SUSPENDU)).thenReturn(true);

        assertThatThrownBy(() -> service.activerApresPaiement(abonnement(2L, "MENSUELLE", StatutAbonnement.EN_ATTENTE_PAIEMENT, 1), null))
                .isInstanceOf(ConflictException.class);
    }

    private static MembrePharmacopee membre(long id, StatutMembre statut) {
        return MembrePharmacopee.builder().id(id).statut(statut).motifSuspension(statut == StatutMembre.SUSPENDU ? MotifSuspensionMembre.QUOTA_ABONNEMENT : null).build();
    }

    private void limiteUtilisateurs(Long abonnementId, int max) {
        when(limiteAbonnementRepository.findByAbonnementIdAndCodeLimite(abonnementId, IPharmacopeeQuotaService.MAX_UTILISATEURS_ACTIFS))
                .thenReturn(Optional.of(LimiteAbonnement.builder().valeurMaximale(max).illimite(false).build()));
    }

    @Test
    @DisplayName("activation : seuls les membres suspendus pour quota reviennent, dans la limite")
    void reactivationMembres() {
        Abonnement nouveau = abonnement(2L, "MENSUELLE", StatutAbonnement.EN_ATTENTE_PAIEMENT, 1);
        enVigueur(null);
        limiteUtilisateurs(2L, 2);
        when(membreRepository.countByPharmacopeeIdAndStatut(PHARMACOPEE_ID, StatutMembre.ACTIF)).thenReturn(1L);
        MembrePharmacopee premier = membre(1, StatutMembre.SUSPENDU);
        MembrePharmacopee second = membre(2, StatutMembre.SUSPENDU);
        when(membreRepository.findByPharmacopeeIdAndStatutAndMotifSuspensionOrderByIdAsc(PHARMACOPEE_ID, StatutMembre.SUSPENDU, MotifSuspensionMembre.QUOTA_ABONNEMENT))
                .thenReturn(List.of(premier, second));

        service.activerApresPaiement(nouveau, null);

        assertThat(premier.getStatut()).isEqualTo(StatutMembre.ACTIF);
        assertThat(premier.getMotifSuspension()).isNull();
        assertThat(second.getStatut()).isEqualTo(StatutMembre.SUSPENDU);
    }

    @Test
    @DisplayName("échéance sans renouvellement : passage en grâce")
    void passageGrace() {
        LocalDateTime fin = LocalDateTime.now().minusHours(1);
        Abonnement actif = abonnement(1L, "MENSUELLE", StatutAbonnement.ACTIF, 1);
        actif.setDateFin(fin);
        when(abonnementRepository.findByStatutAndDateFinBefore(any(), any())).thenReturn(List.of(actif));
        when(abonnementRepository.findByStatutAndDateFinGraceBefore(any(), any())).thenReturn(List.of());
        when(abonnementRepository.findFirstByPharmacopeeIdAndStatut(PHARMACOPEE_ID, StatutAbonnement.PROGRAMME)).thenReturn(Optional.empty());

        assertThat(service.traiterEcheances(LocalDateTime.now())).isEqualTo(1);

        assertThat(actif.getStatut()).isEqualTo(StatutAbonnement.EN_GRACE);
        assertThat(actif.getDateFinGrace()).isEqualTo(fin.plusDays(7));
    }

    @Test
    @DisplayName("échéance avec renouvellement programmé : il prend le relais, sans grâce")
    void echeanceAvecProgramme() {
        LocalDateTime fin = LocalDateTime.now().minusHours(1);
        Abonnement actif = abonnement(1L, "MENSUELLE", StatutAbonnement.ACTIF, 1);
        actif.setDateFin(fin);
        Abonnement programme = abonnement(2L, "MENSUELLE", StatutAbonnement.PROGRAMME, 1);
        programme.setDateDebut(fin);
        when(abonnementRepository.findByStatutAndDateFinBefore(any(), any())).thenReturn(List.of(actif));
        when(abonnementRepository.findByStatutAndDateFinGraceBefore(any(), any())).thenReturn(List.of());
        when(abonnementRepository.findFirstByPharmacopeeIdAndStatut(PHARMACOPEE_ID, StatutAbonnement.PROGRAMME)).thenReturn(Optional.of(programme));

        service.traiterEcheances(LocalDateTime.now());

        assertThat(actif.getStatut()).isEqualTo(StatutAbonnement.EXPIRE);
        assertThat(programme.getStatut()).isEqualTo(StatutAbonnement.ACTIF);
        assertThat(programme.getDateFin()).isEqualTo(fin.plusMonths(1));
    }

    @Test
    @DisplayName("fin de grâce : retour au gratuit, les membres en trop sont suspendus, jamais le gérant")
    void retourGratuit() {
        Abonnement enGrace = abonnement(1L, "MENSUELLE", StatutAbonnement.EN_GRACE, 1);
        when(abonnementRepository.findByStatutAndDateFinBefore(any(), any())).thenReturn(List.of());
        when(abonnementRepository.findByStatutAndDateFinGraceBefore(any(), any())).thenReturn(List.of(enGrace));
        OffreAbonnement gratuite = OffreAbonnement.builder().id(7L).code("GRATUITE").nom("Gratuite").prix(BigDecimal.ZERO).devise("XOF").joursGrace(0).build();
        when(offreRepository.findByCode("GRATUITE")).thenReturn(Optional.of(gratuite));
        when(configurationRepository.findByOffreId(7L)).thenReturn(List.of());
        when(limiteAbonnementRepository.findByAbonnementIdAndCodeLimite(any(), any()))
                .thenReturn(Optional.of(LimiteAbonnement.builder().valeurMaximale(1).illimite(false).build()));
        when(membreRepository.countByPharmacopeeIdAndStatut(PHARMACOPEE_ID, StatutMembre.ACTIF)).thenReturn(3L);
        MembrePharmacopee recent = membre(3, StatutMembre.ACTIF);
        MembrePharmacopee ancien = membre(2, StatutMembre.ACTIF);
        when(membreRepository.findByPharmacopeeIdAndStatutAndEstProprietaireFalseOrderByDateAdhesionDescIdDesc(PHARMACOPEE_ID, StatutMembre.ACTIF))
                .thenReturn(List.of(recent, ancien));
        when(membreRepository.findByPharmacopeeIdAndStatutAndMotifSuspensionOrderByIdAsc(anyLong(), any(), any())).thenReturn(List.of());

        service.traiterEcheances(LocalDateTime.now());

        assertThat(enGrace.getStatut()).isEqualTo(StatutAbonnement.EXPIRE);
        assertThat(recent.getStatut()).isEqualTo(StatutMembre.SUSPENDU);
        assertThat(recent.getMotifSuspension()).isEqualTo(MotifSuspensionMembre.QUOTA_ABONNEMENT);
        assertThat(ancien.getStatut()).isEqualTo(StatutMembre.SUSPENDU);
        verify(historiqueRepository).save(Mockito.argThat(h -> h.getTypeEvenement() == TypeEvenementAbonnement.RETOUR_GRATUIT));
    }
}
