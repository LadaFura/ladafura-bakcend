package com.pharmacopee.ladafura.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import com.pharmacopee.ladafura.controllers.pharmacopee.PharmacopeeClientController;
import com.pharmacopee.ladafura.controllers.pharmacopee.PharmacopeeVenteController;
import com.pharmacopee.ladafura.dto.pharmacopee.abonnement.MotifRequest;
import com.pharmacopee.ladafura.dto.pharmacopee.client.ClientResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.client.EnregistrerClientRequest;
import com.pharmacopee.ladafura.dto.pharmacopee.vente.EnregistrerVenteRequest;
import com.pharmacopee.ladafura.dto.pharmacopee.vente.PaiementVenteRequest;
import com.pharmacopee.ladafura.dto.pharmacopee.vente.PaiementVenteResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.vente.VenteResponse;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeClientService;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeVenteService;

@ExtendWith(MockitoExtension.class)
class VenteControllersTest {

    @Mock private IPharmacopeeVenteService venteService;
    @Mock private IPharmacopeeClientService clientService;

    @InjectMocks private PharmacopeeVenteController venteController;
    @InjectMocks private PharmacopeeClientController clientController;

    @Test
    @DisplayName("POST /api/v1/pharmacopee/ventes - 201 Created")
    void creer() {
        when(venteService.creer(any(EnregistrerVenteRequest.class))).thenReturn(new VenteResponse());

        assertThat(venteController.creer(new EnregistrerVenteRequest()).getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    @DisplayName("POST /api/v1/pharmacopee/ventes/{id}/valider - 200 OK, corps facultatif")
    void valider() {
        when(venteService.valider(1L, null)).thenReturn(new VenteResponse());

        assertThat(venteController.valider(1L, null).getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("PATCH /api/v1/pharmacopee/ventes/{id}/annuler - transmet le motif")
    void annuler() {
        when(venteService.annuler(1L, "Erreur")).thenReturn(new VenteResponse());

        assertThat(venteController.annuler(1L, new MotifRequest("Erreur")).getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("POST /api/v1/pharmacopee/ventes/{id}/paiements - 201 Created")
    void paiement() {
        when(venteService.enregistrerPaiement(any(), any(PaiementVenteRequest.class))).thenReturn(new PaiementVenteResponse());

        assertThat(venteController.enregistrerPaiement(1L, new PaiementVenteRequest()).getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    @DisplayName("POST /api/v1/pharmacopee/clients - 201 Created")
    void creerClient() {
        when(clientService.creer(any(EnregistrerClientRequest.class))).thenReturn(new ClientResponse());

        assertThat(clientController.creer(new EnregistrerClientRequest()).getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    @DisplayName("PATCH /api/v1/pharmacopee/clients/{id}/desactiver - 200 OK")
    void desactiverClient() {
        when(clientService.changerActivation(1L, false)).thenReturn(new ClientResponse());

        assertThat(clientController.desactiver(1L).getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
