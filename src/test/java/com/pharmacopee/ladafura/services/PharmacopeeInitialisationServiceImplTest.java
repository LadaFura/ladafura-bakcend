package com.pharmacopee.ladafura.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.pharmacopee.ladafura.Models.AttributionRolePharmacopee;
import com.pharmacopee.ladafura.Models.Caisse;
import com.pharmacopee.ladafura.Models.MembrePharmacopee;
import com.pharmacopee.ladafura.Models.Pharmacopee;
import com.pharmacopee.ladafura.Models.RolePharmacopee;
import com.pharmacopee.ladafura.Models.Utilisateur;
import com.pharmacopee.ladafura.enums.StatutMembre;
import com.pharmacopee.ladafura.enums.TypeCaisse;
import com.pharmacopee.ladafura.repository.AttributionRolePharmacopeeRepository;
import com.pharmacopee.ladafura.repository.CaisseRepository;
import com.pharmacopee.ladafura.repository.MembrePharmacopeeRepository;
import com.pharmacopee.ladafura.repository.RolePharmacopeeRepository;
import com.pharmacopee.ladafura.services.impl.PharmacopeeInitialisationServiceImpl;
import com.pharmacopee.ladafura.services.interfaces.IAbonnementCycleService;

@ExtendWith(MockitoExtension.class)
class PharmacopeeInitialisationServiceImplTest {

    @Mock private MembrePharmacopeeRepository membreRepository;
    @Mock private AttributionRolePharmacopeeRepository attributionRepository;
    @Mock private RolePharmacopeeRepository roleRepository;
    @Mock private CaisseRepository caisseRepository;
    @Mock private IAbonnementCycleService abonnementCycleService;

    @InjectMocks private PharmacopeeInitialisationServiceImpl service;

    private final Pharmacopee pharmacopee = Pharmacopee.builder().id(1L).nom("Officine de Kayes").build();
    private final RolePharmacopee gerant = RolePharmacopee.builder().id(1L).code("GERANT").build();
    private final RolePharmacopee manager = RolePharmacopee.builder().id(2L).code("MANAGER").build();
    private final Utilisateur proprietaire = utilisateur(10L);

    private static Utilisateur utilisateur(Long id) {
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setId(id);
        return utilisateur;
    }

    @BeforeEach
    void preparer() {
        lenient().when(roleRepository.findByCodeAndPharmacopeeIsNull("GERANT")).thenReturn(Optional.of(gerant));
        lenient().when(roleRepository.findByCodeAndPharmacopeeIsNull("MANAGER")).thenReturn(Optional.of(manager));
        lenient().when(membreRepository.saveAndFlush(any(MembrePharmacopee.class))).thenAnswer(appel -> appel.getArgument(0));
    }

    @Test
    @DisplayName("une nouvelle pharmacopée reçoit son gérant, l'offre gratuite et ses trois comptes")
    void initialisationComplete() {
        service.initialiser(pharmacopee, proprietaire);

        ArgumentCaptor<MembrePharmacopee> membre = ArgumentCaptor.forClass(MembrePharmacopee.class);
        verify(membreRepository).saveAndFlush(membre.capture());
        assertThat(membre.getValue().getEstProprietaire()).isTrue();
        assertThat(membre.getValue().getStatut()).isEqualTo(StatutMembre.ACTIF);

        ArgumentCaptor<AttributionRolePharmacopee> attribution = ArgumentCaptor.forClass(AttributionRolePharmacopee.class);
        verify(attributionRepository).save(attribution.capture());
        assertThat(attribution.getValue().getRolePharmacopee()).isSameAs(gerant);

        verify(abonnementCycleService).attribuerGratuit(pharmacopee, proprietaire);
        ArgumentCaptor<Caisse> comptes = ArgumentCaptor.forClass(Caisse.class);
        verify(caisseRepository, times(3)).save(comptes.capture());
        assertThat(comptes.getAllValues()).extracting(Caisse::getType)
                .containsExactly(TypeCaisse.PHYSIQUE, TypeCaisse.MOBILE_MONEY, TypeCaisse.BANQUE);
    }

    @Test
    @DisplayName("sans titulaire, l'abonnement et les comptes sont créés, mais aucun membre")
    void sansTitulaire() {
        service.initialiser(pharmacopee, null);

        verify(membreRepository, never()).saveAndFlush(any());
        verify(abonnementCycleService).attribuerGratuit(pharmacopee, null);
        verify(caisseRepository, times(3)).save(any(Caisse.class));
    }

    @Test
    @DisplayName("changer de titulaire écrit d'abord l'ancien gérant, qui devient MANAGER")
    void changementDeTitulaire() {
        MembrePharmacopee ancien = MembrePharmacopee.builder().id(5L).utilisateur(utilisateur(20L)).pharmacopee(pharmacopee)
                .statut(StatutMembre.ACTIF).estProprietaire(true).build();
        AttributionRolePharmacopee attributionGerant = AttributionRolePharmacopee.builder().membrePharmacopee(ancien).rolePharmacopee(gerant).build();
        when(membreRepository.findFirstByPharmacopeeIdAndEstProprietaireTrue(1L)).thenReturn(Optional.of(ancien));
        when(attributionRepository.findByMembrePharmacopeeIdAndRolePharmacopeeId(5L, 1L)).thenReturn(Optional.of(attributionGerant));

        service.definirProprietaire(pharmacopee, proprietaire);

        assertThat(ancien.getEstProprietaire()).isFalse();
        verify(attributionRepository).delete(attributionGerant);
        verify(attributionRepository, times(2)).save(any(AttributionRolePharmacopee.class));
    }

    @Test
    @DisplayName("un praticien détaché par l'administrateur est retiré, jamais le gérant")
    void detachement() {
        MembrePharmacopee detache = MembrePharmacopee.builder().id(6L).statut(StatutMembre.ACTIF).estProprietaire(false).build();
        MembrePharmacopee titulaire = MembrePharmacopee.builder().id(7L).statut(StatutMembre.ACTIF).estProprietaire(true).build();
        when(membreRepository.findByUtilisateurIdAndPharmacopeeId(30L, 1L)).thenReturn(Optional.of(detache));
        when(membreRepository.findByUtilisateurIdAndPharmacopeeId(31L, 1L)).thenReturn(Optional.of(titulaire));

        service.synchroniserPraticiens(pharmacopee, List.of(), List.of(30L, 31L));

        assertThat(detache.getStatut()).isEqualTo(StatutMembre.RETIRE);
        assertThat(titulaire.getStatut()).isEqualTo(StatutMembre.ACTIF);
        verify(attributionRepository, never()).existsByMembrePharmacopeeIdAndRolePharmacopeeId(anyLong(), anyLong());
    }
}
