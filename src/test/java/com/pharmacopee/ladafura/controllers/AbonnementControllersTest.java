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

import com.pharmacopee.ladafura.controllers.admin.AdminAbonnementController;
import com.pharmacopee.ladafura.controllers.admin.AdminOffreController;
import com.pharmacopee.ladafura.controllers.pharmacopee.PharmacopeeAbonnementController;
import com.pharmacopee.ladafura.dto.admin.abonnement.EnregistrerOffreRequest;
import com.pharmacopee.ladafura.dto.pharmacopee.abonnement.AbonnementResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.abonnement.DeclarerPaiementRequest;
import com.pharmacopee.ladafura.dto.pharmacopee.abonnement.FactureAbonnementResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.abonnement.OffreResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.abonnement.PaiementAbonnementResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.abonnement.SouscrireOffreRequest;
import com.pharmacopee.ladafura.services.interfaces.IAdminAbonnementService;
import com.pharmacopee.ladafura.services.interfaces.IAdminOffreService;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeAbonnementService;

@ExtendWith(MockitoExtension.class)
class AbonnementControllersTest {

    @Mock private IPharmacopeeAbonnementService abonnementService;
    @Mock private IAdminOffreService offreService;
    @Mock private IAdminAbonnementService adminAbonnementService;

    @InjectMocks private PharmacopeeAbonnementController pharmacopeeController;
    @InjectMocks private AdminOffreController offreController;
    @InjectMocks private AdminAbonnementController adminController;

    @Test
    @DisplayName("POST /api/v1/pharmacopee/abonnement/souscriptions - 201 Created")
    void souscrire() {
        when(abonnementService.souscrire(any(SouscrireOffreRequest.class))).thenReturn(new AbonnementResponse());

        assertThat(pharmacopeeController.souscrire(new SouscrireOffreRequest()).getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    @DisplayName("POST /api/v1/pharmacopee/abonnement/souscriptions/{id}/paiements - 201 Created")
    void declarerPaiement() {
        when(abonnementService.declarerPaiement(any(), any(DeclarerPaiementRequest.class))).thenReturn(new PaiementAbonnementResponse());

        assertThat(pharmacopeeController.declarerPaiement(1L, new DeclarerPaiementRequest()).getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    @DisplayName("POST /api/v1/admin/offres - 201 Created")
    void creerOffre() {
        when(offreService.creer(any(EnregistrerOffreRequest.class))).thenReturn(new OffreResponse());

        assertThat(offreController.creer(new EnregistrerOffreRequest()).getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    @DisplayName("PATCH /api/v1/admin/paiements-abonnement/{id}/confirmer - 200 OK")
    void confirmer() {
        when(adminAbonnementService.confirmerPaiement(9L)).thenReturn(new FactureAbonnementResponse());

        assertThat(adminController.confirmerPaiement(9L).getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
