package com.pharmacopee.ladafura.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Collections;
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
import com.pharmacopee.ladafura.Models.MembrePharmacopee;
import com.pharmacopee.ladafura.Models.Pharmacopee;
import com.pharmacopee.ladafura.dto.pharmacopee.client.ClientResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.client.EnregistrerClientRequest;
import com.pharmacopee.ladafura.exceptions.ConflictException;
import com.pharmacopee.ladafura.exceptions.ResourceNotFoundException;
import com.pharmacopee.ladafura.repository.ClientRepository;
import com.pharmacopee.ladafura.repository.PaiementVenteRepository;
import com.pharmacopee.ladafura.repository.VenteRepository;
import com.pharmacopee.ladafura.services.impl.PharmacopeeClientServiceImpl;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeContexteService;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeContexteService.ContexteMembre;

@ExtendWith(MockitoExtension.class)
class PharmacopeeClientServiceImplTest {

    private static final Long PHARMACOPEE_ID = 5L;

    @Mock private IPharmacopeeContexteService contexteService;
    @Mock private ClientRepository clientRepository;
    @Mock private VenteRepository venteRepository;
    @Mock private PaiementVenteRepository paiementRepository;

    @InjectMocks private PharmacopeeClientServiceImpl service;

    private final Pharmacopee pharmacopee = Pharmacopee.builder().id(PHARMACOPEE_ID).nom("Officine").build();

    @BeforeEach
    void preparer() {
        ContexteMembre contexte = new ContexteMembre(MembrePharmacopee.builder().id(1L).build(), pharmacopee, Set.of());
        lenient().when(contexteService.verifierPermission(anyString())).thenReturn(contexte);
        lenient().when(contexteService.verifierOperationCommerciale(anyString())).thenReturn(contexte);
        lenient().when(clientRepository.save(any(Client.class))).thenAnswer(i -> i.getArgument(0));
    }

    private Client client(Long id) {
        Client c = Client.builder().id(id).pharmacopee(pharmacopee).nom("Sidibé").actif(true).build();
        lenient().when(clientRepository.findByIdAndPharmacopeeId(id, PHARMACOPEE_ID)).thenReturn(Optional.of(c));
        return c;
    }

    private static EnregistrerClientRequest requete(String telephone) {
        return EnregistrerClientRequest.builder().nom("Sidibé").telephone(telephone).build();
    }

    @Test
    @DisplayName("création : téléphone vide enregistré comme absent, sans contrôle de doublon")
    void creationSansTelephone() {
        ClientResponse reponse = service.creer(requete("   "));

        assertThat(reponse.getTelephone()).isNull();
        assertThat(reponse.getEncours()).isEqualByComparingTo(BigDecimal.ZERO);
        verify(clientRepository, never()).findByPharmacopeeIdAndTelephone(any(), any());
    }

    @Test
    @DisplayName("un téléphone déjà utilisé dans la pharmacopée est refusé")
    void telephoneDouble() {
        when(clientRepository.findByPharmacopeeIdAndTelephone(PHARMACOPEE_ID, "70")).thenReturn(Optional.of(Client.builder().id(9L).build()));

        assertThatThrownBy(() -> service.creer(requete("70"))).isInstanceOf(ConflictException.class);
    }

    @Test
    @DisplayName("modifier un client en gardant son téléphone est permis")
    void modificationMemeTelephone() {
        Client c = client(9L);
        when(clientRepository.findByPharmacopeeIdAndTelephone(PHARMACOPEE_ID, "70")).thenReturn(Optional.of(c));
        when(venteRepository.totauxValidesParClient(any())).thenReturn(List.of());
        when(paiementRepository.paiementsValidesParClient(any())).thenReturn(List.of());

        assertThat(service.modifier(9L, requete("70")).getTelephone()).isEqualTo("70");
    }

    @Test
    @DisplayName("encours : total des ventes validées moins les paiements valides")
    void encours() {
        client(9L);
        when(venteRepository.totauxValidesParClient(any())).thenReturn(Collections.singletonList(new Object[] {9L, new BigDecimal("15000")}));
        when(paiementRepository.paiementsValidesParClient(any())).thenReturn(Collections.singletonList(new Object[] {9L, new BigDecimal("4000")}));

        assertThat(service.getClient(9L).getEncours()).isEqualByComparingTo("11000");
    }

    @Test
    @DisplayName("le client d'une autre pharmacopée n'est pas trouvé")
    void autrePharmacopee() {
        when(clientRepository.findByIdAndPharmacopeeId(9L, PHARMACOPEE_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getClient(9L)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("désactivation d'un client")
    void desactivation() {
        Client c = client(9L);
        when(venteRepository.totauxValidesParClient(any())).thenReturn(List.of());
        when(paiementRepository.paiementsValidesParClient(any())).thenReturn(List.of());

        service.changerActivation(9L, false);

        assertThat(c.getActif()).isFalse();
    }
}
