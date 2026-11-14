package com.pharmacopee.ladafura.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import com.pharmacopee.ladafura.controllers.pharmacopee.PharmacopeeCaisseController;
import com.pharmacopee.ladafura.controllers.pharmacopee.PharmacopeeDepenseController;
import com.pharmacopee.ladafura.controllers.pharmacopee.PharmacopeeTresorerieController;
import com.pharmacopee.ladafura.dto.pharmacopee.caisse.CaisseResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.caisse.CloturerSessionRequest;
import com.pharmacopee.ladafura.dto.pharmacopee.caisse.EnregistrerCaisseRequest;
import com.pharmacopee.ladafura.dto.pharmacopee.caisse.OuvrirSessionRequest;
import com.pharmacopee.ladafura.dto.pharmacopee.caisse.SessionCaisseResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.caisse.TransfertCaisseRequest;
import com.pharmacopee.ladafura.dto.pharmacopee.depense.DepenseResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.depense.EnregistrerDepenseRequest;
import com.pharmacopee.ladafura.dto.pharmacopee.tresorerie.SyntheseTresorerieResponse;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeCaisseService;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeDepenseService;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeTresorerieService;

@ExtendWith(MockitoExtension.class)
class CaisseControllersTest {

    @Mock private IPharmacopeeCaisseService caisseService;
    @Mock private IPharmacopeeDepenseService depenseService;
    @Mock private IPharmacopeeTresorerieService tresorerieService;

    @InjectMocks private PharmacopeeCaisseController caisseController;
    @InjectMocks private PharmacopeeDepenseController depenseController;
    @InjectMocks private PharmacopeeTresorerieController tresorerieController;

    @Test
    @DisplayName("POST /api/v1/pharmacopee/caisses - 201 Created")
    void creerCaisse() {
        when(caisseService.creer(any(EnregistrerCaisseRequest.class))).thenReturn(new CaisseResponse());

        assertThat(caisseController.creer(new EnregistrerCaisseRequest()).getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    @DisplayName("POST /api/v1/pharmacopee/caisses/{id}/sessions - 201 Created")
    void ouvrirSession() {
        when(caisseService.ouvrirSession(any(), any(OuvrirSessionRequest.class))).thenReturn(new SessionCaisseResponse());

        assertThat(caisseController.ouvrirSession(1L, new OuvrirSessionRequest()).getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    @DisplayName("POST /api/v1/pharmacopee/caisses/{id}/sessions/courante/cloture - 200 OK")
    void cloturerSession() {
        when(caisseService.cloturerSession(any(), any(CloturerSessionRequest.class))).thenReturn(new SessionCaisseResponse());

        assertThat(caisseController.cloturerSession(1L, new CloturerSessionRequest()).getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("POST /api/v1/pharmacopee/caisses/transferts - 201 Created")
    void transferer() {
        when(caisseService.transferer(any(TransfertCaisseRequest.class))).thenReturn(List.of());

        assertThat(caisseController.transferer(new TransfertCaisseRequest()).getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    @DisplayName("POST /api/v1/pharmacopee/depenses - 201 Created")
    void creerDepense() {
        when(depenseService.creer(any(EnregistrerDepenseRequest.class))).thenReturn(new DepenseResponse());

        assertThat(depenseController.creer(new EnregistrerDepenseRequest()).getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    @DisplayName("GET /api/v1/pharmacopee/tresorerie/synthese - 200 OK")
    void synthese() {
        LocalDate jour = LocalDate.of(2026, 11, 9);
        when(tresorerieService.getSynthese(jour, jour)).thenReturn(new SyntheseTresorerieResponse());

        assertThat(tresorerieController.getSynthese(jour, jour).getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
