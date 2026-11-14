package com.pharmacopee.ladafura.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.pharmacopee.ladafura.Models.Caisse;
import com.pharmacopee.ladafura.Models.Paiement;
import com.pharmacopee.ladafura.Models.Pharmacopee;
import com.pharmacopee.ladafura.dto.population.paiement.PopulationCompteMobileMoneyResponse;
import com.pharmacopee.ladafura.enums.MethodePaiement;
import com.pharmacopee.ladafura.enums.StatutPaiement;
import com.pharmacopee.ladafura.enums.StatutPharmacopee;
import com.pharmacopee.ladafura.enums.TypeCaisse;
import com.pharmacopee.ladafura.exceptions.BadRequestException;
import com.pharmacopee.ladafura.exceptions.ConflictException;
import com.pharmacopee.ladafura.repository.CaisseRepository;
import com.pharmacopee.ladafura.repository.PaiementRepository;
import com.pharmacopee.ladafura.repository.PharmacopeeRepository;
import com.pharmacopee.ladafura.services.impl.PaiementDeclareServiceImpl;

@ExtendWith(MockitoExtension.class)
class PaiementDeclareServiceImplTest {

    private static final Long PHARMACOPEE_ID = 1L;
    private static final Long COMPTE_ID = 12L;
    private static final String TELEPHONE = "+223 70 11 22 33";

    @Mock
    private CaisseRepository caisseRepository;
    @Mock
    private PaiementRepository paiementRepository;
    @Mock
    private PharmacopeeRepository pharmacopeeRepository;

    @InjectMocks
    private PaiementDeclareServiceImpl service;

    private Paiement paiement;
    private Caisse compte;

    @BeforeEach
    void setUp() {
        paiement = new Paiement();
        compte = new Caisse();
        compte.setId(COMPTE_ID);
        compte.setNom("Orange Money");
        compte.setType(TypeCaisse.MOBILE_MONEY);
        compte.setActif(true);
        compte.setReferenceCompte("70 11 22 33");
    }

    @Test
    @DisplayName("declarerMobileMoney - le paiement reste EN_ATTENTE, jamais REUSSI")
    void declarer_ResteEnAttente() {
        when(caisseRepository.findByIdAndPharmacopeeId(COMPTE_ID, PHARMACOPEE_ID)).thenReturn(Optional.of(compte));

        service.declarerMobileMoney(paiement, PHARMACOPEE_ID, COMPTE_ID, " om 998 811 ", TELEPHONE);

        assertThat(paiement.getStatut()).isEqualTo(StatutPaiement.EN_ATTENTE);
        assertThat(paiement.getCaisse()).isSameAs(compte);
        assertThat(paiement.getReference()).isEqualTo("OM998811");
        assertThat(paiement.getTelephoneEmetteur()).isEqualTo(TELEPHONE);
    }

    @Test
    @DisplayName("declarerMobileMoney - une nouvelle déclaration efface le motif de rejet")
    void declarer_EffaceMotifRejet() {
        paiement.setStatut(StatutPaiement.ECHOUE);
        paiement.setMotifRejet("Aucun paiement reçu");
        when(caisseRepository.findByIdAndPharmacopeeId(COMPTE_ID, PHARMACOPEE_ID)).thenReturn(Optional.of(compte));

        service.declarerMobileMoney(paiement, PHARMACOPEE_ID, COMPTE_ID, "OM-2", TELEPHONE);

        assertThat(paiement.getStatut()).isEqualTo(StatutPaiement.EN_ATTENTE);
        assertThat(paiement.getMotifRejet()).isNull();
    }

    @Test
    @DisplayName("declarerMobileMoney - compte d'une autre pharmacopée -> 400")
    void declarer_CompteAutrePharmacopee() {
        when(caisseRepository.findByIdAndPharmacopeeId(COMPTE_ID, PHARMACOPEE_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.declarerMobileMoney(paiement, PHARMACOPEE_ID, COMPTE_ID, "OM-1", TELEPHONE))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("declarerMobileMoney - compte inactif ou d'un autre type -> 400")
    void declarer_CompteNonValable() {
        compte.setType(TypeCaisse.BANQUE);
        when(caisseRepository.findByIdAndPharmacopeeId(COMPTE_ID, PHARMACOPEE_ID)).thenReturn(Optional.of(compte));

        assertThatThrownBy(() -> service.declarerMobileMoney(paiement, PHARMACOPEE_ID, COMPTE_ID, "OM-1", TELEPHONE))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("declarerMobileMoney - numéro de transaction manquant -> 400")
    void declarer_ReferenceManquante() {
        assertThatThrownBy(() -> service.declarerMobileMoney(paiement, PHARMACOPEE_ID, COMPTE_ID, " ", TELEPHONE))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("numéro de transaction");
    }

    @Test
    @DisplayName("declarerMobileMoney - numéro de transaction déjà déclaré -> 409")
    void declarer_ReferenceEnDouble() {
        when(caisseRepository.findByIdAndPharmacopeeId(COMPTE_ID, PHARMACOPEE_ID)).thenReturn(Optional.of(compte));
        when(paiementRepository.referenceDejaDeclaree(COMPTE_ID, "OM-1", StatutPaiement.ECHOUE, null)).thenReturn(true);

        assertThatThrownBy(() -> service.declarerMobileMoney(paiement, PHARMACOPEE_ID, COMPTE_ID, "om-1", TELEPHONE))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    @DisplayName("exigerMethodeDisponible - carte bancaire -> 400, Mobile Money et espèces acceptés")
    void methodeDisponible() {
        assertThatThrownBy(() -> service.exigerMethodeDisponible(MethodePaiement.CARTE_BANCAIRE))
                .isInstanceOf(BadRequestException.class);
        service.exigerMethodeDisponible(MethodePaiement.MOBILE_MONEY);
        service.exigerMethodeDisponible(MethodePaiement.CASH);
    }

    @Test
    @DisplayName("getComptesMobileMoney - seuls les comptes Mobile Money avec un numéro sont proposés")
    void comptes_Filtres() {
        Pharmacopee validee = new Pharmacopee();
        validee.setStatut(StatutPharmacopee.VALIDEE);
        Caisse sansNumero = new Caisse();
        sansNumero.setType(TypeCaisse.MOBILE_MONEY);
        Caisse physique = new Caisse();
        physique.setType(TypeCaisse.PHYSIQUE);
        physique.setReferenceCompte("CAISSE-1");
        when(pharmacopeeRepository.findById(PHARMACOPEE_ID)).thenReturn(Optional.of(validee));
        when(caisseRepository.findByPharmacopeeIdAndActifTrue(PHARMACOPEE_ID)).thenReturn(List.of(compte, sansNumero, physique));

        List<PopulationCompteMobileMoneyResponse> comptes = service.getComptesMobileMoney(PHARMACOPEE_ID);

        assertThat(comptes).extracting(PopulationCompteMobileMoneyResponse::getId).containsExactly(COMPTE_ID);
    }

    @Test
    @DisplayName("getComptesMobileMoney - pharmacopée non validée : aucun numéro exposé")
    void comptes_PharmacopeeNonValidee() {
        Pharmacopee enAttente = new Pharmacopee();
        enAttente.setStatut(StatutPharmacopee.EN_ATTENTE);
        when(pharmacopeeRepository.findById(PHARMACOPEE_ID)).thenReturn(Optional.of(enAttente));

        assertThat(service.getComptesMobileMoney(PHARMACOPEE_ID)).isEmpty();
    }
}
