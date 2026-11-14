package com.pharmacopee.ladafura.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.pharmacopee.ladafura.Models.Depense;
import com.pharmacopee.ladafura.Models.MembrePharmacopee;
import com.pharmacopee.ladafura.Models.OperationCaisse;
import com.pharmacopee.ladafura.Models.Pharmacopee;
import com.pharmacopee.ladafura.dto.pharmacopee.depense.EnregistrerDepenseRequest;
import com.pharmacopee.ladafura.dto.pharmacopee.tresorerie.SyntheseTresorerieResponse;
import com.pharmacopee.ladafura.enums.CategorieDepense;
import com.pharmacopee.ladafura.enums.MethodePaiement;
import com.pharmacopee.ladafura.enums.NatureOperationCaisse;
import com.pharmacopee.ladafura.enums.SensOperation;
import com.pharmacopee.ladafura.enums.StatutEcriture;
import com.pharmacopee.ladafura.enums.TypeCaisse;
import com.pharmacopee.ladafura.exceptions.BadRequestException;
import com.pharmacopee.ladafura.exceptions.ConflictException;
import com.pharmacopee.ladafura.exceptions.ResourceNotFoundException;
import com.pharmacopee.ladafura.repository.CommandeRepository;
import com.pharmacopee.ladafura.repository.DepenseRepository;
import com.pharmacopee.ladafura.repository.OperationCaisseRepository;
import com.pharmacopee.ladafura.repository.VenteRepository;
import com.pharmacopee.ladafura.services.impl.PharmacopeeDepenseServiceImpl;
import com.pharmacopee.ladafura.services.impl.PharmacopeeTresorerieServiceImpl;
import com.pharmacopee.ladafura.services.interfaces.IJournalAuditService;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeContexteService;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeContexteService.ContexteMembre;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeOperationCaisseService;

class PharmacopeeDepenseEtTresorerieServiceImplTest {

    private static final Long PHARMACOPEE_ID = 5L;

    private final IPharmacopeeContexteService contexteService = mock(IPharmacopeeContexteService.class);
    private final IPharmacopeeOperationCaisseService operationCaisseService = mock(IPharmacopeeOperationCaisseService.class);
    private final DepenseRepository depenseRepository = mock(DepenseRepository.class);
    private final OperationCaisseRepository operationRepository = mock(OperationCaisseRepository.class);
    private final VenteRepository venteRepository = mock(VenteRepository.class);
    private final CommandeRepository commandeRepository = mock(CommandeRepository.class);

    private final PharmacopeeDepenseServiceImpl depenses = new PharmacopeeDepenseServiceImpl(
            contexteService, operationCaisseService, mock(IJournalAuditService.class), depenseRepository, operationRepository);
    private final PharmacopeeTresorerieServiceImpl tresorerie = new PharmacopeeTresorerieServiceImpl(
            contexteService, venteRepository, commandeRepository, operationRepository, depenseRepository);

    private final MembrePharmacopee membre = MembrePharmacopee.builder().id(1L).build();

    @BeforeEach
    void preparer() {
        ContexteMembre contexte = new ContexteMembre(membre, Pharmacopee.builder().id(PHARMACOPEE_ID).build(), Set.of());
        when(contexteService.verifierPermission(anyString())).thenReturn(contexte);
        when(contexteService.verifierOperationCommerciale(anyString())).thenReturn(contexte);
        when(depenseRepository.save(any(Depense.class))).thenAnswer(i -> i.getArgument(0));
    }

    @Test
    @DisplayName("une dépense crée une sortie de caisse de nature DEPENSE")
    void creationDepense() {
        depenses.creer(EnregistrerDepenseRequest.builder().libelle("Loyer").categorie(CategorieDepense.LOYER).montant(new BigDecimal("50000"))
                .caisseId(3L).methode(MethodePaiement.MOBILE_MONEY).build());

        verify(operationCaisseService).enregistrer(eq(PHARMACOPEE_ID), argThat(o -> o.sens() == SensOperation.SORTIE
                && o.nature() == NatureOperationCaisse.DEPENSE && o.depense() != null && o.caisseId().equals(3L)));
    }

    private Depense depense(StatutEcriture statut) {
        Depense d = Depense.builder().id(40L).statut(statut).build();
        when(depenseRepository.findByIdAndPharmacopeeId(40L, PHARMACOPEE_ID)).thenReturn(Optional.of(d));
        return d;
    }

    @Test
    @DisplayName("annuler une dépense contre-passe son opération")
    void annulationDepense() {
        Depense d = depense(StatutEcriture.VALIDE);
        when(operationRepository.findFirstByDepenseIdAndNatureNot(40L, NatureOperationCaisse.ANNULATION)).thenReturn(Optional.of(OperationCaisse.builder().id(70L).build()));

        depenses.annuler(40L, "Saisie en double");

        verify(operationCaisseService).contrePasser(eq(PHARMACOPEE_ID), eq(70L), any(), eq(membre));
        assertThat(d.getStatut()).isEqualTo(StatutEcriture.ANNULE);
        assertThat(d.getMotifAnnulation()).isEqualTo("Saisie en double");
    }

    @Test
    @DisplayName("une dépense déjà annulée ne s'annule pas deux fois")
    void annulationDouble() {
        depense(StatutEcriture.ANNULE);

        assertThatThrownBy(() -> depenses.annuler(40L, "x")).isInstanceOf(ConflictException.class);
    }

    @Test
    @DisplayName("la dépense d'une autre pharmacopée n'est pas trouvée")
    void autrePharmacopee() {
        when(depenseRepository.findByIdAndPharmacopeeId(40L, PHARMACOPEE_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> depenses.getDepense(40L)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("synthèse : chiffres séparés, entrées et sorties sur une ligne par compte")
    void synthese() {
        LocalDate debut = LocalDate.of(2026, 11, 1);
        LocalDate fin = LocalDate.of(2026, 11, 30);
        when(venteRepository.chiffreAffaires(eq(PHARMACOPEE_ID), any(), any())).thenReturn(new BigDecimal("15000"));
        when(commandeRepository.sumMontantTotalParPeriode(eq(PHARMACOPEE_ID), any(), any(), any())).thenReturn(9000.0);
        List<Object[]> flux = Arrays.asList(
                new Object[] {3L, "Caisse", TypeCaisse.PHYSIQUE, SensOperation.ENTREE, new BigDecimal("15000")},
                new Object[] {3L, "Caisse", TypeCaisse.PHYSIQUE, SensOperation.SORTIE, new BigDecimal("2000")});
        when(operationRepository.fluxParCompte(eq(PHARMACOPEE_ID), any(), any())).thenReturn(flux);
        when(depenseRepository.totauxParCategorie(eq(PHARMACOPEE_ID), any(), any()))
                .thenReturn(java.util.Collections.singletonList(new Object[] {CategorieDepense.LOYER, new BigDecimal("2000")}));

        SyntheseTresorerieResponse synthese = tresorerie.getSynthese(debut, fin);

        assertThat(synthese.getChiffreAffairesComptoir()).isEqualByComparingTo("15000");
        assertThat(synthese.getChiffreAffairesCommandes()).isEqualByComparingTo("9000");
        assertThat(synthese.getComptes()).singleElement().satisfies(c -> {
            assertThat(c.getEntrees()).isEqualByComparingTo("15000");
            assertThat(c.getSorties()).isEqualByComparingTo("2000");
        });
        assertThat(synthese.getDepenses()).singleElement().satisfies(d -> assertThat(d.getCategorie()).isEqualTo(CategorieDepense.LOYER));
    }

    @Test
    @DisplayName("une période inversée est refusée")
    void periodeInvalide() {
        assertThatThrownBy(() -> tresorerie.getSynthese(LocalDate.of(2026, 12, 1), LocalDate.of(2026, 11, 1))).isInstanceOf(BadRequestException.class);
    }
}
