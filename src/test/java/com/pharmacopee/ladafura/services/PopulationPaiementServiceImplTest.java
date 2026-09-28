package com.pharmacopee.ladafura.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.pharmacopee.ladafura.Models.Commande;
import com.pharmacopee.ladafura.Models.Paiement;
import com.pharmacopee.ladafura.Models.Utilisateur;
import com.pharmacopee.ladafura.dto.population.paiement.PopulationMethodePaiementInfoDto;
import com.pharmacopee.ladafura.dto.population.paiement.PopulationPaiementResponse;
import com.pharmacopee.ladafura.dto.population.paiement.PopulationProcessPaiementRequest;
import com.pharmacopee.ladafura.enums.MethodePaiement;
import com.pharmacopee.ladafura.enums.Role;
import com.pharmacopee.ladafura.enums.StatutCommande;
import com.pharmacopee.ladafura.enums.StatutPaiement;
import com.pharmacopee.ladafura.enums.StatutUtilisateur;
import com.pharmacopee.ladafura.exceptions.BadRequestException;
import com.pharmacopee.ladafura.mappers.PopulationPaiementMapper;
import com.pharmacopee.ladafura.repository.CommandeRepository;
import com.pharmacopee.ladafura.repository.PaiementRepository;
import com.pharmacopee.ladafura.services.impl.PopulationPaiementServiceImpl;
import com.pharmacopee.ladafura.services.interfaces.IPopulationAuthService;

@ExtendWith(MockitoExtension.class)
class PopulationPaiementServiceImplTest {

    @Mock
    private IPopulationAuthService populationAuthService;

    @Mock
    private PaiementRepository paiementRepository;

    @Mock
    private CommandeRepository commandeRepository;

    @Spy
    private PopulationPaiementMapper mapper = new PopulationPaiementMapper();

    @InjectMocks
    private PopulationPaiementServiceImpl paiementService;

    private Utilisateur currentUser;
    private Commande commande;

    @BeforeEach
    void setUp() {
        currentUser = new Utilisateur();
        currentUser.setId(10L);
        currentUser.setEmail("client@ladafura.ml");
        currentUser.setNom("Keita");
        currentUser.setPrenom("Salif");
        currentUser.setRole(Role.POPULATION);
        currentUser.setStatut(StatutUtilisateur.ACTIF);

        commande = Commande.builder()
                .id(1L)
                .numero("CMD-2026-001")
                .dateCommande(LocalDateTime.now())
                .statut(StatutCommande.EN_ATTENTE)
                .totalProduit(5000.0)
                .montantLivraison(1500.0)
                .montantTotal(6500.0)
                .utilisateur(currentUser)
                .build();
    }

    @Test
    @DisplayName("getMethodesPaiement - Retourne les 3 méthodes supportées")
    void getMethodesPaiement_Success() {
        List<PopulationMethodePaiementInfoDto> methodes = paiementService.getMethodesPaiement();

        assertThat(methodes).isNotNull();
        assertThat(methodes).hasSize(3);
        assertThat(methodes.stream().anyMatch(m -> m.getCode() == MethodePaiement.MOBILE_MONEY)).isTrue();
        assertThat(methodes.stream().anyMatch(m -> m.getCode() == MethodePaiement.CASH)).isTrue();
        assertThat(methodes.stream().anyMatch(m -> m.getCode() == MethodePaiement.CARTE_BANCAIRE)).isTrue();
    }

    @Test
    @DisplayName("payerCommande - Succès Mobile Money : Paiement REUSSI et Commande CONFIRMEE")
    void payerCommande_MobileMoney_Success() {
        PopulationProcessPaiementRequest request = PopulationProcessPaiementRequest.builder()
                .commandeId(1L)
                .methode(MethodePaiement.MOBILE_MONEY)
                .operateur("ORANGE_MONEY")
                .telephoneMobileMoney("+223 70 11 22 33")
                .simulerSucces(true)
                .build();

        when(populationAuthService.getCurrentPopulationUser()).thenReturn(currentUser);
        when(commandeRepository.findByIdAndUtilisateurId(1L, 10L)).thenReturn(Optional.of(commande));
        when(paiementRepository.findByCommandeId(1L)).thenReturn(Optional.empty());
        when(paiementRepository.save(any(Paiement.class))).thenAnswer(invocation -> {
            Paiement p = invocation.getArgument(0);
            p.setId(50L);
            return p;
        });

        PopulationPaiementResponse response = paiementService.payerCommande(request);

        assertThat(response).isNotNull();
        assertThat(response.getStatut()).isEqualTo(StatutPaiement.REUSSI);
        assertThat(response.getSucces()).isTrue();
        assertThat(response.getMontant()).isEqualTo(6500.0);
        assertThat(commande.getStatut()).isEqualTo(StatutCommande.CONFIRMEE);
        verify(commandeRepository).save(commande);
    }

    @Test
    @DisplayName("payerCommande - Succès Cash : Paiement EN_ATTENTE et Commande CONFIRMEE")
    void payerCommande_Cash_Success() {
        PopulationProcessPaiementRequest request = PopulationProcessPaiementRequest.builder()
                .commandeId(1L)
                .methode(MethodePaiement.CASH)
                .build();

        when(populationAuthService.getCurrentPopulationUser()).thenReturn(currentUser);
        when(commandeRepository.findByIdAndUtilisateurId(1L, 10L)).thenReturn(Optional.of(commande));
        when(paiementRepository.findByCommandeId(1L)).thenReturn(Optional.empty());
        when(paiementRepository.save(any(Paiement.class))).thenAnswer(invocation -> {
            Paiement p = invocation.getArgument(0);
            p.setId(51L);
            return p;
        });

        PopulationPaiementResponse response = paiementService.payerCommande(request);

        assertThat(response).isNotNull();
        assertThat(response.getStatut()).isEqualTo(StatutPaiement.EN_ATTENTE);
        assertThat(response.getMessage()).contains("en espèces");
        assertThat(commande.getStatut()).isEqualTo(StatutCommande.CONFIRMEE);
    }

    @Test
    @DisplayName("payerCommande - Échec simulé : Paiement ECHOUE et Commande inchangée")
    void payerCommande_SimulatedFailure() {
        PopulationProcessPaiementRequest request = PopulationProcessPaiementRequest.builder()
                .commandeId(1L)
                .methode(MethodePaiement.MOBILE_MONEY)
                .operateur("WAVE")
                .simulerSucces(false)
                .build();

        when(populationAuthService.getCurrentPopulationUser()).thenReturn(currentUser);
        when(commandeRepository.findByIdAndUtilisateurId(1L, 10L)).thenReturn(Optional.of(commande));
        when(paiementRepository.findByCommandeId(1L)).thenReturn(Optional.empty());
        when(paiementRepository.save(any(Paiement.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PopulationPaiementResponse response = paiementService.payerCommande(request);

        assertThat(response).isNotNull();
        assertThat(response.getStatut()).isEqualTo(StatutPaiement.ECHOUE);
        assertThat(response.getSucces()).isFalse();
        assertThat(commande.getStatut()).isEqualTo(StatutCommande.EN_ATTENTE);
    }

    @Test
    @DisplayName("payerCommande - Commande déjà réglée avec succès -> BadRequestException")
    void payerCommande_AlreadyPaid() {
        Paiement existing = Paiement.builder()
                .id(99L)
                .montant(6500.0)
                .statut(StatutPaiement.REUSSI)
                .build();

        PopulationProcessPaiementRequest request = PopulationProcessPaiementRequest.builder()
                .commandeId(1L)
                .methode(MethodePaiement.MOBILE_MONEY)
                .build();

        when(populationAuthService.getCurrentPopulationUser()).thenReturn(currentUser);
        when(commandeRepository.findByIdAndUtilisateurId(1L, 10L)).thenReturn(Optional.of(commande));
        when(paiementRepository.findByCommandeId(1L)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> paiementService.payerCommande(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("déjà été intégralement réglée");
    }

    @Test
    @DisplayName("payerCommande - Commande annulée -> BadRequestException")
    void payerCommande_CancelledOrder() {
        commande.setStatut(StatutCommande.ANNULEE);

        PopulationProcessPaiementRequest request = PopulationProcessPaiementRequest.builder()
                .commandeId(1L)
                .methode(MethodePaiement.MOBILE_MONEY)
                .build();

        when(populationAuthService.getCurrentPopulationUser()).thenReturn(currentUser);
        when(commandeRepository.findByIdAndUtilisateurId(1L, 10L)).thenReturn(Optional.of(commande));

        assertThatThrownBy(() -> paiementService.payerCommande(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("commande annulée");
    }

    @Test
    @DisplayName("getPaiementByCommande - Récupère le reçu de paiement de la commande")
    void getPaiementByCommande_Success() {
        Paiement paiement = Paiement.builder()
                .id(77L)
                .reference("PAY-OM-2026-001")
                .montant(6500.0)
                .methode(MethodePaiement.MOBILE_MONEY)
                .statut(StatutPaiement.REUSSI)
                .datePaiement(LocalDateTime.now())
                .commande(commande)
                .build();

        when(populationAuthService.getCurrentPopulationUser()).thenReturn(currentUser);
        when(commandeRepository.findByIdAndUtilisateurId(1L, 10L)).thenReturn(Optional.of(commande));
        when(paiementRepository.findByCommandeId(1L)).thenReturn(Optional.of(paiement));

        PopulationPaiementResponse response = paiementService.getPaiementByCommande(1L);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(77L);
        assertThat(response.getReference()).isEqualTo("PAY-OM-2026-001");
        assertThat(response.getStatut()).isEqualTo(StatutPaiement.REUSSI);
    }

    @Test
    @DisplayName("getHistoriquePaiements - Récupère l'historique paginé des règlements")
    void getHistoriquePaiements_Success() {
        Paiement p = Paiement.builder()
                .id(1L)
                .reference("PAY-OM-001")
                .montant(5000.0)
                .methode(MethodePaiement.MOBILE_MONEY)
                .statut(StatutPaiement.REUSSI)
                .commande(commande)
                .build();

        Pageable pageable = PageRequest.of(0, 10);
        when(populationAuthService.getCurrentPopulationUser()).thenReturn(currentUser);
        when(paiementRepository.findByCommandeUtilisateurId(10L, pageable))
                .thenReturn(new PageImpl<>(List.of(p)));

        Page<PopulationPaiementResponse> page = paiementService.getHistoriquePaiements(pageable);

        assertThat(page).isNotNull();
        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent().get(0).getReference()).isEqualTo("PAY-OM-001");
    }
}
