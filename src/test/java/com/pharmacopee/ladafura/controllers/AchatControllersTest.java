package com.pharmacopee.ladafura.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import com.pharmacopee.ladafura.controllers.pharmacopee.PharmacopeeAchatController;
import com.pharmacopee.ladafura.controllers.pharmacopee.PharmacopeeFournisseurController;
import com.pharmacopee.ladafura.dto.pharmacopee.abonnement.MotifRequest;
import com.pharmacopee.ladafura.dto.pharmacopee.achat.AchatResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.achat.EnregistrerAchatRequest;
import com.pharmacopee.ladafura.dto.pharmacopee.achat.EnregistrerReglementAchatRequest;
import com.pharmacopee.ladafura.dto.pharmacopee.achat.ReglementAchatResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.fournisseur.EnregistrerFournisseurRequest;
import com.pharmacopee.ladafura.dto.pharmacopee.fournisseur.FournisseurResponse;
import com.pharmacopee.ladafura.enums.StatutFournisseur;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeAchatService;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeFournisseurService;

@ExtendWith(MockitoExtension.class)
class AchatControllersTest {

    @Mock private IPharmacopeeAchatService achatService;
    @Mock private IPharmacopeeFournisseurService fournisseurService;

    @InjectMocks private PharmacopeeAchatController achatController;
    @InjectMocks private PharmacopeeFournisseurController fournisseurController;

    @Test
    @DisplayName("POST /api/v1/pharmacopee/achats - 201 Created")
    void creerAchat() {
        when(achatService.creer(any(EnregistrerAchatRequest.class))).thenReturn(new AchatResponse());

        assertThat(achatController.creer(new EnregistrerAchatRequest()).getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    @DisplayName("PATCH /api/v1/pharmacopee/achats/{id}/valider - 200 OK")
    void valider() {
        when(achatService.valider(1L)).thenReturn(new AchatResponse());

        assertThat(achatController.valider(1L).getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("PATCH /api/v1/pharmacopee/achats/{id}/annuler - transmet le motif")
    void annuler() {
        when(achatService.annuler(1L, "Erreur")).thenReturn(new AchatResponse());

        assertThat(achatController.annuler(1L, new MotifRequest("Erreur")).getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("POST /api/v1/pharmacopee/achats/{id}/reglements - 201 Created")
    void reglement() {
        when(achatService.enregistrerReglement(any(), any())).thenReturn(new ReglementAchatResponse());

        assertThat(achatController.enregistrerReglement(1L, new EnregistrerReglementAchatRequest()).getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    @DisplayName("DELETE /api/v1/pharmacopee/achats/{id} - 204 No Content")
    void supprimerAchat() {
        assertThat(achatController.supprimer(1L).getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(achatService).supprimer(1L);
    }

    @Test
    @DisplayName("POST /api/v1/pharmacopee/fournisseurs - 201 Created")
    void creerFournisseur() {
        when(fournisseurService.creer(any(EnregistrerFournisseurRequest.class))).thenReturn(new FournisseurResponse());

        assertThat(fournisseurController.creer(new EnregistrerFournisseurRequest()).getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    @DisplayName("PATCH /api/v1/pharmacopee/fournisseurs/{id}/desactiver - statut INACTIF")
    void desactiverFournisseur() {
        when(fournisseurService.changerStatut(1L, StatutFournisseur.INACTIF)).thenReturn(new FournisseurResponse());

        assertThat(fournisseurController.desactiver(1L).getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
