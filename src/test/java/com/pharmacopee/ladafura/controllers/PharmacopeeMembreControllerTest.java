package com.pharmacopee.ladafura.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.pharmacopee.ladafura.controllers.pharmacopee.PharmacopeeInvitationController;
import com.pharmacopee.ladafura.controllers.pharmacopee.PharmacopeeMembreController;
import com.pharmacopee.ladafura.controllers.pharmacopee.PharmacopeeRoleController;
import com.pharmacopee.ladafura.dto.pharmacopee.membre.CreerMembreRequest;
import com.pharmacopee.ladafura.dto.pharmacopee.membre.InvitationResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.membre.InviterMembreRequest;
import com.pharmacopee.ladafura.dto.pharmacopee.membre.MembreResponse;
import com.pharmacopee.ladafura.dto.pharmacopee.role.EnregistrerRoleRequest;
import com.pharmacopee.ladafura.dto.pharmacopee.role.RoleResponse;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeInvitationService;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeMembreService;
import com.pharmacopee.ladafura.services.interfaces.IPharmacopeeRoleService;

@ExtendWith(MockitoExtension.class)
class PharmacopeeMembreControllerTest {

    @Mock private IPharmacopeeMembreService membreService;
    @Mock private IPharmacopeeInvitationService invitationService;
    @Mock private IPharmacopeeRoleService roleService;

    @InjectMocks private PharmacopeeMembreController membreController;
    @InjectMocks private PharmacopeeInvitationController invitationController;
    @InjectMocks private PharmacopeeRoleController roleController;

    @Test
    @DisplayName("POST /api/v1/pharmacopee/membres/invitations - 201 Created")
    void inviter() {
        when(membreService.inviter(any(InviterMembreRequest.class))).thenReturn(new MembreResponse());

        assertThat(membreController.inviter(new InviterMembreRequest()).getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    @DisplayName("POST /api/v1/pharmacopee/membres - 201 Created")
    void creer() {
        when(membreService.creer(any(CreerMembreRequest.class))).thenReturn(new MembreResponse());

        assertThat(membreController.creer(new CreerMembreRequest()).getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    @DisplayName("PATCH /api/v1/pharmacopee/membres/{id}/suspendre - 200 OK")
    void suspendre() {
        MembreResponse reponse = new MembreResponse();
        when(membreService.suspendre(10L)).thenReturn(reponse);

        ResponseEntity<MembreResponse> resultat = membreController.suspendre(10L);

        assertThat(resultat.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resultat.getBody()).isSameAs(reponse);
    }

    @Test
    @DisplayName("GET /api/v1/pharmacopee/invitations - 200 OK")
    void mesInvitations() {
        when(invitationService.getMesInvitations()).thenReturn(List.of(new InvitationResponse()));

        assertThat(invitationController.getMesInvitations().getBody()).hasSize(1);
    }

    @Test
    @DisplayName("POST /api/v1/pharmacopee/invitations/{id}/accepter - 204 No Content")
    void accepter() {
        assertThat(invitationController.accepter(9L).getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(invitationService).accepter(9L);
    }

    @Test
    @DisplayName("POST /api/v1/pharmacopee/roles - 201 Created")
    void creerRole() {
        when(roleService.creer(any(EnregistrerRoleRequest.class))).thenReturn(new RoleResponse());

        assertThat(roleController.creer(new EnregistrerRoleRequest()).getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    @DisplayName("DELETE /api/v1/pharmacopee/roles/{id} - 204 No Content")
    void supprimerRole() {
        assertThat(roleController.supprimer(30L).getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(roleService).supprimer(30L);
    }
}
