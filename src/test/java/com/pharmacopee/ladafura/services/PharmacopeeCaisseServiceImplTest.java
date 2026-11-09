package com.pharmacopee.ladafura.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.pharmacopee.ladafura.Models.Caisse;
import com.pharmacopee.ladafura.Models.Depense;
import com.pharmacopee.ladafura.Models.MembrePharmacopee;
import com.pharmacopee.ladafura.Models.OperationCaisse;
import com.pharmacopee.ladafura.Models.Pharmacopee;
import com.pharmacopee.ladafura.Models.SessionCaisse;
import com.pharmacopee.ladafura.dto.pharmacopee.caisse.CloturerSessionRequest;
import com.pharmacopee.ladafura.dto.pharmacopee.caisse.OperationManuelleRequest;
import com.pharmacopee.ladafura.dto.pharmacopee.caisse.OuvrirSessionRequest;
import com.pharmacopee.ladafura.dto.pharmacopee.caisse.SessionCaisseResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.caisse.TransfertCaisseRequest;
import com.pharmacopee.ladafura.enums.NatureOperationCaisse;
import com.pharmacopee.ladafura.enums.SensOperation;
import com.pharmacopee.ladafura.enums.StatutSessionCaisse;
import com.pharmacopee.ladafura.enums.TypeActionAudit;
import com.pharmacopee.ladafura.enums.TypeCaisse;
import com.pharmacopee.ladafura.exceptions.BadRequestException;
import com.pharmacopee.ladafura.exceptions.ConflictException;
import com.pharmacopee.ladafura.exceptions.ResourceNotFoundException;
import com.pharmacopee.ladafura.repository.CaisseRepository;
import com.pharmacopee.ladafura.repository.OperationCaisseRepository;
import com.pharmacopee.ladafura.repository.SessionCaisseRepository;
import com.pharmacopee.ladafura.services.impl.PharmacopeeCaisseServiceImpl;
import com.pharmacopee.ladafura.services.interfaces.IJournalAuditService;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeContexteService;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeContexteService.ContexteMembre;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeOperationCaisseService;

@ExtendWith(MockitoExtension.class)
class PharmacopeeCaisseServiceImplTest {

    private static final Long PHARMACOPEE_ID = 5L;
    private static final Long CAISSE_ID = 3L;

    @Mock private IPharmacopeeContexteService contexteService;
    @Mock private IPharmacopeeOperationCaisseService operationCaisseService;
    @Mock private IJournalAuditService journalAuditService;
    @Mock private CaisseRepository caisseRepository;
    @Mock private SessionCaisseRepository sessionRepository;
    @Mock private OperationCaisseRepository operationRepository;

    @InjectMocks private PharmacopeeCaisseServiceImpl service;

    private final Pharmacopee pharmacopee = Pharmacopee.builder().id(PHARMACOPEE_ID).nom("Officine").build();
    private final MembrePharmacopee membre = MembrePharmacopee.builder().id(1L).build();

    @BeforeEach
    void preparer() {
        ContexteMembre contexte = new ContexteMembre(membre, pharmacopee, Set.of());
        lenient().when(contexteService.verifierPermission(anyString())).thenReturn(contexte);
        lenient().when(contexteService.verifierOperationCommerciale(anyString())).thenReturn(contexte);
        lenient().when(sessionRepository.saveAndFlush(any(SessionCaisse.class))).thenAnswer(i -> i.getArgument(0));
        lenient().when(sessionRepository.findFirstByCaisseIdAndStatut(any(), any())).thenReturn(Optional.empty());
    }

    private Caisse caisse(TypeCaisse type, boolean actif) {
        Caisse c = Caisse.builder().id(CAISSE_ID).pharmacopee(pharmacopee).nom("Caisse").type(type).actif(actif).build();
        lenient().when(caisseRepository.findByIdAndPharmacopeeIdForUpdate(CAISSE_ID, PHARMACOPEE_ID)).thenReturn(Optional.of(c));
        lenient().when(caisseRepository.findByIdAndPharmacopeeId(CAISSE_ID, PHARMACOPEE_ID)).thenReturn(Optional.of(c));
        return c;
    }

    private SessionCaisse sessionOuverte(Caisse c, String fond) {
        SessionCaisse s = SessionCaisse.builder().id(30L).caisse(c).fondOuverture(new BigDecimal(fond)).statut(StatutSessionCaisse.OUVERTE).build();
        when(sessionRepository.findFirstByCaisseIdAndStatut(CAISSE_ID, StatutSessionCaisse.OUVERTE)).thenReturn(Optional.of(s));
        return s;
    }

    private void derniereCloture(String constate) {
        when(sessionRepository.findFirstByCaisseIdOrderByDateOuvertureDesc(CAISSE_ID))
                .thenReturn(Optional.of(SessionCaisse.builder().montantConstate(new BigDecimal(constate)).statut(StatutSessionCaisse.CLOTUREE).build()));
    }

    private void premiereSession() {
        when(sessionRepository.findFirstByCaisseIdOrderByDateOuvertureDesc(CAISSE_ID)).thenReturn(Optional.empty());
    }

    @Test
    @DisplayName("une caisse non physique n'a pas de session")
    void ouvertureCompte() {
        caisse(TypeCaisse.MOBILE_MONEY, true);

        assertThatThrownBy(() -> service.ouvrirSession(CAISSE_ID, new OuvrirSessionRequest())).isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("une caisse désactivée ne s'ouvre pas")
    void ouvertureDesactivee() {
        caisse(TypeCaisse.PHYSIQUE, false);

        assertThatThrownBy(() -> service.ouvrirSession(CAISSE_ID, new OuvrirSessionRequest())).isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("une deuxième session ouverte est refusée")
    void ouvertureDouble() {
        sessionOuverte(caisse(TypeCaisse.PHYSIQUE, true), "0");

        assertThatThrownBy(() -> service.ouvrirSession(CAISSE_ID, new OuvrirSessionRequest())).isInstanceOf(ConflictException.class);
    }

    @Test
    @DisplayName("le fond reprend le montant de la dernière clôture")
    void fondRepris() {
        caisse(TypeCaisse.PHYSIQUE, true);
        derniereCloture("7400");

        SessionCaisseResponse session = service.ouvrirSession(CAISSE_ID, new OuvrirSessionRequest());

        assertThat(session.getFondOuverture()).isEqualByComparingTo("7400");
        verify(journalAuditService, never()).tracer(any());
    }

    @Test
    @DisplayName("un fond différent sans commentaire est refusé")
    void fondDifferentSansCommentaire() {
        caisse(TypeCaisse.PHYSIQUE, true);
        derniereCloture("7400");

        assertThatThrownBy(() -> service.ouvrirSession(CAISSE_ID, new OuvrirSessionRequest(new BigDecimal("5000"), " "))).isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("un fond différent et justifié est accepté et tracé")
    void fondDifferentJustifie() {
        caisse(TypeCaisse.PHYSIQUE, true);
        derniereCloture("7400");

        service.ouvrirSession(CAISSE_ID, new OuvrirSessionRequest(new BigDecimal("5000"), "Prélèvement du gérant"));

        verify(journalAuditService).tracer(argThat(e -> e.action() == TypeActionAudit.ECART_CAISSE));
    }

    @Test
    @DisplayName("première session : fond libre, sans commentaire")
    void premiereOuverture() {
        caisse(TypeCaisse.PHYSIQUE, true);
        premiereSession();

        assertThat(service.ouvrirSession(CAISSE_ID, new OuvrirSessionRequest(new BigDecimal("10000"), null)).getFondOuverture()).isEqualByComparingTo("10000");
    }

    @Test
    @DisplayName("clôture sans écart : aucun ajustement")
    void clotureSansEcart() {
        SessionCaisse s = sessionOuverte(caisse(TypeCaisse.PHYSIQUE, true), "1000");
        when(operationRepository.soldeSession(30L)).thenReturn(new BigDecimal("500"));

        service.cloturerSession(CAISSE_ID, new CloturerSessionRequest(new BigDecimal("1500"), null));

        assertThat(s.estOuverte()).isFalse();
        assertThat(s.getEcart()).isEqualByComparingTo("0");
        verify(operationCaisseService, never()).enregistrer(any(), any());
        verify(journalAuditService).tracer(argThat(e -> e.action() == TypeActionAudit.SESSION_CAISSE_CLOTUREE));
    }

    @Test
    @DisplayName("un écart sans commentaire est refusé")
    void ecartSansCommentaire() {
        sessionOuverte(caisse(TypeCaisse.PHYSIQUE, true), "1000");
        when(operationRepository.soldeSession(30L)).thenReturn(BigDecimal.ZERO);

        assertThatThrownBy(() -> service.cloturerSession(CAISSE_ID, new CloturerSessionRequest(new BigDecimal("900"), null))).isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("manque de caisse : ajustement en sortie, tracé")
    void ecartManque() {
        SessionCaisse s = sessionOuverte(caisse(TypeCaisse.PHYSIQUE, true), "1000");
        when(operationRepository.soldeSession(30L)).thenReturn(BigDecimal.ZERO);

        service.cloturerSession(CAISSE_ID, new CloturerSessionRequest(new BigDecimal("900"), "Erreur de rendu"));

        verify(operationCaisseService).enregistrer(eq(PHARMACOPEE_ID), argThat(o -> o.sens() == SensOperation.SORTIE
                && o.nature() == NatureOperationCaisse.AJUSTEMENT && o.montant().compareTo(new BigDecimal("100")) == 0));
        verify(journalAuditService).tracer(argThat(e -> e.action() == TypeActionAudit.ECART_CAISSE));
        assertThat(s.getEcart()).isEqualByComparingTo("-100");
    }

    @Test
    @DisplayName("excédent de caisse : ajustement en entrée")
    void ecartExcedent() {
        sessionOuverte(caisse(TypeCaisse.PHYSIQUE, true), "1000");
        when(operationRepository.soldeSession(30L)).thenReturn(BigDecimal.ZERO);

        service.cloturerSession(CAISSE_ID, new CloturerSessionRequest(new BigDecimal("1200"), "Oubli de saisie"));

        verify(operationCaisseService).enregistrer(eq(PHARMACOPEE_ID), argThat(o -> o.sens() == SensOperation.ENTREE));
    }

    @Test
    @DisplayName("clôture sans session ouverte refusée")
    void clotureSansSession() {
        caisse(TypeCaisse.PHYSIQUE, true);

        assertThatThrownBy(() -> service.cloturerSession(CAISSE_ID, new CloturerSessionRequest(BigDecimal.TEN, null))).isInstanceOf(ConflictException.class);
    }

    @Test
    @DisplayName("une caisse avec une session ouverte ne se désactive pas")
    void desactivationSessionOuverte() {
        sessionOuverte(caisse(TypeCaisse.PHYSIQUE, true), "0");

        assertThatThrownBy(() -> service.changerActivation(CAISSE_ID, false)).isInstanceOf(ConflictException.class);
    }

    @Test
    @DisplayName("la caisse d'une autre pharmacopée n'est pas trouvée")
    void autrePharmacopee() {
        when(caisseRepository.findByIdAndPharmacopeeId(9L, PHARMACOPEE_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getCaisse(9L)).isInstanceOf(ResourceNotFoundException.class);
    }

    private void operationSaisie(NatureOperationCaisse nature) {
        when(operationCaisseService.enregistrer(eq(PHARMACOPEE_ID), any())).thenReturn(OperationCaisse.builder().id(1L).caisse(Caisse.builder().id(CAISSE_ID).build()).nature(nature).build());
    }

    @Test
    @DisplayName("un apport est une entrée, un retrait une sortie")
    void sensManuel() {
        operationSaisie(NatureOperationCaisse.APPORT);

        service.enregistrerOperation(CAISSE_ID, new OperationManuelleRequest(NatureOperationCaisse.APPORT, BigDecimal.TEN, "Apport"));
        service.enregistrerOperation(CAISSE_ID, new OperationManuelleRequest(NatureOperationCaisse.RETRAIT, BigDecimal.TEN, "Retrait"));

        verify(operationCaisseService).enregistrer(eq(PHARMACOPEE_ID), argThat(o -> o.nature() == NatureOperationCaisse.APPORT && o.sens() == SensOperation.ENTREE));
        verify(operationCaisseService).enregistrer(eq(PHARMACOPEE_ID), argThat(o -> o.nature() == NatureOperationCaisse.RETRAIT && o.sens() == SensOperation.SORTIE));
    }

    @Test
    @DisplayName("une vente ne se saisit pas à la main")
    void natureInterdite() {
        assertThatThrownBy(() -> service.enregistrerOperation(CAISSE_ID, new OperationManuelleRequest(NatureOperationCaisse.VENTE, BigDecimal.TEN, "x")))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("un transfert vers la même caisse est refusé")
    void transfertMemeCaisse() {
        assertThatThrownBy(() -> service.transferer(new TransfertCaisseRequest(3L, 3L, BigDecimal.TEN, "x"))).isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("transfert : verrous dans un ordre fixe, sortie puis entrée liée")
    void transfert() {
        Caisse source = Caisse.builder().id(9L).build();
        Caisse destination = Caisse.builder().id(3L).build();
        when(caisseRepository.findByIdAndPharmacopeeIdForUpdate(3L, PHARMACOPEE_ID)).thenReturn(Optional.of(destination));
        when(caisseRepository.findByIdAndPharmacopeeIdForUpdate(9L, PHARMACOPEE_ID)).thenReturn(Optional.of(source));
        OperationCaisse sortie = OperationCaisse.builder().id(1L).caisse(source).build();
        when(operationCaisseService.enregistrer(eq(PHARMACOPEE_ID), argThat(o -> o != null && o.sens() == SensOperation.SORTIE))).thenReturn(sortie);
        when(operationCaisseService.enregistrer(eq(PHARMACOPEE_ID), argThat(o -> o != null && o.sens() == SensOperation.ENTREE)))
                .thenReturn(OperationCaisse.builder().id(2L).caisse(destination).operationLiee(sortie).build());

        assertThat(service.transferer(new TransfertCaisseRequest(9L, 3L, BigDecimal.TEN, "Dépôt"))).hasSize(2);

        InOrder ordre = inOrder(caisseRepository);
        ordre.verify(caisseRepository).findByIdAndPharmacopeeIdForUpdate(3L, PHARMACOPEE_ID);
        ordre.verify(caisseRepository).findByIdAndPharmacopeeIdForUpdate(9L, PHARMACOPEE_ID);
        verify(operationCaisseService, times(2)).enregistrer(eq(PHARMACOPEE_ID), any());
        verify(operationCaisseService).enregistrer(eq(PHARMACOPEE_ID), argThat(o -> o != null && o.sens() == SensOperation.ENTREE && o.operationLiee() == sortie));
    }

    private void operationExistante(OperationCaisse operation) {
        when(operationRepository.findByIdAndPharmacopeeId(70L, PHARMACOPEE_ID)).thenReturn(Optional.of(operation));
    }

    @Test
    @DisplayName("annulation d'un apport : contre-passation tracée")
    void annulationApport() {
        operationExistante(OperationCaisse.builder().id(70L).nature(NatureOperationCaisse.APPORT).build());
        when(operationCaisseService.contrePasser(eq(PHARMACOPEE_ID), eq(70L), any(), eq(membre)))
                .thenReturn(OperationCaisse.builder().id(71L).caisse(Caisse.builder().id(CAISSE_ID).build()).build());

        assertThat(service.annulerOperation(70L, "Erreur").getId()).isEqualTo(71L);
        verify(journalAuditService).tracer(argThat(e -> e.action() == TypeActionAudit.OPERATION_CAISSE_ANNULEE));
    }

    @Test
    @DisplayName("une opération issue d'une dépense s'annule depuis son origine")
    void annulationAvecOrigine() {
        operationExistante(OperationCaisse.builder().id(70L).nature(NatureOperationCaisse.DEPENSE).depense(new Depense()).build());

        assertThatThrownBy(() -> service.annulerOperation(70L, "x")).isInstanceOf(ConflictException.class);
    }

    @Test
    @DisplayName("un transfert ou un ajustement ne s'annule pas")
    void annulationInterdite() {
        for (NatureOperationCaisse nature : new NatureOperationCaisse[] {NatureOperationCaisse.TRANSFERT, NatureOperationCaisse.AJUSTEMENT, NatureOperationCaisse.ANNULATION}) {
            operationExistante(OperationCaisse.builder().id(70L).nature(nature).build());
            assertThatThrownBy(() -> service.annulerOperation(70L, "x")).isInstanceOf(ConflictException.class);
        }
    }
}
