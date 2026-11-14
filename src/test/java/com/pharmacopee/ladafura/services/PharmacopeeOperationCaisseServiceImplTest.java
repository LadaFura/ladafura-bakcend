package com.pharmacopee.ladafura.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.pharmacopee.ladafura.Models.Caisse;
import com.pharmacopee.ladafura.Models.Depense;
import com.pharmacopee.ladafura.Models.OperationCaisse;
import com.pharmacopee.ladafura.Models.PaiementVente;
import com.pharmacopee.ladafura.Models.Pharmacopee;
import com.pharmacopee.ladafura.Models.SessionCaisse;
import com.pharmacopee.ladafura.enums.MethodePaiement;
import com.pharmacopee.ladafura.enums.NatureOperationCaisse;
import com.pharmacopee.ladafura.enums.SensOperation;
import com.pharmacopee.ladafura.enums.StatutSessionCaisse;
import com.pharmacopee.ladafura.enums.TypeCaisse;
import com.pharmacopee.ladafura.exceptions.BadRequestException;
import com.pharmacopee.ladafura.exceptions.ConflictException;
import com.pharmacopee.ladafura.exceptions.ResourceNotFoundException;
import com.pharmacopee.ladafura.repository.CaisseRepository;
import com.pharmacopee.ladafura.repository.OperationCaisseRepository;
import com.pharmacopee.ladafura.repository.SessionCaisseRepository;
import com.pharmacopee.ladafura.services.impl.PharmacopeeOperationCaisseServiceImpl;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeOperationCaisseService.NouvelleOperation;

@ExtendWith(MockitoExtension.class)
class PharmacopeeOperationCaisseServiceImplTest {

    private static final Long PHARMACOPEE_ID = 5L;
    private static final Long CAISSE_ID = 3L;
    private static final Long SESSION_ID = 30L;

    @Mock private CaisseRepository caisseRepository;
    @Mock private SessionCaisseRepository sessionCaisseRepository;
    @Mock private OperationCaisseRepository operationCaisseRepository;

    @InjectMocks private PharmacopeeOperationCaisseServiceImpl service;

    private final Pharmacopee pharmacopee = Pharmacopee.builder().id(PHARMACOPEE_ID).nom("Officine").build();

    @BeforeEach
    void preparer() {
        lenient().when(operationCaisseRepository.save(any(OperationCaisse.class))).thenAnswer(i -> i.getArgument(0));
    }

    private Caisse caisse(TypeCaisse type, boolean actif) {
        Caisse caisse = Caisse.builder().id(CAISSE_ID).pharmacopee(pharmacopee).type(type).actif(actif).build();
        when(caisseRepository.findByIdAndPharmacopeeIdForUpdate(CAISSE_ID, PHARMACOPEE_ID)).thenReturn(Optional.of(caisse));
        return caisse;
    }

    private SessionCaisse sessionOuverte(String fond) {
        SessionCaisse session = SessionCaisse.builder().id(SESSION_ID).fondOuverture(new BigDecimal(fond)).statut(StatutSessionCaisse.OUVERTE).build();
        when(sessionCaisseRepository.findFirstByCaisseIdAndStatut(CAISSE_ID, StatutSessionCaisse.OUVERTE)).thenReturn(Optional.of(session));
        return session;
    }

    private static NouvelleOperation.NouvelleOperationBuilder operation(SensOperation sens, MethodePaiement methode, String montant) {
        return NouvelleOperation.builder().caisseId(CAISSE_ID).sens(sens).nature(NatureOperationCaisse.VENTE)
                .methode(methode).montant(new BigDecimal(montant));
    }

    @Test
    @DisplayName("un encaissement en espèces est rattaché à la session ouverte")
    void especesDansSession() {
        caisse(TypeCaisse.PHYSIQUE, true);
        SessionCaisse session = sessionOuverte("1000.00");

        OperationCaisse resultat = service.enregistrer(PHARMACOPEE_ID, operation(SensOperation.ENTREE, MethodePaiement.CASH, "2500.00").build());

        assertThat(resultat.getSession()).isSameAs(session);
        assertThat(resultat.getPharmacopee()).isSameAs(pharmacopee);
    }

    @Test
    @DisplayName("des espèces sans session ouverte sont refusées")
    void especesSansSession() {
        caisse(TypeCaisse.PHYSIQUE, true);
        when(sessionCaisseRepository.findFirstByCaisseIdAndStatut(CAISSE_ID, StatutSessionCaisse.OUVERTE)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.enregistrer(PHARMACOPEE_ID, operation(SensOperation.ENTREE, MethodePaiement.CASH, "100.00").build()))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    @DisplayName("un compte Mobile Money n'a pas besoin de session")
    void mobileMoneySansSession() {
        caisse(TypeCaisse.MOBILE_MONEY, true);

        OperationCaisse resultat = service.enregistrer(PHARMACOPEE_ID, operation(SensOperation.ENTREE, MethodePaiement.MOBILE_MONEY, "100.00").build());

        assertThat(resultat.getSession()).isNull();
    }

    @Test
    @DisplayName("un mode de paiement incompatible avec la caisse est refusé")
    void modeIncompatible() {
        caisse(TypeCaisse.PHYSIQUE, true);

        assertThatThrownBy(() -> service.enregistrer(PHARMACOPEE_ID, operation(SensOperation.ENTREE, MethodePaiement.MOBILE_MONEY, "100.00").build()))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("une caisse désactivée est refusée")
    void caisseInactive() {
        caisse(TypeCaisse.MOBILE_MONEY, false);

        assertThatThrownBy(() -> service.enregistrer(PHARMACOPEE_ID, operation(SensOperation.ENTREE, MethodePaiement.MOBILE_MONEY, "100.00").build()))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("une caisse d'une autre pharmacopée n'est pas trouvée")
    void autrePharmacopee() {
        when(caisseRepository.findByIdAndPharmacopeeIdForUpdate(CAISSE_ID, PHARMACOPEE_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.enregistrer(PHARMACOPEE_ID, operation(SensOperation.ENTREE, null, "100.00").build()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("un montant nul est refusé")
    void montantNul() {
        assertThatThrownBy(() -> service.enregistrer(PHARMACOPEE_ID, operation(SensOperation.ENTREE, null, "0").build()))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("deux origines sur une même opération sont refusées")
    void deuxOrigines() {
        NouvelleOperation operation = operation(SensOperation.ENTREE, null, "100.00")
                .paiementVente(new PaiementVente()).depense(new Depense()).build();

        assertThatThrownBy(() -> service.enregistrer(PHARMACOPEE_ID, operation)).isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("une sortie peut utiliser le fond d'ouverture")
    void sortieSurFond() {
        caisse(TypeCaisse.PHYSIQUE, true);
        sessionOuverte("5000.00");
        when(operationCaisseRepository.soldeSession(SESSION_ID)).thenReturn(BigDecimal.ZERO);

        assertThat(service.enregistrer(PHARMACOPEE_ID, operation(SensOperation.SORTIE, MethodePaiement.CASH, "5000.00").build())).isNotNull();
    }

    @Test
    @DisplayName("une sortie ne peut pas rendre négatif le solde d'une caisse physique")
    void soldeNegatifRefuse() {
        caisse(TypeCaisse.PHYSIQUE, true);
        sessionOuverte("5000.00");
        when(operationCaisseRepository.soldeSession(SESSION_ID)).thenReturn(new BigDecimal("-1000.00"));

        assertThatThrownBy(() -> service.enregistrer(PHARMACOPEE_ID, operation(SensOperation.SORTIE, MethodePaiement.CASH, "4000.01").build()))
                .isInstanceOf(ConflictException.class);
        verify(operationCaisseRepository, never()).save(any());
    }

    private OperationCaisse originale(Caisse caisse, SensOperation sens, NatureOperationCaisse nature) {
        OperationCaisse originale = OperationCaisse.builder().id(70L).caisse(caisse).sens(sens).nature(nature).montant(new BigDecimal("1500.00")).build();
        when(operationCaisseRepository.findByIdAndPharmacopeeId(70L, PHARMACOPEE_ID)).thenReturn(Optional.of(originale));
        return originale;
    }

    @Test
    @DisplayName("la contre-passation inverse le sens et se lie à l'opération d'origine")
    void contrePassation() {
        Caisse caisse = caisse(TypeCaisse.MOBILE_MONEY, true);
        OperationCaisse originale = originale(caisse, SensOperation.ENTREE, NatureOperationCaisse.VENTE);
        when(operationCaisseRepository.existsByOperationLieeIdAndNature(70L, NatureOperationCaisse.ANNULATION)).thenReturn(false);

        OperationCaisse inverse = service.contrePasser(PHARMACOPEE_ID, 70L, "Vente annulée", null);

        assertThat(inverse.getSens()).isEqualTo(SensOperation.SORTIE);
        assertThat(inverse.getNature()).isEqualTo(NatureOperationCaisse.ANNULATION);
        assertThat(inverse.getOperationLiee()).isSameAs(originale);
        assertThat(inverse.getMontant()).isEqualByComparingTo("1500.00");
    }

    @Test
    @DisplayName("une opération déjà contre-passée est refusée")
    void contrePassationDouble() {
        Caisse caisse = Caisse.builder().id(CAISSE_ID).type(TypeCaisse.MOBILE_MONEY).build();
        originale(caisse, SensOperation.ENTREE, NatureOperationCaisse.VENTE);
        when(operationCaisseRepository.existsByOperationLieeIdAndNature(70L, NatureOperationCaisse.ANNULATION)).thenReturn(true);

        assertThatThrownBy(() -> service.contrePasser(PHARMACOPEE_ID, 70L, "Encore", null)).isInstanceOf(ConflictException.class);
    }

    @Test
    @DisplayName("une contre-passation ne se contre-passe pas")
    void contrePasserUneAnnulation() {
        Caisse caisse = Caisse.builder().id(CAISSE_ID).type(TypeCaisse.MOBILE_MONEY).build();
        originale(caisse, SensOperation.SORTIE, NatureOperationCaisse.ANNULATION);

        assertThatThrownBy(() -> service.contrePasser(PHARMACOPEE_ID, 70L, "Encore", null)).isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("solde d'une caisse physique ouverte : fond plus opérations de la session")
    void soldeCaisseOuverte() {
        when(caisseRepository.findByIdAndPharmacopeeId(CAISSE_ID, PHARMACOPEE_ID))
                .thenReturn(Optional.of(Caisse.builder().id(CAISSE_ID).type(TypeCaisse.PHYSIQUE).build()));
        when(sessionCaisseRepository.findFirstByCaisseIdOrderByDateOuvertureDesc(CAISSE_ID)).thenReturn(Optional.of(
                SessionCaisse.builder().id(SESSION_ID).fondOuverture(new BigDecimal("1000.00")).statut(StatutSessionCaisse.OUVERTE).build()));
        when(operationCaisseRepository.soldeSession(SESSION_ID)).thenReturn(new BigDecimal("500.00"));

        assertThat(service.soldeTheorique(PHARMACOPEE_ID, CAISSE_ID)).isEqualByComparingTo("1500.00");
    }

    @Test
    @DisplayName("solde d'une caisse physique fermée : montant constaté à la dernière clôture")
    void soldeCaisseFermee() {
        when(caisseRepository.findByIdAndPharmacopeeId(CAISSE_ID, PHARMACOPEE_ID))
                .thenReturn(Optional.of(Caisse.builder().id(CAISSE_ID).type(TypeCaisse.PHYSIQUE).build()));
        when(sessionCaisseRepository.findFirstByCaisseIdOrderByDateOuvertureDesc(CAISSE_ID)).thenReturn(Optional.of(
                SessionCaisse.builder().montantConstate(new BigDecimal("7400.00")).statut(StatutSessionCaisse.CLOTUREE).build()));

        assertThat(service.soldeTheorique(PHARMACOPEE_ID, CAISSE_ID)).isEqualByComparingTo("7400.00");
    }

    @Test
    @DisplayName("solde d'un compte Mobile Money : entrées moins sorties")
    void soldeMobileMoney() {
        when(caisseRepository.findByIdAndPharmacopeeId(CAISSE_ID, PHARMACOPEE_ID))
                .thenReturn(Optional.of(Caisse.builder().id(CAISSE_ID).type(TypeCaisse.MOBILE_MONEY).build()));
        when(operationCaisseRepository.soldeTheorique(CAISSE_ID)).thenReturn(new BigDecimal("900.00"));

        assertThat(service.soldeTheorique(PHARMACOPEE_ID, CAISSE_ID)).isEqualByComparingTo("900.00");
    }
}
